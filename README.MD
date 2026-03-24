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
| **Redis** | 7+ | Cache, sessões JWT e rate limiting |
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
├── entities/
│   └── user/          # 🔸 Domínio: Usuário
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
- Sessão armazenada no Redis com TTL automático
- Rate limiting: 5 requisições/minuto por IP

> Documentação completa: [`docs/authentication.md`](docs/authentication.md)

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
- **PostgreSQL 15+**
- **Redis 7+**

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

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=0

# JWT
JWT_SECRET=sua_chave_secreta_longa_e_aleatoria
```

> O carregamento das variáveis é feito automaticamente pelo `DotenvConfig`, que injeta o `.env` como property source do Spring com prioridade máxima.

### 3. Execute a aplicação

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

### 4. Acesse a documentação

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
