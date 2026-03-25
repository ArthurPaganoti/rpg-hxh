# Salas (Rooms)

Este documento descreve o sistema de criacao de Salas de RPG da API RPG HxH.

## Visao Geral

O fluxo de criacao de salas permite que usuarios autenticados criem salas de RPG, tornando-se automaticamente o Mestre (Master) da sala. Salas podem ser publicas ou privadas. Salas privadas recebem um codigo de convite unico de 6 caracteres.

## Endpoint

```
POST /rooms
```

**Autenticacao**: Obrigatoria (Bearer JWT).

## Requisicao

### Headers

| Header | Valor |
|--------|-------|
| `Content-Type` | `application/json` |
| `Authorization` | `Bearer <token>` |

### Body (`CreateRoomDTO`)

| Campo | Tipo | Obrigatorio | Regras de Validacao |
|-------|------|-------------|---------------------|
| `name` | `String` | Sim | Nao pode ser vazio (`@NotBlank`) |
| `isPrivate` | `boolean` | Nao | Default `false` |

### Exemplo

```json
{
  "name": "Sala do Gon",
  "isPrivate": false
}
```

### Exemplo (Sala Privada)

```json
{
  "name": "Sala Secreta",
  "isPrivate": true
}
```

## Respostas

### 201 Created — Sala Criada com Sucesso

**Body (Sala Publica):**

```json
{
  "success": true,
  "message": "Sala criada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala do Gon",
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

**Body (Sala Privada):**

```json
{
  "success": true,
  "message": "Sala criada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala Secreta",
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

### 400 Bad Request — Erro de Validacao

Retornado quando os campos do `CreateRoomDTO` falham na validacao.

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

### 401/403 — Nao Autenticado

Retornado quando o token JWT esta ausente ou invalido.

## Arquitetura

A feature segue o padrao de **Functional Slice**:

```
rooms/
  controller/RoomController.java         -- Endpoint REST
  service/RoomService.java               -- Logica de criacao de sala
  dto/CreateRoomDTO.java                 -- DTO de requisicao com validacoes
  dto/RoomResponseDTO.java               -- DTO de resposta com dados da sala

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
  |  3. Se isPrivate=true, gera invite_code (6 chars alfanumericos)
  |  4. Cria Room com master=usuario, currentPlayers=1, maxPlayers=10
  |  5. Salva via RoomRepository
  v
RoomController retorna 201 Created + ResponseDTO.success(RoomResponseDTO)
```

## Regras de Negocio

| Regra | Descricao |
|-------|-----------|
| Mastership | O usuario autenticado se torna o Mestre da sala |
| Multiplas salas | Um usuario pode ser Mestre de multiplas salas |
| Privacidade | Se `isPrivate=true`, um `invite_code` unico de 6 caracteres e gerado |
| Jogadores iniciais | `current_players` inicia com 1 (o Mestre) |
| Limite de jogadores | `max_players` padrao e 10 |

## Invite Code

O `invite_code` e gerado apenas para salas privadas (`isPrivate=true`):
- 6 caracteres alfanumericos (A-Z, a-z, 0-9)
- Gerado com `SecureRandom` para seguranca
- Armazenado como coluna `UNIQUE` no banco

## Migracao de Banco de Dados

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
```

## Swagger

A feature esta documentada no Swagger com:

- **Tag**: "Salas" — Agrupa os endpoints de gerenciamento de salas
- **Seguranca**: Requer `bearerAuth` (JWT) configurado globalmente
