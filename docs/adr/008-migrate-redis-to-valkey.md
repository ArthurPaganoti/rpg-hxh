# ADR 008 — Migração de Redis para Valkey

## Status

Aceito (2026-08-01) — decisão registrada; implementação (docker-compose, .env, docs) pendente.

## Contexto

O projeto usa Redis para três responsabilidades: sessões JWT (`RedisSessionService`), rate limiting (`RateLimitFilter`), e convites de sala com TTL (`RedisInviteService`) — além do pub/sub planejado para o SSE (ADR 005). Hoje roda `redis:7-alpine` via docker-compose.

Dois motivos levam à migração para Valkey:

1. **Licença.** Em março de 2024 o Redis abandonou a licença BSD e passou para SSPL/RSALv2 (source-available, não mais open source puro). O **Valkey** é o fork mantido pela Linux Foundation sob **licença BSD**, criado em resposta a essa mudança, com respaldo de AWS, Google Cloud e Oracle. Para um projeto open source, manter a stack 100% open source importa.

2. **Compatibilidade drop-in.** O Valkey nasceu do Redis 7.2.4 e mantém compatibilidade total de protocolo (wire protocol) e comandos. Isso torna a migração de baixíssimo risco.

## Decisão

Migrar de `redis:7-alpine` para **`valkey/valkey:8-alpine`**.

Escolha da versão por critério de segurança:
- **Linha 8.x** — linha ativa do Valkey, recebe correções de CVE via backport; a 7.2 será descontinuada antes.
- **Variante `alpine`** — superfície de ataque mínima (menos pacotes no container, menos CVEs).
- Pinar a linha major (`8`) e repin para minor específica conforme releases; nunca usar `latest`.

## O que muda

**docker-compose.yml (único arquivo de infra):**
- `image: redis:7-alpine` → `image: valkey/valkey:8-alpine`
- `container_name: rpg-hxh-redis` → `rpg-hxh-valkey`
- `command: redis-server --appendonly yes` → `valkey-server --appendonly yes --requirepass ${VALKEY_PASSWORD}`
- healthcheck `redis-cli ping` → `valkey-cli ping` (com `-a ${VALKEY_PASSWORD}` se autenticado)
- volume `redis_data` → `valkey_data`

**Variáveis de ambiente (`.env`, `.env.example`):**
- `REDIS_HOST` → `VALKEY_HOST`
- `REDIS_PORT` → `VALKEY_PORT`
- `REDIS_DATABASE` → `VALKEY_DATABASE`
- **Novo:** `VALKEY_PASSWORD` — ver seção Segurança.

**application.yaml:**
- As chaves `spring.data.redis.*` **permanecem** (é o namespace do Spring Data Redis, independente do servidor). Apenas os valores passam a referenciar as novas variáveis: `host: ${VALKEY_HOST}`, `port: ${VALKEY_PORT}`, `database: ${VALKEY_DATABASE}`, e nova `password: ${VALKEY_PASSWORD}`.

## O que NÃO muda

- **Código Java: zero alterações.** `StringRedisTemplate`, `RedisSessionService`, `RedisInviteService`, `RateLimitFilter`, `LoginService`, `RoomService` ficam intactos. Os nomes com "Redis" referem-se ao client Spring Data Redis, não ao servidor — e o client continua sendo o mesmo (não existe "Spring Data Valkey"; o Valkey é acessado pelo próprio Spring Data Redis via Lettuce).
- **build.gradle: zero alterações.** Os starters `spring-boot-starter-data-redis`, `-reactive` e `-session-data-redis` são abstrações do Spring, não a imagem do servidor. Permanecem.

## Segurança

A migração aproveita para corrigir uma lacuna existente: o Redis atual roda **sem autenticação** (sem `requirepass`, sem password no Spring). O Valkey passa a exigir senha via `--requirepass ${VALKEY_PASSWORD}`, e o Spring autentica via `spring.data.redis.password`. Em produção, a senha vem do gerenciador de segredos; em dev, do `.env` (fora do git).

## Consequências

- Stack 100% open source (BSD), sem risco de licença source-available.
- Ganho de segurança: autenticação no cache/sessão passa a existir.
- Migração reversível: se necessário, reverter é trocar a imagem de volta e as variáveis — nenhum dado de código muda.
- Dados existentes no volume Redis não são migrados automaticamente (formato AOF/RDB é compatível, mas o volume é novo). Como todo dado no Redis é efêmero (sessões, rate limit, convites com TTL), não há perda relevante — sessões ativas expiram e usuários refazem login.
- `valkey-cli` substitui `redis-cli` nos comandos operacionais; ferramentas GUI (RedisInsight, Another Redis Desktop Manager) continuam funcionando por compatibilidade de protocolo.