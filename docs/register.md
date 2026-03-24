# Registro de Usuario

Este documento descreve a feature de Registro de Usuario, o primeiro functional slice da API RPG HxH.

## Visao Geral

O fluxo de registro permite que novos usuarios criem uma conta fornecendo nome, email e senha. O sistema aplica restricoes de unicidade, regras de forca de senha e verificacao de confirmacao de senha antes de persistir o usuario com a senha criptografada.

## Endpoint

```
POST /register
```

**Autenticacao**: Publica (nenhum token necessario).
**Rate Limit**: 5 requisicoes/minuto por IP (via `RateLimitFilter`, usando Redis).

## Requisicao

### Headers

| Header | Valor |
|--------|-------|
| `Content-Type` | `application/json` |

### Body (`RegisterDTO`)

| Campo | Tipo | Obrigatorio | Regras de Validacao |
|-------|------|-------------|---------------------|
| `name` | `String` | Sim | Nao pode ser vazio, entre 3 e 100 caracteres |
| `email` | `String` | Sim | Nao pode ser vazio, formato de email valido |
| `senha` | `String` | Sim | Nao pode ser vazio, regras do `@ValidPassword` (veja abaixo) |
| `confirmacaoSenha` | `String` | Sim | Nao pode ser vazio, deve ser igual ao campo `senha` |

### Exemplo

```json
{
  "name": "Gon Freecss",
  "email": "gon@hunterxhunter.com",
  "senha": "Jajanken@1",
  "confirmacaoSenha": "Jajanken@1"
}
```

## Respostas

Todas as respostas seguem o formato padrao `ResponseDTO<T>`:

```json
{
  "success": true|false,
  "code": "CODIGO_ERRO",       // null em caso de sucesso
  "message": "...",
  "content": null|{...},       // campos null sao omitidos do JSON
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 200 OK — Registro Bem-sucedido

```json
{
  "success": true,
  "message": "Usuário registrado com sucesso",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 400 Bad Request — Erro de Validacao

Retornado quando a validacao de campos ou de classe falha (ex.: campos vazios, email invalido, senha fraca, senhas diferentes).

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Erro de validação dos campos",
  "content": {
    "senha": "A senha deve ter no mínimo 8 caracteres, incluindo letra maiúscula, minúscula e caractere especial",
    "confirmacaoSenha": "A confirmação de senha é obrigatória"
  },
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 400 Bad Request — Erro de Negocio (Email Duplicado)

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "O email 'gon@hunterxhunter.com' já está cadastrado",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

### 400 Bad Request — Erro de Negocio (Nome Duplicado)

```json
{
  "success": false,
  "code": "BUSINESS_ERROR",
  "message": "O nome 'Gon Freecss' já está cadastrado",
  "timestamp": "2026-03-24T12:00:00Z"
}
```

## Arquitetura

A feature segue o padrao de **Functional Slice**:

```
register/
  controller/RegisterController.java   -- Endpoint REST
  service/RegisterService.java          -- Logica de negocio
  dto/RegisterDTO.java                  -- DTO de requisicao com validacoes
  mapper/RegisterMapper.java            -- Conversao DTO para Entity
```

### Fluxo

```
Cliente
  |
  v
RegisterController (@Valid @RequestBody RegisterDTO)
  |  -- Jakarta Bean Validation executa aqui (campo + classe)
  |  -- Falhas -> GlobalExceptionHandler -> VALIDATION_ERROR
  v
RegisterService.register(dto)
  |  1. Verifica unicidade do email  -> EmailAlreadyExistsException
  |  2. Verifica unicidade do nome   -> NameAlreadyExistsException
  |  3. Criptografa a senha (BCrypt)
  |  4. Converte DTO para Entity via RegisterMapper
  |  5. Salva via UserRepository
  v
ResponseDTO.success("Usuário registrado com sucesso")
```

## Validacoes

### Nivel de Campo (Jakarta Bean Validation)

Aplicadas em campos individuais do `RegisterDTO`:

| Anotacao | Campo | Mensagem |
|----------|-------|----------|
| `@NotBlank` | `name` | "O nome é obrigatório" |
| `@Size(min=3, max=100)` | `name` | "O nome deve ter entre 3 e 100 caracteres" |
| `@NotBlank` | `email` | "O email é obrigatório" |
| `@Email` | `email` | "Email inválido" |
| `@NotBlank` | `senha` | "A senha é obrigatória" |
| `@ValidPassword` | `senha` | Veja o validador customizado abaixo |
| `@NotBlank` | `confirmacaoSenha` | "A confirmação de senha é obrigatória" |

### Customizado: `@ValidPassword`

**Localizacao**: `shared/validation/ValidPassword.java` + `PasswordValidator.java`

Aplica regras de forca de senha usando um padrao regex. A senha deve conter **todos** os seguintes criterios:

| Regra | Descricao |
|-------|-----------|
| Comprimento minimo | No minimo 8 caracteres |
| Letra minuscula | Pelo menos uma `[a-z]` |
| Letra maiuscula | Pelo menos uma `[A-Z]` |
| Caractere especial | Pelo menos um de `@#$%^&+=!*()_-{}[]:;<>,.?/~\|` |

Regex utilizado:
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()_\-{}\[\]:;<>,.?/~`|]).{8,}$
```

> Nota: Um digito numerico **nao** e explicitamente exigido pelo regex, porem o comprimento minimo de 8 combinado com as demais regras garante senhas fortes.

### Customizado: `@PasswordMatch`

**Localizacao**: `shared/validation/PasswordMatch.java` + `PasswordMatchValidator.java`

Uma anotacao de **nivel de classe** no `RegisterDTO` que compara dois campos usando `BeanWrapperImpl`:

```java
@PasswordMatch(
    password = "senha",
    confirmPassword = "confirmacaoSenha",
    message = "A senha e a confirmação de senha não coincidem"
)
```

Se os campos nao coincidirem, um erro de validacao de nivel de classe e retornado com o nome do objeto do DTO como chave (nao o nome de um campo).

## Seguranca

### Criptografia de Senha

As senhas sao criptografadas com `BCryptPasswordEncoder`, fornecido como bean Spring no `SecurityConfig`. A codificacao acontece no `RegisterService` antes da entidade ser persistida:

```java
registerMapper.toEntity(dto, passwordEncoder.encode(dto.getSenha()));
```

O `RegisterMapper` recebe a senha ja codificada e a define diretamente na entidade `User`. A senha em texto puro nunca e armazenada.

### Acesso Publico

O endpoint `/register` esta configurado como `permitAll()` no `SecurityConfig`, ou seja, nenhum token JWT e necessario. Porem, o `RateLimitFilter` ainda se aplica para prevenir abuso (5 requisicoes/minuto por IP).

## Banco de Dados

A tabela `users` e criada pela migracao Flyway `V1_Create_Users_Table.sql`:

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Tratamento de Excecoes

Todas as excecoes sao capturadas pelo `GlobalExceptionHandler`:

| Excecao | Status HTTP | Codigo de Erro | Gatilho |
|---------|-------------|----------------|---------|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` | Falhas de Bean Validation |
| `EmailAlreadyExistsException` | 400 | `BUSINESS_ERROR` | Email duplicado |
| `NameAlreadyExistsException` | 400 | `BUSINESS_ERROR` | Nome duplicado |
| `Exception` (fallback) | 500 | `INTERNAL_ERROR` | Erros inesperados |
