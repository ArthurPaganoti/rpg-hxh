# Configuracao Inicial

Este documento descreve a infraestrutura e a configuracao de ambiente necessarias para executar a aplicacao RPG HxH.

## Pre-requisitos

- **Java 21** (via SDKMAN, Homebrew ou instalacao manual)
- **PostgreSQL 15+** em execucao e acessivel
- **Redis 7+** em execucao e acessivel
- **Gradle 8+** (ou use o wrapper incluso `./gradlew`)

## Variaveis de Ambiente

A aplicacao utiliza [dotenv-java](https://github.com/cdimascio/dotenv-java) para carregar variaveis de ambiente a partir de um arquivo `.env` na raiz do projeto. O carregamento e feito pelo `DotenvConfig`, um `ApplicationContextInitializer` customizado que injeta as entradas do `.env` como uma property source do Spring com prioridade maxima.

Copie o `.env.example` para `.env` e preencha os valores:

```bash
cp .env.example .env
```

### Variaveis Obrigatorias

| Variavel | Descricao | Exemplo |
|----------|-----------|---------|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco de dados | `rpg_hxh` |
| `DB_USERNAME` | Usuario do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `secret` |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `REDIS_DATABASE` | Indice do banco Redis | `0` |
| `JWT_SECRET` | Chave secreta para assinatura JWT (HMAC) | Uma string longa e aleatoria |

### Configuracao JWT

As configuracoes de JWT estao definidas no `application.yaml`:

- **`jwt.secret`**: Carregado da variavel `JWT_SECRET`. Utilizado pelo `JwtService` para assinar e verificar tokens.
- **`jwt.expiration`**: Tempo de vida do token em milissegundos. Padrao: `28800000` (8 horas).

## Banco de Dados

### Conexao

O datasource e configurado no `application.yaml` utilizando as variaveis de ambiente acima:

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### Migracoes (Flyway)

As alteracoes de schema sao gerenciadas exclusivamente pelo Flyway. Os arquivos de migracao ficam em:

```
src/main/resources/db/migration/
```

O Flyway esta configurado para executar na inicializacao com `baseline-on-migrate: true`. Migracoes atuais:

| Arquivo | Descricao |
|---------|-----------|
| `V1_Create_Users_Table.sql` | Cria a tabela `users` com `id`, `name`, `email`, `senha`, `created_at`, `updated_at` |

> **Regra**: Nunca dependa de `ddl-auto` para alteracoes de schema em producao. Todo DDL deve ser feito por arquivos de migracao Flyway.

## Redis

O Redis e utilizado para dois propositos:

1. **Rate Limiting** — O `RateLimitFilter` rastreia a contagem de requisicoes por IP usando chaves Redis com TTL. Limita `POST /register` e `POST /login` a 5 requisicoes/minuto por IP.
2. **Cache** — O Spring Cache esta configurado com `spring.cache.type=redis`.

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      database: ${REDIS_DATABASE}
```

## Executando a Aplicacao

```bash
# Build do projeto
./gradlew build

# Executar a aplicacao
./gradlew bootRun

# Executar todos os testes
./gradlew test

# Build limpo
./gradlew clean build
```

A aplicacao inicia na porta padrao do Spring Boot (`8080`). A documentacao da API esta disponivel em:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Testes

Os testes utilizam um banco de dados **H2** em memoria e nao exigem que PostgreSQL ou Redis estejam em execucao. O Flyway e desabilitado no perfil de testes. A dependencia do H2 esta inclusa como `testRuntimeOnly` no `build.gradle`.

## Observabilidade (Planejado)

Existem placeholders para ferramentas de observabilidade futuras:

- **Prometheus** — Coleta de metricas
- **Grafana** — Visualizacao de metricas e dashboards
- **OpenTelemetry (OTEL)** — Rastreamento distribuido

Estas ferramentas ainda nao estao configuradas. A documentacao sera atualizada quando implementadas.
