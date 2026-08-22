# Fichas de Personagem

## Visao Geral

Dentro de cada sala existe a area de **Fichas**: os jogadores sobem o arquivo da sua ficha (PDF, DOC, DOCX ou ODT, ate 50MB). Cada jogador tem **uma unica ficha por sala** — um novo envio substitui a anterior. Um jogador so tem acesso a **sua** ficha; o **Mestre** visualiza e baixa a ficha de **todos**.

Segue o ADR 007: os **bytes** ficam no **MinIO**; os **metadados** (dono, sala, nome, tipo, tamanho, chave do objeto) no **PostgreSQL** (`room_sheets`). O arquivo trafega **pelo backend** — o MinIO nao e exposto publicamente e a autorizacao usa o JWT.

## Endpoints

Todos exigem Bearer JWT. Base: `/rooms/{id}`.

### `POST /rooms/{id}/sheet` — Enviar/substituir a propria ficha
`multipart/form-data` com o campo `file`. **Qualquer membro** da sala (inclusive o Mestre). Substitui a ficha anterior do mesmo jogador (mesma `object_key`, sobrescreve no MinIO e atualiza a linha).

| Codigo | Situacao |
|--------|----------|
| 200 | `Ficha enviada com sucesso` |
| 403 | Nao e membro da sala |
| 404 | Sala nao encontrada |
| 413 | Arquivo maior que 50MB |
| 415 | Tipo nao permitido (aceita PDF, DOC, DOCX, ODT) |

### `GET /rooms/{id}/sheets` — Listar fichas
O **Mestre** recebe as fichas de todos; um **jogador** recebe apenas a propria (lista de 0 ou 1). Cada item: `userId`, `ownerName`, `fileName`, `contentType`, `sizeBytes`, `uploadedAt`, `isMine`. **403** para nao-membro, **404** se a sala nao existir.

### `GET /rooms/{id}/sheets/{userId}/download` — Baixar ficha
Retorna o arquivo (`Content-Disposition: attachment`). O **dono** baixa a sua; o **Mestre** baixa a de qualquer jogador. Um membro pedindo a ficha de outro recebe **403**; nao-membro **403**; ficha/sala inexistente **404**.

### `DELETE /rooms/{id}/sheet` — Remover a propria ficha
Remove o objeto no MinIO e a linha. **Qualquer membro**, apenas sobre a **propria** ficha. **404** se nao houver ficha.

## Regras de Negocio

| Regra | Descricao |
|-------|-----------|
| Uma por jogador | `UNIQUE(room_id, user_id)`; novo upload substitui (mesma `object_key` `rooms/{roomId}/sheets/{userId}`) |
| Visibilidade | Jogador ve so a propria ficha; Mestre ve todas |
| Download | Dono baixa a sua; Mestre baixa a de qualquer um |
| Tipos aceitos | PDF, DOC, DOCX, ODT (por `content-type`) |
| Tamanho | Ate 50MB (`spring.servlet.multipart.max-file-size`) |
| Cleanup | `deleteRoom` remove os objetos no MinIO e as linhas de `room_sheets` |

## Armazenamento (MinIO)

- Bucket unico configuravel (`MINIO_BUCKET`), criado no startup por `MinioConfig`
- Chave do objeto: `rooms/{roomId}/sheets/{userId}` — estavel por jogador, entao o upload sobrescreve
- Acesso encapsulado em `shared/storage/FileStorageService` (`upload`/`download`/`delete`)

## Migracao

```sql
-- V7__Create_Room_Sheets_Table.sql
CREATE TABLE room_sheets (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    object_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_room_sheets UNIQUE (room_id, user_id)
);
```

## Configuracao (.env)

```
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=...
MINIO_SECRET_KEY=...
MINIO_BUCKET=rpg-hxh
```

`docker compose up -d minio` sobe o MinIO (API em 9000, console em 9001).

## Swagger
- Tag: "Fichas"
- Requer `bearerAuth`