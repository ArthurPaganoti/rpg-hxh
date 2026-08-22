# RPG HxH — Backend API

> Ajudando a comunidade de RPG a jogar de forma mais dinâmica, divertida e simples, facilitando a vida dos jogadores.

Uma API backend open source para RPG de mesa, construída com temática **Hunter x Hunter** e baseada no sistema **Hunter Legacy**. A arquitetura é modular, permitindo fácil adaptação para outros sistemas de RPG.

![KILLUA_IMAGEM_README.png](src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png)
---

## 🧰 Tech Stack

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| **Java** | 21 | Linguagem principal |
| **Spring Boot** | 4.0.3 | Framework backend |
| **PostgreSQL** | 15+ | Banco de dados relacional |
| **Flyway** | — | Migrações de schema |
| **Valkey** | 8+ | Cache, sessões JWT e rate limiting (fork BSD do Redis, protocolo compatível) |
| **MinIO** | — | Armazenamento de objetos (arquivos de ficha; avatares/exports futuros — ADR 007) |
| **Spring Security** | — | Autenticação e autorização |
| **JWT (jjwt)** | 0.12.6 | Tokens de autenticação |
| **Springdoc OpenAPI** | 3.0.2 | Documentação Swagger |
| **Lombok** | — | Redução de boilerplate |
| **H2** | — | Banco em memória para testes |

---

## 🏗️ Arquitetura — Functional Slices

O projeto é organizado em **Functional Slices** verticais — cada feature é um pacote direto, sem pastas intermediárias como `features/` ou `modules/`.

```
com.rpg.rpghxh/
├── register/          # 🔹 Slice: Registro de usuário
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── mapper/
├── login/             # 🔹 Slice: Login e autenticação JWT
│   ├── controller/
│   ├── service/       # LoginService, JwtService, RedisSessionService
│   ├── dto/
│   └── filter/        # JwtAuthenticationFilter, RateLimitFilter
├── rooms/             # 🔹 Slice: Criação e gerenciamento de salas
│   ├── controller/
│   ├── service/
│   └── dto/
├── entities/
│   ├── user/          # 🔸 Domínio: Usuário
│   │   ├── entity/
│   │   └── repository/
│   └── room/          # 🔸 Domínio: Sala
│       ├── entity/
│       └── repository/
├── shared/            # 🔹 Componentes globais reutilizáveis
│   ├── dto/           # ResponseDTO
│   ├── validation/    # @ValidPassword, @PasswordMatch
│   └── exceptions/    # BusinessException, GlobalExceptionHandler
├── config/            # ⚙️ SecurityConfig, SwaggerConfig, DotenvConfig
└── utils/             # 🔧 Classes utilitárias stateless
```

### Princípios

- **Slices independentes** — Um service em `register/` não depende de um service em `login/`
- **Controllers** lidam apenas com HTTP
- **Services** contêm regras de negócio
- **Repositories** lidam apenas com persistência
- **Respostas padronizadas** — Todos os endpoints retornam `ResponseDTO<T>`

---

## 📡 API — Endpoints

### `POST /register` — Registro de Usuário

Cria um novo usuário com validação de senha forte e confirmação.

**Request:**

```json
{
  "name": "Gon Freecss",
  "email": "gon@hunterxhunter.com",
  "senha": "Jajanken@1",
  "confirmacaoSenha": "Jajanken@1"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Usuário registrado com sucesso",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

**Validações:**
- `name` — Obrigatório, entre 3 e 100 caracteres, único
- `email` — Obrigatório, formato válido, único
- `senha` — Mínimo 8 caracteres, letra maiúscula, minúscula e caractere especial (`@ValidPassword`)
- `confirmacaoSenha` — Deve ser igual ao campo `senha` (`@PasswordMatch`)

> Documentação completa: [`docs/register.md`](docs/register.md)

---

### `POST /login` — Autenticação JWT

Autentica o usuário e retorna um token JWT no header `Authorization`.

**Request:**

```json
{
  "email": "gon@hunterxhunter.com",
  "senha": "Jajanken@1"
}
```

**Response (200):**

Header: `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...`

```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

**Segurança:**
- Token JWT com expiração de 8 horas
- Sessão armazenada no Valkey com TTL automático
- Rate limiting: 5 requisições/minuto por IP

> Documentação completa: [`docs/authentication.md`](docs/authentication.md)

---

### `POST /rooms` — Criação de Sala

Cria uma nova sala de RPG. O usuário autenticado se torna o Mestre da sala. Requer token JWT. Todas as salas são privadas — o acesso é feito exclusivamente via link de convite.

**Request:**

```json
{
  "name": "Sala do Gon"
}
```

**Response (201):**

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

**Regras:**
- `name` — Obrigatório, entre 3 e 100 caracteres
- `maxPlayers` — Opcional, entre 2 e 10 (padrão 10)
- O usuário autenticado se torna o Mestre (`master_id`)
- `currentPlayers` inicia com 1 (o Mestre)

---

### `GET /rooms/{id}/invite` — Secure Invite System

Gera ou recupera o link de convite da sala. **Apenas o Mestre** da sala pode acessar. Requer token JWT. O link é armazenado no Redis com validade de **8 horas**.

**Response (200):**

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

**Regras:**
- Apenas o Mestre da sala pode gerar o link (403 para outros usuários)
- O `invite_hash` é um UUID criptograficamente seguro armazenado no Redis
- Validade de 8 horas — após expirar, um novo link é gerado automaticamente

> Documentação completa: [`docs/rooms.md`](docs/rooms.md)

---

### `DELETE /rooms/{id}/invite` — Revogar Convite

Invalida o link de convite ativo antes do fim das 8 horas (útil quando o link vaza). **Apenas o Mestre** pode revogar (403 para outros). Operação idempotente; um novo `GET /rooms/{id}/invite` gera um hash diferente.

**Response (200):**

```json
{
  "success": true,
  "message": "Convite revogado com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

---

### `GET /rooms` — Listar Minhas Salas

Lista todas as salas em que o usuário autenticado participa (como Mestre ou jogador), ordenadas da mais recente para a mais antiga. Retorna `content: []` quando o usuário não tem salas.

---

### `GET /rooms/{id}` — Detalhes da Sala

Retorna os dados de uma sala específica (nome, mestre, jogadores, `isMaster`). **Qualquer membro** da sala pode acessar; não-membros recebem 403. Permite abrir a tela da sala direto por URL, sem depender da listagem.

---

### `GET /rooms/{id}/members` — Listar Membros da Sala

Lista os membros da sala (id, nome, data de entrada e flag `isMaster`), ordenados pela entrada. **Qualquer membro** da sala pode acessar; não-membros recebem 403.

---

### `PATCH /rooms/{id}` — Atualizar Nome da Sala

Atualiza o nome e, opcionalmente, o máximo de jogadores da sala. **Apenas o Mestre** pode atualizar (403 para outros usuários). O máximo não pode ficar menor que o número atual de jogadores (409).

**Request:**

```json
{
  "name": "Sala do Gon Renovada"
}
```

---

### `DELETE /rooms/{id}` — Deletar Sala

Deleta a sala permanentemente. **Apenas o Mestre** pode deletar (403 para outros usuários). Remove o convite ativo no Redis, todos os jogadores e a sala na mesma transação.

**Response (200):**

```json
{
  "success": true,
  "message": "Sala deletada com sucesso",
  "timestamp": "2026-07-19T15:00:00Z"
}
```

---

### `POST /rooms/{id}/bans/{userId}` · `DELETE /rooms/{id}/bans/{userId}` · `GET /rooms/{id}/bans` — Banimento

**Apenas o Mestre.** `POST` bane um jogador: remove-o da sala (se membro) e impede que reentre **mesmo com um link de convite válido** (`joinRoom` checa `room_bans` → 403). Idempotente; o Mestre não pode ser banido (409). `DELETE` desbane (404 se não estiver banido). `GET` lista os banidos. Ao deletar a sala, os banimentos são removidos junto.

**Response (200):**

```json
{
  "success": true,
  "message": "Jogador banido da sala com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

---

### `POST /rooms/{id}/leave` — Sair da Sala

Remove o jogador autenticado da sala e recalcula o número de jogadores. **Qualquer membro** pode sair, **exceto o Mestre** (409 — o Mestre sai deletando a sala). Não-membros recebem 403.

**Response (200):**

```json
{
  "success": true,
  "message": "Voce saiu da sala com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

---

### `DELETE /rooms/{id}/members/{userId}` — Remover Membro

Remove um jogador da sala e recalcula o número de jogadores. **Apenas o Mestre** pode remover (403 para outros). O Mestre não pode ser removido (409). O alvo precisa ser membro da sala (404).

---

### `POST /rooms/{id}/sheet` · `GET /rooms/{id}/sheets` · `GET /rooms/{id}/sheets/{userId}/download` · `DELETE /rooms/{id}/sheet` — Fichas

Área de fichas da sala. Cada jogador sobe **uma** ficha (PDF/DOC/DOCX/ODT, até 10MB) via `multipart/form-data`; um novo envio **substitui** a anterior. Um jogador só vê/baixa a **própria** ficha; o **Mestre** vê e baixa a de **todos**. Os bytes ficam no **MinIO** (ADR 007), os metadados no PostgreSQL, e o download passa pelo backend (MinIO privado, auth por JWT). Tipos inválidos → 415; acima de 10MB → 413.

> Documentação completa: [`docs/sheets.md`](docs/sheets.md)

**Response (200):**

```json
{
  "success": true,
  "message": "Jogador removido da sala com sucesso",
  "timestamp": "2026-08-22T15:00:00Z"
}
```

---

### Formato Padrão de Resposta (`ResponseDTO<T>`)

Todos os endpoints seguem o mesmo formato:

```json
{
  "success": true,
  "code": null,
  "message": "Mensagem descritiva",
  "content": null,
  "timestamp": "2026-03-24T12:00:00Z"
}
```

Campos `null` são omitidos automaticamente do JSON.

| Código | Descrição |
|--------|-----------|
| `VALIDATION_ERROR` | Falha de validação nos campos da requisição |
| `BUSINESS_ERROR` | Violação de regra de negócio (email duplicado, credenciais inválidas, etc.) |
| `INTERNAL_ERROR` | Erro interno inesperado |

---

## 🚀 Getting Started

### Pré-requisitos

- **Java 21**
- **PostgreSQL 15+**, **Valkey 8+** e **MinIO** — ou **Docker + Docker Compose** para subir os três automaticamente

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/rpg-hxh.git
cd rpg-hxh
```

### 2. Configure as variáveis de ambiente

Copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

```env
# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=rpg_hxh
DB_USERNAME=postgres
DB_PASSWORD=sua_senha

# Valkey
VALKEY_HOST=localhost
VALKEY_PORT=6379
VALKEY_DATABASE=0
VALKEY_PASSWORD=sua_senha_valkey

# MinIO (armazenamento de arquivos de ficha)
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=sua_access_key
MINIO_SECRET_KEY=sua_secret_key
MINIO_BUCKET=rpg-hxh

# JWT
JWT_SECRET=sua_chave_secreta_longa_e_aleatoria

# Invite Links
INVITE_BASE_URL=http://localhost:8080/rooms/join/
```

> O carregamento das variáveis é feito automaticamente pelo `DotenvConfig`, que injeta o `.env` como property source do Spring com prioridade máxima.

### 3. Suba a infraestrutura (PostgreSQL + Valkey + MinIO) com Docker

```bash
docker compose up -d
```

O `docker-compose.yml` lê as variáveis do `.env` e sobe:

| Serviço | Imagem | Porta |
|---------|--------|-------|
| PostgreSQL | `postgres:15-alpine` | `${DB_PORT}` (padrão 5432) |
| Valkey | `valkey/valkey:8-alpine` (AOF + requirepass) | `${VALKEY_PORT}` (padrão 6379) |
| MinIO | `minio/minio` | `${MINIO_PORT}` API 9000 · console 9001 |

Todos possuem healthcheck e volumes persistentes. O console do MinIO fica em `http://localhost:9001`. Se preferir instalações locais, pule este passo.

### 4. Execute a aplicação

```bash
# Build do projeto
./gradlew build

# Executar a aplicação
./gradlew bootRun

# Executar todos os testes
./gradlew test

# Build limpo
./gradlew clean build
```

### 5. Acesse a documentação

| Recurso | URL |
|---------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

> Detalhes completos de infraestrutura: [`docs/initial_setup.md`](docs/initial_setup.md)

---

## 🔒 Segurança

| Recurso | Implementação |
|---------|---------------|
| Criptografia de senhas | `BCryptPasswordEncoder` |
| Autenticação | JWT (HMAC, 8h de expiração) |
| Sessões | Stateless (Redis para session tracking) |
| Rate Limiting | 5 req/min por IP em `POST /register` e `POST /login` |
| CSRF | Desabilitado (API stateless) |
| Rotas públicas | `/register`, `/login`, Swagger |
| Demais rotas | Exigem token JWT válido |

---

## 📂 Documentação

| Documento | Descrição |
|-----------|-----------|
| [`docs/initial_setup.md`](docs/initial_setup.md) | Configuração de ambiente, variáveis, banco de dados e Redis |
| [`docs/register.md`](docs/register.md) | Detalhes técnicos do fluxo de registro de usuário |
| [`docs/authentication.md`](docs/authentication.md) | Sistema de login, JWT, sessões Redis e rate limiting |
| [`docs/rooms.md`](docs/rooms.md) | Criação de salas, regras de negócio e invite code |
| [`graphify-out/GRAPH_REPORT.md`](graphify-out/GRAPH_REPORT.md) | Grafo de conhecimento do código (nós centrais, comunidades e conexões) — visualização interativa em `graphify-out/graph.html` |

---

## 🤝 Contribuindo

Este é um projeto **100% open source**! Contribuições são muito bem-vindas.

O core é baseado no sistema **Hunter Legacy**, mas a arquitetura de Functional Slices foi desenhada para ser **modular e adaptável** — você pode modificar e adaptar para qualquer outro sistema de RPG.

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona minha feature'`)
4. Push para a branch (`git push origin feature/minha-feature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto é open source. Consulte o arquivo de licença para mais detalhes.

---

<p align="center">
  Feito com ☕ e Nen por jogadores, para jogadores.
</p>
