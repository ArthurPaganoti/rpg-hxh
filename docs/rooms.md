# Salas (Rooms)

Este documento descreve o sistema de criacao e convite de Salas de RPG da API RPG HxH.

## Visao Geral

O fluxo de criacao de salas permite que usuarios autenticados criem salas de RPG, tornando-se automaticamente o Mestre (Master) da sala. Salas sao **privadas por padrao** (Security by Default). Salas privadas recebem um codigo de convite unico de 6 caracteres. O link de convite seguro e armazenado no **Redis com TTL de 8 horas**.

## Endpoints

### `POST /rooms` — Criar Sala

**Autenticacao**: Obrigatoria (Bearer JWT).

#### Headers

| Header | Valor |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <token>` |

#### Body (`CreateRoomDTO`)

| Campo | Tipo | Obrigatorio | Regras de Validacao |
|-------|------|-------------|---------------------|
| `name` | `String` | Sim | Nao pode ser vazio (`@NotBlank`) |
| `isPrivate` | `Boolean` | Nao | Default `true` (Security by Default) |

#### Exemplo (sem `isPrivate` — privada por padrao)

```json
{
  "name": "Sala do Gon"
}
```

#### Exemplo (Sala Publica)

```json
{
  "name": "Sala Publica",
  "isPrivate": false
}
```

#### Respostas

**201 Created — Sala Criada com Sucesso (Privada):**

```json
{
  "success": true,
  "message": "Sala criada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala do Gon",
    "masterName": "Gon Freecss",
    "isPrivate": true,
    "inviteCode": "Ab3xZ9",
    "currentPlayers": 1,
    "maxPlayers": 10,
    "createdAt": "2026-03-24T12:00:00"
  },
  "timestamp": "2026-03-24T15:00:00Z"
}
```

**201 Created — Sala Publica:**

```json
{
  "success": true,
  "message": "Sala criada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala Publica",
    "masterName": "Gon Freecss",
    "isPrivate": false,
    "inviteCode": null,
    "currentPlayers": 1,
    "maxPlayers": 10,
    "createdAt": "2026-03-24T12:00:00"
  },
  "timestamp": "2026-03-24T15:00:00Z"
}
```

**400 Bad Request — Erro de Validacao:**

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Erro de validacao dos campos",
  "content": {
    "name": "O nome da sala e obrigatorio"
  },
  "timestamp": "2026-03-24T15:00:00Z"
}
```

**401/403 — Nao Autenticado:**

Retornado quando o token JWT esta ausente ou invalido.

---

### `GET /rooms/{id}/invite` — Gerar Link de Convite

**Autenticacao**: Obrigatoria (Bearer JWT).
**Restricao**: Apenas o **Mestre** da sala pode acessar este endpoint.

#### Parametros

| Parametro | Tipo | Local | Descricao |
|-----------|------|-------|-----------|
| `id` | `UUID` | Path | ID da sala |

#### Respostas

**200 OK — Link Gerado com Sucesso:**

```json
{
  "success": true,
  "message": "Link de convite gerado com sucesso",
  "content": {
    "inviteUrl": "https://api.rpg.com/rooms/join/550e8400-e29b-41d4-a716-446655440000"
  },
  "timestamp": "2026-03-28T15:00:00Z"
}
```

**403 Forbidden — Usuario nao e o Mestre:**

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "Apenas o Mestre da sala pode gerar o link de convite",
  "timestamp": "2026-03-28T15:00:00Z"
}
```

**404 Not Found — Sala nao encontrada:**

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "Sala nao encontrada",
  "timestamp": "2026-03-28T15:00:00Z"
}
```

---

## Arquitetura

A feature segue o padrao de **Functional Slice**:

```
rooms/
  controller/RoomController.java         -- Endpoints REST (POST /rooms, GET /rooms/{id}/invite)
  service/RoomService.java               -- Logica de criacao de sala e geracao de convite
  service/RedisInviteService.java        -- Gerenciamento de convites no Redis (TTL 8h)
  dto/CreateRoomDTO.java                 -- DTO de requisicao com validacoes
  dto/RoomResponseDTO.java               -- DTO de resposta com dados da sala
  dto/InviteResponseDTO.java             -- DTO de resposta com URL de convite

entities/room/
  entity/Room.java                       -- Entidade JPA
  repository/RoomRepository.java         -- Spring Data Repository
```

### Fluxo de Criacao

```
Cliente (com header Authorization: Bearer <token>)
  |
  v
RoomController (@Valid @RequestBody CreateRoomDTO)
  |  -- Jakarta Bean Validation executa aqui
  |  -- Falhas -> GlobalExceptionHandler -> VALIDATION_ERROR
  v
RoomService.createRoom(dto)
  |  1. Extrai email do SecurityContextHolder
  |  2. Busca usuario por email via UserRepository
  |  3. isPrivate defaults to true quando null
  |  4. Se isPrivate=true, gera invite_code (6 chars alfanumericos)
  |  5. Cria Room com master=usuario, currentPlayers=1, maxPlayers=10
  |  6. Salva via RoomRepository
  v
RoomController retorna 201 Created + ResponseDTO.success(RoomResponseDTO)
```

### Fluxo de Geracao de Convite

```
Cliente (com header Authorization: Bearer <token>)
  |
  v
RoomController GET /rooms/{id}/invite
  |
  v
RoomService.getInviteLink(roomId)
  |  1. Extrai email do SecurityContextHolder
  |  2. Busca usuario por email
  |  3. Busca sala por ID (404 se nao encontrada)
  |  4. Verifica se usuario e o Mestre (403 se nao for)
  |  5. Delega para RedisInviteService.getOrCreateInvite()
  |  6. Retorna URL: https://api.rpg.com/rooms/join/{invite_hash}
  v
RoomController retorna 200 OK + ResponseDTO.success(InviteResponseDTO)
```

### Armazenamento no Redis

```
Chave:    invite:{roomId}
Tipo:     Hash
TTL:      8 horas
Campos:
  - inviteHash  -> UUID criptograficamente seguro
  - masterId    -> ID do Mestre que gerou o convite
  - createdAt   -> Timestamp de criacao
```

Se o convite ja existir no Redis (dentro do prazo de 8h), retorna o existente.
Se expirou ou nao existe, gera um novo hash e salva com TTL renovado.

## Regras de Negocio

| Regra | Descricao |
|-------|-----------|
| Mastership | O usuario autenticado se torna o Mestre da sala |
| Multiplas salas | Um usuario pode ser Mestre de multiplas salas |
| Privacy by Default | `isPrivate` defaults to `true` quando nao informado (Security by Default) |
| Invite Code | Se `isPrivate=true`, um `invite_code` unico de 6 caracteres e gerado (PostgreSQL) |
| Invite Link | Link de convite gerado sob demanda via `GET /rooms/{id}/invite` (Redis, TTL 8h) |
| Master-only Invite | Apenas o Mestre pode gerar/obter o link de convite |
| Jogadores iniciais | `current_players` inicia com 1 (o Mestre) |
| Limite de jogadores | `max_players` padrao e 10 |

## Invite System (Sistema de Convite Seguro)

### Invite Code (PostgreSQL)
O `invite_code` e gerado apenas para salas privadas (`isPrivate=true`):
- 6 caracteres alfanumericos (A-Z, a-z, 0-9)
- Gerado com `SecureRandom` para seguranca
- Armazenado como coluna `UNIQUE` no banco
- Persistente (nao expira)

### Invite Link (Redis)
O link de convite e gerado sob demanda pelo Mestre:
- UUID criptograficamente seguro (`UUID.randomUUID()`)
- Armazenado no Redis como Hash com TTL de 8 horas
- Campos: `inviteHash`, `masterId`, `createdAt`
- Se ja existir no Redis, retorna o hash existente
- Se expirou, gera novo hash automaticamente
- URL formato: `https://api.rpg.com/rooms/join/{invite_hash}`
- A logica de join via URL sera implementada em uma task futura

## Migracoes de Banco de Dados

```sql
-- V2__Create_Rooms_Table.sql
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    master_id BIGINT NOT NULL REFERENCES users(id),
    is_private BOOLEAN NOT NULL DEFAULT false,
    invite_code VARCHAR(6) UNIQUE,
    current_players INTEGER NOT NULL DEFAULT 1,
    max_players INTEGER NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V3__Add_Invite_Hash_And_Default_Privacy.sql
ALTER TABLE rooms ALTER COLUMN is_private SET DEFAULT TRUE;
```

## Swagger

A feature esta documentada no Swagger com:

- **Tag**: "Salas" — Agrupa os endpoints de gerenciamento de salas
- **Seguranca**: Requer `bearerAuth` (JWT) configurado globalmente
- **Endpoints**: `POST /rooms`, `GET /rooms/{id}/invite`
