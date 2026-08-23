# Salas (Rooms)

Este documento descreve o sistema de criacao e convite de Salas de RPG da API RPG HxH.

## Visao Geral

O fluxo de criacao de salas permite que usuarios autenticados criem salas de RPG, tornando-se automaticamente o Mestre (Master) da sala. Todas as salas sao privadas — o acesso e feito exclusivamente via link de convite. O link de convite seguro e armazenado no **Redis com TTL de 8 horas**.

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
| `name` | `String` | Sim | Nao pode ser vazio (`@NotBlank`); entre 3 e 100 caracteres (`@Size`) |
| `maxPlayers` | `Integer` | Nao | Entre 2 e 10 (`@Min`/`@Max`); quando omitido, padrao 10 |

#### Exemplo

```json
{
  "name": "Sala do Gon"
}
```

#### Respostas

**201 Created — Sala Criada com Sucesso:**

```json
{
  "success": true,
  "message": "Sala criada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala do Gon",
    "masterName": "Gon Freecss",
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

### `DELETE /rooms/{id}/invite` — Revogar Convite

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode revogar.

Invalida o link de convite ativo antes do fim das 8 horas (util quando o link vaza). Reusa `RedisInviteService.removeInvite`, que apaga tanto a hash da sala quanto o indice reverso (hash -> roomId). **Idempotente**: revogar quando nao ha convite ativo retorna 200 do mesmo jeito. Um novo `GET /rooms/{id}/invite` gera um hash diferente.

#### Respostas

**200 OK — Convite Revogado:**

```json
{
  "success": true,
  "message": "Convite revogado com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

**403 Forbidden — Usuario nao e o Mestre** e **404 Not Found — Sala nao encontrada**: mesmo formato dos demais endpoints (`BUSINESS_ERROR`), validados via `findRoomAsMaster`.

---

### `GET /rooms` — Listar Minhas Salas

**Autenticacao**: Obrigatoria (Bearer JWT).

Lista todas as salas em que o usuario autenticado participa — como Mestre ou como jogador — ordenadas da mais recente para a mais antiga. Como o Mestre tambem e registrado em `room_players` na criacao, uma unica query cobre os dois papeis (`findRoomsByUser`, com `JOIN FETCH` do Mestre para evitar N+1).

#### Respostas

**200 OK:**

```json
{
  "success": true,
  "message": "Salas listadas com sucesso",
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Sala do Gon",
      "masterName": "Gon Freecss",
      "currentPlayers": 2,
      "maxPlayers": 10,
      "createdAt": "2026-03-24T12:00:00"
    }
  ],
  "timestamp": "2026-07-19T15:00:00Z"
}
```

Usuario sem salas recebe `content: []`.

---

### `GET /rooms/{id}` — Detalhes da Sala

**Autenticacao**: Obrigatoria (Bearer JWT). **Qualquer membro da sala** pode acessar (nao-membros recebem 403).

Retorna os dados de uma sala especifica. `isMaster` reflete se o usuario autenticado e o Mestre. Util para abrir a tela da sala diretamente por URL, sem depender da listagem `GET /rooms`.

#### Respostas

**200 OK:**

```json
{
  "success": true,
  "message": "Sala encontrada com sucesso",
  "content": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Sala do Gon",
    "masterName": "Gon Freecss",
    "currentPlayers": 2,
    "maxPlayers": 10,
    "createdAt": "2026-08-01T12:00:00",
    "isMaster": true
  },
  "timestamp": "2026-08-22T15:00:00Z"
}
```

**403 Forbidden — Nao-membro** (`BUSINESS_ERROR`, "Apenas membros da sala podem acessar") e **404 Not Found — Sala nao encontrada**: mesmo formato dos demais endpoints.

---

### `GET /rooms/{id}/members` — Listar Membros da Sala

**Autenticacao**: Obrigatoria (Bearer JWT). **Qualquer membro da sala** pode acessar (validado via `findRoomAsMember`; nao-membros recebem 403).

Lista os membros ordenados pela data de entrada (`joined_at ASC`). O Mestre e sempre o primeiro (entrou na criacao da sala). Query com `JOIN FETCH` do usuario para evitar N+1.

#### Respostas

**200 OK:**

```json
{
  "success": true,
  "message": "Membros listados com sucesso",
  "content": [
    { "id": 1, "name": "Gon Freecss", "joinedAt": "2026-08-01T12:00:00", "isMaster": true },
    { "id": 2, "name": "Killua Zoldyck", "joinedAt": "2026-08-01T12:05:00", "isMaster": false }
  ],
  "timestamp": "2026-08-01T15:00:00Z"
}
```

**403 Forbidden — Nao-membro** (`BUSINESS_ERROR`, "Apenas membros da sala podem acessar") e **404 Not Found — Sala nao encontrada**: mesmo formato dos endpoints anteriores.

---

### `PATCH /rooms/{id}` — Atualizar Nome da Sala

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode atualizar (validado via `findRoomAsMaster`).

#### Body (`UpdateRoomDTO`)

| Campo | Tipo | Obrigatorio | Regras de Validacao |
|-------|------|-------------|---------------------|
| `name` | `String` | Sim | Nao pode ser vazio (`@NotBlank`); entre 3 e 100 caracteres (`@Size`) |
| `maxPlayers` | `Integer` | Nao | Entre 2 e 10 (`@Min`/`@Max`); nao pode ser menor que `current_players` (409 `MaxPlayersBelowCurrentException`) |

O `RoomResponseDTO` inclui `isMaster` (calculado por requisicao), usado pelo frontend para exibir acoes de Mestre.

#### Respostas

**200 OK — Sala Atualizada:**

```json
{
  "success": true,
  "message": "Sala atualizada com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala do Gon Renovada",
    "masterName": "Gon Freecss",
    "currentPlayers": 1,
    "maxPlayers": 10,
    "createdAt": "2026-03-24T12:00:00"
  },
  "timestamp": "2026-07-19T15:00:00Z"
}
```

**400 Bad Request** (`VALIDATION_ERROR`), **403 Forbidden** e **404 Not Found** (`BUSINESS_ERROR`): mesmo formato dos endpoints anteriores.

---

### `DELETE /rooms/{id}` — Deletar Sala

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode deletar.

Deleta a sala permanentemente, removendo na mesma transacao:
1. O convite ativo no Redis (`RedisInviteService.removeInvite`)
2. Todos os registros de `room_players` (`deleteByRoom` — a FK nao tem cascade)
3. A sala em si

#### Respostas

**200 OK — Sala Deletada:**

```json
{
  "success": true,
  "message": "Sala deletada com sucesso",
  "timestamp": "2026-07-19T15:00:00Z"
}
```

**403 Forbidden — Usuario nao e o Mestre** e **404 Not Found — Sala nao encontrada**: mesmo formato dos endpoints anteriores (`BUSINESS_ERROR`), validados via `findRoomAsMaster`.

---

### `POST /rooms/{id}/leave` — Sair da Sala

**Autenticacao**: Obrigatoria (Bearer JWT). **Qualquer membro** da sala pode sair, **exceto o Mestre**.

Remove o jogador autenticado da sala na mesma transacao (com lock pessimista na sala para recalculo seguro):
1. Carrega a sala com `findByIdWithLock` (404 se nao existir)
2. Se o autenticado for o Mestre -> 409 (o Mestre sai deletando a sala, nao via leave)
3. Localiza o `RoomPlayer` do usuario (`findByRoomAndUser`) -> 403 se nao for membro
4. Remove o `RoomPlayer` e recalcula `currentPlayers = countByRoom`

#### Respostas

**200 OK — Saiu da Sala:**

```json
{
  "success": true,
  "message": "Voce saiu da sala com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

| Codigo | Situacao | Mensagem |
|--------|----------|----------|
| 403 | Nao e membro da sala | Apenas membros da sala podem acessar |
| 404 | Sala nao encontrada | Sala nao encontrada |
| 409 | Mestre tentando sair | O Mestre nao pode sair da sala. Delete a sala em vez disso |

---

### `DELETE /rooms/{id}/members/{userId}` — Remover Membro

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode remover jogadores.

Remove o jogador alvo da sala na mesma transacao (com lock pessimista na sala):
1. Carrega a sala com `findByIdWithLock` (404 se nao existir)
2. Autenticado nao e o Mestre -> 403
3. Alvo e o proprio Mestre -> 409 (o Mestre nao pode ser removido; deve deletar a sala)
4. Busca o usuario alvo (`userRepository.findById`) -> 404 se nao existir
5. Localiza o `RoomPlayer` do alvo (`findByRoomAndUser`) -> 404 se nao for membro
6. Remove o `RoomPlayer` e recalcula `currentPlayers = countByRoom`

#### Respostas

**200 OK — Jogador Removido:**

```json
{
  "success": true,
  "message": "Jogador removido da sala com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

| Codigo | Situacao | Mensagem |
|--------|----------|----------|
| 403 | Nao e o Mestre da sala | Acesso negado: apenas o Mestre da sala |
| 404 | Sala inexistente / usuario inexistente / alvo nao e membro | Sala nao encontrada / Usuario nao encontrado / Jogador nao esta na sala |
| 409 | Tentativa de remover o Mestre | O Mestre nao pode ser removido da sala |

---

### `POST /rooms/{id}/bans/{userId}` — Banir Jogador

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode banir.

Bane um jogador: remove-o da sala se for membro (recalcula `current_players`) e grava o banimento, impedindo que ele reentre **mesmo com um link de convite valido**. Na mesma transacao, com lock pessimista na sala:
1. Carrega a sala (`findByIdWithLock`, 404 se nao existir) e valida Mestre (403)
2. Alvo e o proprio Mestre -> 409 `CannotBanMasterException`
3. Busca o usuario alvo (404 se nao existir)
4. Se for membro, remove o `RoomPlayer` e recalcula `current_players`
5. Grava `RoomBan` se ainda nao existir (**idempotente**: banir de novo nao duplica)

**200 OK:** `{ "success": true, "message": "Jogador banido da sala com sucesso" }`

| Codigo | Situacao |
|--------|----------|
| 403 | Nao e o Mestre da sala |
| 404 | Sala ou usuario inexistente |
| 409 | Tentativa de banir o Mestre |

### `DELETE /rooms/{id}/bans/{userId}` — Desbanir Jogador

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode desbanir. Remove o `RoomBan`, permitindo que o jogador volte a entrar via convite.

**200 OK:** `{ "success": true, "message": "Banimento removido com sucesso" }`

| Codigo | Situacao |
|--------|----------|
| 403 | Nao e o Mestre da sala |
| 404 | Sala/usuario inexistente ou jogador nao esta banido |

### `GET /rooms/{id}/bans` — Listar Banidos

**Autenticacao**: Obrigatoria (Bearer JWT). **Apenas o Mestre** da sala pode listar. Retorna os banidos (`id`, `name`, `bannedAt`) ordenados pela data do banimento. **403** para nao-Mestre, **404** se a sala nao existir.

---

### Descricao da sala

`description` (opcional, ate 500 caracteres) faz parte de `CreateRoomDTO`/`UpdateRoomDTO` e volta em `RoomResponseDTO`. Exibida na home (`GET /rooms`) e nos detalhes. **Atencao:** em `UpdateRoomDTO`, `@Min/@Max` pertencem ao `maxPlayers` — nao devem ficar sobre o `description` (bug ja corrigido; ha teste de regressao no controller).

### `POST /rooms/{id}/cover` — Enviar/atualizar imagem da sala

`multipart/form-data` com o campo `file`. **Apenas o Mestre.** Aceita PNG/JPG/WEBP; grava no MinIO na chave `rooms/{roomId}/cover` (sobrescreve) e seta `cover_object_key`. Tipo invalido -> **415** `InvalidImageTypeException`.

### `GET /rooms/{id}/cover` — Baixar imagem da sala

Retorna a imagem (stream do MinIO). **Qualquer membro** acessa; nao-membro **403**; sala sem capa **404** `CoverNotFoundException`. O front busca como blob (Authorization Bearer) e usa object URL em `<img>` — o MinIO fica privado.

### `DELETE /rooms/{id}/cover` — Remover imagem da sala

**Apenas o Mestre.** Remove o objeto no MinIO e limpa `cover_object_key`. Idempotente.

`RoomResponseDTO.hasCover` indica se a sala tem imagem. `deleteRoom` remove o objeto de capa junto com sheets/players/bans/convite.

---

## Arquitetura

A feature segue o padrao de **Functional Slice**:

```
rooms/
  controller/RoomController.java         -- Endpoints REST (POST /rooms, GET /rooms/{id}/invite, GET /rooms/join/{hash})
  service/RoomService.java               -- Logica de criacao, convite e entrada na sala
  service/RedisInviteService.java        -- Gerenciamento de convites no Redis (TTL 8h, lookup reverso)
  dto/CreateRoomDTO.java                 -- DTO de requisicao com validacoes
  dto/RoomResponseDTO.java               -- DTO de resposta com dados da sala
  dto/InviteResponseDTO.java             -- DTO de resposta com URL de convite

entities/room/
  entity/Room.java                       -- Entidade JPA
  entity/RoomPlayer.java                 -- Entidade JPA (jogadores na sala)
  repository/RoomRepository.java         -- Spring Data Repository
  repository/RoomPlayerRepository.java   -- Spring Data Repository (existsByRoomAndUser)
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
  |  3. Cria Room com master=usuario, currentPlayers=1, maxPlayers=10
  |  4. Salva via RoomRepository
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
  |  1. findRoomAsMaster(roomId) — valida autenticacao e mastership
  |  2. Delega para RedisInviteService.getOrCreateInvite()
  |  3. Retorna URL: {INVITE_BASE_URL}{invite_hash} (base configuravel via .env)
  v
RoomController retorna 200 OK + ResponseDTO.success(InviteResponseDTO)
```

### Autorizacao de Mestre (helpers centralizados)

O `RoomService` centraliza a autorizacao em dois helpers privados, reutilizados por todas as operacoes de sala:

| Helper | Responsabilidade |
|--------|------------------|
| `getAuthenticatedUser()` | Valida que ha `Authentication` no `SecurityContextHolder` (guard contra NPE), extrai o email e busca o usuario (`UserNotFoundException` se nao encontrado) |
| `findRoomAsMaster(UUID roomId)` | Chama `getAuthenticatedUser()`, busca a sala (`RoomNotFoundException` 404) e valida que o usuario autenticado e o Mestre (`RoomAccessDeniedException` 403) |
| `findRoomAsMember(UUID roomId)` | Chama `getAuthenticatedUser()`, busca a sala (404) e valida que o usuario e membro via `existsByRoomAndUser` (`RoomMembershipRequiredException` 403) |

**Regra:** toda nova rota restrita ao Mestre (deletar sala, atualizar nome, expulsar/banir membros, convite por email) **deve** reutilizar `findRoomAsMaster`; toda rota restrita a membros (detalhes da sala, eventos SSE do ADR 005) **deve** reutilizar `findRoomAsMember`.

### Armazenamento no Redis

```
Chave:    invite:{roomId}           (Hash)
TTL:      8 horas
Campos:
  - inviteHash  -> UUID criptograficamente seguro
  - masterId    -> ID do Mestre que gerou o convite
  - createdAt   -> Timestamp de criacao

Chave:    invite:hash:{hash}        (String — indice reverso)
TTL:      8 horas
Valor:    roomId (UUID como string)
```

Se o convite ja existir no Redis (dentro do prazo de 8h), retorna o existente.
Se expirou ou nao existe, gera um novo hash e salva com TTL renovado.

## Regras de Negocio

| Regra | Descricao |
|-------|-----------|
| Mastership | O usuario autenticado se torna o Mestre da sala |
| Multiplas salas | Um usuario pode ser Mestre de multiplas salas |
| Invite Link | Link de convite gerado sob demanda via `GET /rooms/{id}/invite` (Redis, TTL 8h) |
| Master-only Invite | Apenas o Mestre pode gerar/obter o link de convite |
| Revogar convite | Apenas o Mestre revoga via `DELETE /rooms/{id}/invite`; invalida o link vazado antes das 8h (idempotente); novo GET gera hash diferente |
| Jogadores iniciais | `current_players` inicia com 1 (o Mestre) |
| Contagem de jogadores | Apos cada join, `current_players` e recalculado via `COUNT(room_players)` (fonte de verdade e a tabela `room_players`, nao o contador manual) |
| Limite de jogadores | `max_players` configuravel na criacao (2 a 10); padrao 10 quando omitido |
| Atualizacao de nome | Apenas o Mestre atualiza (`findRoomAsMaster`); nome entre 3 e 100 caracteres |
| Listagem de salas | `GET /rooms` retorna as salas do usuario autenticado (Mestre ou jogador), ordenadas por criacao decrescente |
| Detalhes da sala | `GET /rooms/{id}` retorna uma sala especifica; qualquer membro acessa (`findRoomAsMember`), nao-membros recebem 403 |
| Join via convite | Jogador autenticado entra na sala via `GET /rooms/join/{hash}` |
| Sala cheia | Retorna 409 se `current_players >= max_players` |
| Membros da sala | Qualquer membro ve a lista de membros (`findRoomAsMember`); nao-membros recebem 403 |
| Delete de sala | Apenas o Mestre deleta (`findRoomAsMaster`); remove convite Redis, `room_players` e a sala na mesma transacao |
| Sair da sala | Qualquer membro sai via `POST /rooms/{id}/leave`; remove o `RoomPlayer` e recalcula `current_players`; o Mestre nao pode sair (409), deve deletar a sala |
| Remover membro | Apenas o Mestre remove via `DELETE /rooms/{id}/members/{userId}`; recalcula `current_players`; o Mestre nao pode ser removido (409); alvo precisa ser membro (404) |
| Banir jogador | Apenas o Mestre bane via `POST /rooms/{id}/bans/{userId}`; remove da sala e impede reentrada mesmo com convite valido (`joinRoom` checa `room_bans` -> 403); o Mestre nao pode ser banido (409); idempotente |
| Desbanir jogador | Apenas o Mestre desbane via `DELETE /rooms/{id}/bans/{userId}`; jogador nao banido -> 404 |
| Cleanup no delete | `deleteRoom` remove `room_bans`, `room_sheets` (objetos MinIO + linhas) e a imagem de capa, alem de `room_players` e convite (FK sem cascade) |
| Descricao | `description` opcional (ate 500 caracteres) no create/update; exibida na home e nos detalhes |
| Imagem de capa | Mestre envia via `POST /rooms/{id}/cover` (PNG/JPG/WEBP, MinIO); membros baixam via `GET /rooms/{id}/cover`; `hasCover` no response |
| Duplicidade | Retorna 409 se jogador ja esta na sala (ou e o proprio Mestre) |
| Convite invalido | Retorna 404 se o hash nao existe ou expirou |

### `GET /rooms/join/{hash}` — Entrar em uma Sala

**Autenticacao**: Obrigatoria (Bearer JWT).

#### Parametros

| Parametro | Tipo | Local | Descricao |
|-----------|------|-------|-----------|
| `hash` | `String` | Path | Hash do link de convite |

#### Respostas

**200 OK — Entrou na sala com sucesso:**

```json
{
  "success": true,
  "message": "Entrou na sala com sucesso",
  "content": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sala do Gon",
    "masterName": "Gon Freecss",
    "currentPlayers": 2,
    "maxPlayers": 10,
    "createdAt": "2026-03-24T12:00:00"
  }
}
```

**404 Not Found — Convite invalido ou expirado:**

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "Convite invalido ou expirado"
}
```

**409 Conflict — Sala cheia ou jogador ja esta na sala:**

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "A sala esta cheia"
}
```

**403 Forbidden — Jogador banido da sala** (`BUSINESS_ERROR`, "Voce foi banido desta sala"): `joinRoom` checa `room_bans` antes de adicionar, bloqueando a entrada mesmo com um convite valido.

---

## Invite System (Sistema de Convite Seguro)

O link de convite e gerado sob demanda pelo Mestre via Redis:
- UUID criptograficamente seguro (`UUID.randomUUID()`)
- Armazenado no Redis como Hash com TTL de 8 horas: chave `invite:{roomId}`
- Indice reverso com TTL de 8 horas: chave `invite:hash:{hash}` -> `roomId`
- Campos do Hash: `inviteHash`, `masterId`, `createdAt`
- Se ja existir no Redis, retorna o hash existente
- Se expirou, gera novo hash automaticamente
- URL formato: `{INVITE_BASE_URL}{invite_hash}` — a base e configuravel via variavel `INVITE_BASE_URL` no `.env` (padrao: `https://api.rpg.com/rooms/join/`)

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

-- V4__Remove_Privacy_And_Invite_Code.sql
ALTER TABLE rooms DROP COLUMN IF EXISTS is_private;
ALTER TABLE rooms DROP COLUMN IF EXISTS invite_code;
ALTER TABLE rooms DROP COLUMN IF EXISTS invite_hash;
```

### Schema final da tabela `rooms`

```sql
rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    master_id BIGINT NOT NULL REFERENCES users(id),
    current_players INTEGER NOT NULL DEFAULT 1,
    max_players INTEGER NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
)
```

## Migracoes de Banco de Dados (completas)

```sql
-- V5__Create_Room_Players_Table.sql
CREATE TABLE room_players (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_room_players UNIQUE (room_id, user_id)
);

-- V6__Create_Room_Bans_Table.sql
CREATE TABLE room_bans (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    banned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_room_bans UNIQUE (room_id, user_id)
);

-- V8__Add_Room_Description_And_Cover.sql
ALTER TABLE rooms ADD COLUMN description VARCHAR(500);
ALTER TABLE rooms ADD COLUMN cover_object_key VARCHAR(512);
```

## Swagger

A feature esta documentada no Swagger com:

- **Tag**: "Salas" — Agrupa os endpoints de gerenciamento de salas
- **Seguranca**: Requer `bearerAuth` (JWT) configurado globalmente
- **Endpoints**: `POST /rooms`, `GET /rooms/{id}/invite`, `GET /rooms/join/{hash}`
