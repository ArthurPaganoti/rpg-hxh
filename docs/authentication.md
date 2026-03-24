# Autenticacao (Login e JWT)

Este documento descreve o sistema de Login, geracao de tokens JWT e gerenciamento de sessoes da API RPG HxH.

## Visao Geral

O fluxo de autenticacao permite que usuarios registrados facam login com email e senha. Ao autenticar com sucesso, o sistema gera um token JWT, salva uma sessao no Redis e retorna o token no header `Authorization` da resposta. Rotas protegidas exigem esse token para acesso.

## Endpoint

```
POST /login
```

**Autenticacao**: Publica (nenhum token necessario).
**Rate Limit**: 5 requisicoes/minuto por IP (via `RateLimitFilter`, usando Redis).

## Requisicao

### Headers

| Header | Valor |
|--------|-------|
| `Content-Type` | `application/json` |

### Body (`LoginDTO`)

| Campo | Tipo | Obrigatorio | Regras de Validacao |
|-------|------|-------------|---------------------|
| `email` | `String` | Sim | Nao pode ser vazio, formato de email valido |
| `senha` | `String` | Sim | Nao pode ser vazio |

### Exemplo

```json
{
  "email": "gon@hunterxhunter.com",
  "senha": "Jajanken@1"
}
```

## Respostas

### 200 OK — Login Bem-sucedido

O token JWT e retornado no header `Authorization` da resposta.

**Headers da resposta:**

| Header | Valor |
|--------|-------|
| `Authorization` | `Bearer eyJhbGciOiJIUzI1NiJ9...` |

**Body:**

```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 400 Bad Request — Erro de Validacao

Retornado quando os campos do `LoginDTO` falham na validacao (ex.: email vazio, formato invalido).

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Erro de validação dos campos",
  "content": {
    "email": "O email é obrigatório",
    "senha": "A senha é obrigatória"
  },
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 401 Unauthorized — Credenciais Invalidas

Retornado quando o email nao esta cadastrado ou a senha nao confere.

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "Credenciais inválidas",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 429 Too Many Requests — Rate Limit Excedido

Retornado quando o IP ultrapassou 5 requisicoes em 1 minuto.

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Muitas tentativas. Tente novamente em 1 minuto.",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

## Arquitetura

A feature segue o padrao de **Functional Slice**:

```
login/
  controller/LoginController.java          -- Endpoint REST
  service/LoginService.java                -- Logica de autenticacao
  service/JwtService.java                  -- Geracao e validacao de tokens JWT
  service/RedisSessionService.java         -- Persistencia de sessao no Redis
  dto/LoginDTO.java                        -- DTO de requisicao com validacoes
  filter/JwtAuthenticationFilter.java      -- Filtro de autenticacao JWT para rotas protegidas
  filter/RateLimitFilter.java              -- Filtro de rate limiting por IP
```

### Fluxo de Login

```
Cliente
  |
  v
RateLimitFilter (verifica limite de requisicoes por IP)
  |
  v
LoginController (@Valid @RequestBody LoginDTO)
  |  -- Jakarta Bean Validation executa aqui
  |  -- Falhas -> GlobalExceptionHandler -> VALIDATION_ERROR
  v
LoginService.authenticate(dto)
  |  1. Busca usuario por email         -> InvalidCredentialsException se nao encontrado
  |  2. Compara senha com BCrypt        -> InvalidCredentialsException se nao confere
  |  3. Gera token JWT via JwtService
  |  4. Salva sessao no Redis via RedisSessionService
  v
LoginController retorna token no header Authorization + ResponseDTO.success()
```

### Fluxo de Autenticacao JWT (Rotas Protegidas)

```
Cliente (com header Authorization: Bearer <token>)
  |
  v
JwtAuthenticationFilter (OncePerRequestFilter)
  |  1. Extrai o header Authorization
  |  2. Remove o prefixo "Bearer "
  |  3. Valida o token via JwtService.isTokenValid()
  |  4. Extrai o email do token via JwtService.extractEmail()
  |  5. Define autenticacao no SecurityContextHolder
  v
Requisicao segue para o controller da rota protegida
```

## JWT (JSON Web Token)

### Configuracao

As configuracoes ficam no `application.yaml`:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 28800000   # 8 horas em milissegundos
```

### Estrutura do Token

O `JwtService` gera tokens com a seguinte estrutura:

| Claim | Descricao |
|-------|-----------|
| `sub` (subject) | Email do usuario |
| `iat` (issued at) | Data/hora de emissao |
| `exp` (expiration) | Data/hora de expiracao |

O token e assinado com HMAC usando a chave secreta (`JWT_SECRET`).

### Operacoes do `JwtService`

| Metodo | Descricao |
|--------|-----------|
| `generateToken(email)` | Gera um novo token JWT com o email como subject |
| `extractEmail(token)` | Extrai o email (subject) do token |
| `isTokenValid(token)` | Verifica se o token e valido e nao expirou |
| `getIssuedAt(token)` | Retorna o `Instant` de emissao do token |
| `getExpiration(token)` | Retorna o `Instant` de expiracao do token |

## Sessao Redis

O `RedisSessionService` armazena sessoes de usuario no Redis apos o login bem-sucedido.

### Estrutura da Sessao

Cada sessao e armazenada como um hash Redis com a chave `auth:{email}`:

| Campo | Descricao | Exemplo |
|-------|-----------|---------|
| `token` | Token JWT completo | `eyJhbGciOiJIUzI1NiJ9...` |
| `createdAt` | Data/hora de criacao | `2026-03-24T12:00:00Z` |
| `expiresAt` | Data/hora de expiracao | `2026-03-25T04:00:00Z` |

O TTL da chave Redis e definido como a diferenca entre o momento atual e a expiracao do token, garantindo que a sessao seja automaticamente removida quando o token expirar.

### Operacoes do `RedisSessionService`

| Metodo | Descricao |
|--------|-----------|
| `saveSession(email, token, createdAt, expiresAt)` | Salva a sessao no Redis com TTL automatico |
| `removeSession(email)` | Remove a sessao do Redis (para logout futuro) |

## Rate Limiting

O `RateLimitFilter` protege os endpoints `POST /login` e `POST /register` contra abuso.

### Funcionamento

1. Extrai o IP do cliente (suporte a `X-Forwarded-For` para proxies)
2. Cria uma chave Redis no formato `rate_limit:{rota}:{ip}`
3. Incrementa o contador atomicamente
4. Na primeira requisicao, define um TTL de 1 minuto na chave
5. Se o contador exceder 5, retorna `429 Too Many Requests`

### Configuracao

| Parametro | Valor |
|-----------|-------|
| Maximo de requisicoes | 5 por janela |
| Janela de tempo | 1 minuto |
| Rotas protegidas | `POST /login`, `POST /register` |

## Seguranca

### Configuracao do Spring Security (`SecurityConfig`)

| Configuracao | Valor | Descricao |
|--------------|-------|-----------|
| CSRF | Desabilitado | API REST stateless, autenticacao via JWT |
| Sessao | `STATELESS` | Sem sessoes HTTP do lado do servidor |
| Rotas publicas | `/register`, `/login`, `/swagger-ui/**`, `/v3/api-docs/**` | Acessiveis sem token |
| Demais rotas | `authenticated()` | Exigem token JWT valido |

### Cadeia de Filtros

A ordem dos filtros no Spring Security:

```
RateLimitFilter -> JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter (padrao Spring)
```

1. **RateLimitFilter** — Executa primeiro, bloqueia requisicoes excessivas antes de qualquer processamento
2. **JwtAuthenticationFilter** — Valida o token JWT e define o contexto de seguranca

## Tratamento de Excecoes

| Excecao | Status HTTP | Codigo de Erro | Gatilho |
|---------|-------------|----------------|---------|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` | Falhas de Bean Validation |
| `InvalidCredentialsException` | 401 | `BUSINESS_ERROR` | Email nao encontrado ou senha incorreta |
| `Exception` (fallback) | 500 | `INTERNAL_ERROR` | Erros inesperados |

## Swagger

A feature esta documentada no Swagger com:

- **Tag**: "Autenticacao" — Agrupa os endpoints de login
- **Esquema de seguranca**: `bearerAuth` (JWT) configurado globalmente no `SwaggerConfig`, permitindo testar rotas protegidas diretamente pela interface do Swagger UI
