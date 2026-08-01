# ADR 007 — Eventos do jogo no PostgreSQL e estrategia de armazenamento (Postgres / Redis / MinIO / Loki)

## Status

Aceito (2026-08-01) — decisao registrada; implementacao (migracao V6, endpoint de rolagem, export) ainda nao iniciada.

## Contexto

Com a jogabilidade se aproximando (turnos, rolagem de dados, ataques — visao confirmada no ADR 005), foi avaliado onde armazenar os eventos do jogo, como permitir download/export posterior e onde entrariam MinIO e a pilha de observabilidade (Prometheus/Loki/Grafana). A proposta inicial considerava MinIO para rolagens/ataques e "logs por sala" via Loki — este ADR corrige o mapeamento ferramenta→problema.

A regra que orienta todas as decisoes abaixo:

> **Dado estruturado que se consulta → PostgreSQL. Estado efemero com TTL → Redis. Arquivo que se serve/baixa inteiro → MinIO. Log operacional → Loki.**

## Decisao

### 1. Eventos do jogo: PostgreSQL (`room_events`)

Rolagens, ataques, turnos e mensagens sao **dados de dominio estruturados e consultaveis** ("rolagens da sessao de ontem", "historico do personagem X") — nao arquivos e nao logs. Ficam numa tabela `room_events` via Flyway (V6):

- `id`, `room_id` (FK), `session_id`, `author_id` (FK user), `type` (`DICE_ROLL`, `ATTACK`, `TURN`, `MESSAGE`...), `payload` (JSONB), `created_at`
- Indice `(room_id, created_at DESC)` — busca "ultimos N eventos da sala" em milissegundos, mesmo com milhoes de linhas.

**Padrao de leitura (por que nao e pesado):** 1 query indexada na abertura da tela da sala (historico recente) + eventos novos chegando por **push via SSE/Redis pub/sub (ADR 005)** — sem polling. Escrita: 1 INSERT + 1 PUBLISH por evento. Dimensionamento: sessao intensa ≈ 200–500 eventos; mil mesas ativas por um ano ≈ ~20M linhas — tabela pequena para Postgres indexado.

**Retencao:** o historico E produto (recap de sessao, export de campanha) — **nao ha expiracao**. Ciclo de vida:

| Fase | Onde | Quando |
|------|------|--------|
| Quente | Postgres, tabela viva indexada | Sempre — tela, export e recap leem daqui |
| Fria (otimizacao futura) | Particionamento por mes + arquivamento de sessoes antigas em MinIO (JSON) com remocao do banco | Somente se/quando o volume incomodar (milhares de mesas por anos) |

### 2. Download/export: duas fases

- **Fase 1 (suficiente por muito tempo):** `GET /rooms/{id}/events/export?format=csv|json` — consulta o Postgres e responde em **streaming** (`Content-Disposition: attachment`), sem armazenamento intermediario. Protegido por `findRoomAsMember`.
- **Fase 2 (exports pesados/ricos):** `POST /rooms/{id}/exports` gera o artefato em background (ex.: PDF de recap), salva no **MinIO** (bucket `exports/` com TTL) e devolve **URL pre-assinada**. O arquivo e derivado e descartavel; o Postgres continua dono dos dados.

### 3. MinIO: arquivos, nunca dados de dominio

Casos de uso (em ordem provavel de chegada):

1. **Avatares de personagem** e foto de perfil (primeiro upload do projeto, junto do sistema de personagens);
2. **Imagens/anexos de ficha, tokens e mapas da sala**;
3. **Artefatos de export** (fase 2 acima);
4. **Arquivo frio** de sessoes antigas (ciclo de vida acima).

Em todos: o MinIO guarda **os bytes**; o Postgres guarda **os metadados** (chave do objeto, dono, sala, data). Fichas de personagem tem as duas partes: atributos/HP/Nen/inventario no Postgres (consultados e validados pelo jogo); retrato/anexos no MinIO.

### 4. Observabilidade: Loki para logs operacionais — nunca para historico de jogo

- **Historico de jogo** (jogador ve) = `room_events` no Postgres — log operacional expira, dado de negocio nao.
- **Logs operacionais** (debug: "o que a API fez quando a sala X travou") = **Loki + Grafana**, com `roomId`/`sessionId` no MDC para filtrar por sala. Complementa o Prometheus/Grafana ja aprovado nas preparacoes do ADR 006; Loki entra na mesma fase de observabilidade (profile `observability` do docker-compose).

## Mapa final de armazenamento

| Sistema | Papel no rpg-hxh |
|---------|------------------|
| **PostgreSQL** | Memoria do jogo: usuarios, salas, membros, eventos (`room_events`), metadados de arquivos |
| **Redis** | Estado efemero com TTL: sessoes JWT, rate limiting, convites, pub/sub do SSE |
| **MinIO** | Arquivos e derivados: avatares, anexos de ficha, mapas, exports (URL pre-assinada, TTL em `exports/`) |
| **Loki/Prometheus/Grafana** | Observabilidade operacional: logs (MDC roomId/sessionId), metricas, dashboards |

## Consequencias

- A implementacao da jogabilidade comeca por: **migracao V6 (`room_events`) + `POST /rooms/{id}/rolls` (dado rolado no servidor — anti-cheat) + fan-out SSE (ADR 005)** — a primeira rolagem persistida e transmitida ao vivo.
- MinIO **nao** entra na infraestrutura agora — sobe no docker-compose quando o primeiro caso de arquivo (avatares) chegar.
- O export fase 1 nasce barato (streaming direto do banco); a fase 2 tem desenho pronto quando for necessaria.
- Regra de bolso para decisoes futuras: se da para fazer `WHERE` em cima, e Postgres; se abre em visualizador de imagem/PDF, e MinIO.
