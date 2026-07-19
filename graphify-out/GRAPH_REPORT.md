# Graph Report - .  (2026-07-19)

## Corpus Check
- 76 files · ~105,171 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 564 nodes · 1271 edges · 37 communities (26 shown, 11 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 173 edges (avg confidence: 0.81)
- Token cost: 75,726 input · 0 output

## Community Hubs (Navigation)
- Entidades de Sala (JPA)
- Login Controller
- Filtro JWT
- Rate Limiting Redis
- Room Controller
- Exceções de Salas
- Validação de Senha
- Serviço de Convites Redis
- Docs de Auth e Infra
- ResponseDTO e ExceptionHandler
- Exceções de Negócio
- Validador PasswordMatch
- Testes RegisterService
- DTO de Registro
- Testes RegisterController
- SecurityConfig
- Config de Teste Web
- Register Controller
- Registro: Service e Mapper
- DotenvConfig
- SwaggerConfig
- JacksonConfig
- Teste de Contexto Spring
- Entidade User
- Tema Hunter x Hunter
- Script Gradlew
- Aplicação Principal
- Templates de Issues
- Config de Testes H2
- Migração Users
- Migração Rooms
- Migração Invite Hash
- Migração Remove Invite
- Migração Room Players
- Observabilidade Planejada

## God Nodes (most connected - your core abstractions)
1. `ResponseDTO` - 30 edges
2. `User` - 26 edges
3. `RoomServiceTest` - 23 edges
4. `UserRepository` - 22 edges
5. `JwtService` - 22 edges
6. `RateLimitFilterTest` - 22 edges
7. `RoomControllerTest` - 18 edges
8. `RegisterDTO` - 17 edges
9. `RoomService` - 17 edges
10. `Room` - 16 edges

## Surprising Connections (you probably didn't know these)
- `RPG HxH Backend API (Project Overview)` --references--> `DotenvConfig (.env Loading)`  [EXTRACTED]
  README.MD → docs/initial_setup.md
- `RPG HxH Backend API (Project Overview)` --references--> `Secure Invite System`  [EXTRACTED]
  README.MD → docs/rooms.md
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `Bug Report Issue Template` --semantically_similar_to--> `Feature Request Issue Template`  [INFERRED] [semantically similar]
  .github/bug_report.yaml → .github/feature_request.yaml
- `RPG HxH Backend API (Project Overview)` --references--> `Login and JWT Authentication Flow`  [EXTRACTED]
  README.MD → docs/authentication.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_rooms_redis_invite_service, docs_initial_setup_redis_infrastructure [INFERRED 0.85]
- **Room Invite Lifecycle (create, invite, join)** — docs_rooms_room_creation, docs_rooms_invite_system, docs_rooms_join_room, docs_rooms_redis_invite_service [EXTRACTED 1.00]

## Communities (37 total, 11 thin omitted)

### Community 0 - "Entidades de Sala (JPA)"
Cohesion: 0.07
Nodes (36): JpaRepository, Lock, Query, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor (+28 more)

### Community 1 - "Login Controller"
Cohesion: 0.06
Nodes (35): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController, AllArgsConstructor (+27 more)

### Community 2 - "Filtro JWT"
Cohesion: 0.09
Nodes (22): Claims, OncePerRequestFilter, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override (+14 more)

### Community 3 - "Rate Limiting Redis"
Cohesion: 0.12
Nodes (17): Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate, RateLimitFilter (+9 more)

### Community 4 - "Room Controller"
Cohesion: 0.11
Nodes (25): GetMapping, RequestMapping, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag (+17 more)

### Community 5 - "Exceções de Salas"
Cohesion: 0.12
Nodes (13): InvalidInviteException, RoomNotFoundException, Bean, HttpSecurity, Import, MockMvc, SecurityFilterChain, Test (+5 more)

### Community 6 - "Validação de Senha"
Cohesion: 0.13
Nodes (15): ConstraintValidator, NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint (+7 more)

### Community 7 - "Serviço de Convites Redis"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 8 - "Docs de Auth e Infra"
Cohesion: 0.14
Nodes (24): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+16 more)

### Community 9 - "ResponseDTO e ExceptionHandler"
Cohesion: 0.27
Nodes (9): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, GlobalExceptionHandler (+1 more)

### Community 10 - "Exceções de Negócio"
Cohesion: 0.11
Nodes (7): BusinessException, EmailAlreadyExistsException, NameAlreadyExistsException, PasswordMismatchException, PlayerAlreadyInRoomException, RoomFullException, UserNotFoundException

### Community 11 - "Validador PasswordMatch"
Cohesion: 0.25
Nodes (6): ConstraintValidatorContext, Override, PasswordMatchValidator, BeforeEach, Test, PasswordMatchValidatorTest

### Community 12 - "Testes RegisterService"
Cohesion: 0.30
Nodes (5): BeforeEach, ExtendWith, PasswordEncoder, Test, RegisterServiceTest

### Community 13 - "DTO de Registro"
Cohesion: 0.24
Nodes (11): AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema, RegisterDTO, Constraint, Documented (+3 more)

### Community 15 - "SecurityConfig"
Cohesion: 0.31
Nodes (7): EnableWebSecurity, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain, SecurityConfig

### Community 16 - "Config de Teste Web"
Cohesion: 0.25
Nodes (9): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, TestConfiguration, WebMvcTest (+1 more)

### Community 17 - "Register Controller"
Cohesion: 0.29
Nodes (7): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController

### Community 18 - "Registro: Service e Mapper"
Cohesion: 0.39
Nodes (6): Component, RegisterMapper, PasswordEncoder, Service, Transactional, RegisterService

### Community 19 - "DotenvConfig"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 20 - "SwaggerConfig"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 21 - "JacksonConfig"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 22 - "Teste de Contexto Spring"
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 24 - "Tema Hunter x Hunter"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 25 - "Script Gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **14 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+9 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `DTO de Registro` to `Testes RegisterService`, `Testes RegisterController`, `Config de Teste Web`, `Register Controller`, `Registro: Service e Mapper`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `ResponseDTO` connect `ResponseDTO e ExceptionHandler` to `Entidades de Sala (JPA)`, `Login Controller`, `Room Controller`, `Exceções de Salas`, `Testes RegisterService`, `Register Controller`, `Registro: Service e Mapper`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Filtro JWT` to `Login Controller`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _14 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Entidades de Sala (JPA)` be split into smaller, more focused modules?**
  _Cohesion score 0.0700152207001522 - nodes in this community are weakly interconnected._
- **Should `Login Controller` be split into smaller, more focused modules?**
  _Cohesion score 0.06312098188194039 - nodes in this community are weakly interconnected._