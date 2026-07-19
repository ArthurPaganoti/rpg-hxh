# Graph Report - .  (2026-07-19)

## Corpus Check
- 4 files · ~105,412 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 585 nodes · 1271 edges · 34 communities (22 shown, 12 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 167 edges (avg confidence: 0.8)
- Token cost: 37,038 input · 0 output

## Community Hubs (Navigation)
- Salas: Entidades e DTOs
- Login Controller
- Seguranca e JWT
- Registro de Usuario
- Excecoes de Negocio
- Filtros (JWT e Rate Limit)
- Room Controller
- Validacao de Senha
- Servico de Convites Redis
- Anotacoes de Validacao
- Config de Teste Web
- ResponseDTO e ExceptionHandler
- Infra Docker e Autorizacao de Mestre
- Docs de Autenticacao
- DotenvConfig
- SwaggerConfig
- JacksonConfig
- Teste de Contexto Spring
- Tema Hunter x Hunter
- Script Gradlew
- Aplicacao Principal
- Templates de Issues
- Config de Testes H2
- Migracao Users
- Migracao Rooms
- Migracao Invite Hash
- Migracao Remove Invite
- Migracao Room Players
- Politica Flyway
- Observabilidade Planejada
- Anotacao Service
- Anotacao Transactional

## God Nodes (most connected - your core abstractions)
1. `ResponseDTO` - 26 edges
2. `User` - 24 edges
3. `RoomServiceTest` - 23 edges
4. `JwtService` - 22 edges
5. `RateLimitFilterTest` - 22 edges
6. `UserRepository` - 19 edges
7. `RoomService` - 19 edges
8. `RoomControllerTest` - 18 edges
9. `RegisterDTO` - 17 edges
10. `RegisterControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Secure Invite System (Redis-backed, cryptographically secure UUID hash, TTL 8h, reverse index invite:hash:{hash} -> roomId; rationale: on-demand generation by Master only, auto-regenerated after expiry, all rooms private so invite link is the sole access path)` --semantically_similar_to--> `Security Model (BCrypt passwords, stateless JWT 8h HMAC, Redis session tracking, 5 req/min rate limiting on /register and /login, CSRF disabled for stateless API)`  [INFERRED] [semantically similar]
  docs/rooms.md → README.MD
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (BCrypt passwords, stateless JWT 8h HMAC, Redis session tracking, 5 req/min rate limiting on /register and /login, CSRF disabled for stateless API)`  [INFERRED]
  docker-compose.yml → README.MD
- `getAuthenticatedUser() helper (extracts email from SecurityContextHolder, loads User, UserNotFoundException if absent)` --conceptually_related_to--> `Security Model (BCrypt passwords, stateless JWT 8h HMAC, Redis session tracking, 5 req/min rate limiting on /register and /login, CSRF disabled for stateless API)`  [INFERRED]
  docs/rooms.md → README.MD
- `postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume)` --shares_data_with--> `Rooms Flyway Migrations (V2 create rooms, V3 default privacy true, V4 drop is_private/invite_code/invite_hash — invite state moved to Redis, V5 room_players with UNIQUE(room_id, user_id))`  [INFERRED]
  docker-compose.yml → docs/rooms.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Centralized Master Authorization Pattern** — docs_rooms_roomservice, docs_rooms_findroomasmaster, docs_rooms_getauthenticateduser [EXTRACTED 1.00]
- **Redis Invite Lifecycle (generation, storage with TTL, reverse lookup, join)** — docs_rooms_secure_invite_system, docs_rooms_redisinviteservice, docs_rooms_join_room_flow, docker_compose_redis_service [INFERRED 0.85]
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (34 total, 12 thin omitted)

### Community 0 - "Salas: Entidades e DTOs"
Cohesion: 0.06
Nodes (44): CreateRoomDTO, InviteResponseDTO, JpaRepository, Lock, Query, RedisInviteService, ResponseDTO, Room (+36 more)

### Community 1 - "Login Controller"
Cohesion: 0.06
Nodes (36): UserRepository, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController (+28 more)

### Community 2 - "Seguranca e JWT"
Cohesion: 0.07
Nodes (28): Claims, EnableWebSecurity, SecretKey, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain (+20 more)

### Community 3 - "Registro de Usuario"
Cohesion: 0.08
Nodes (28): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController, AllArgsConstructor (+20 more)

### Community 4 - "Excecoes de Negocio"
Cohesion: 0.07
Nodes (19): BusinessException, InvalidInviteException, PasswordMismatchException, PlayerAlreadyInRoomException, RoomAccessDeniedException, RoomFullException, RoomNotFoundException, UserNotFoundException (+11 more)

### Community 5 - "Filtros (JWT e Rate Limit)"
Cohesion: 0.12
Nodes (18): OncePerRequestFilter, Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate (+10 more)

### Community 6 - "Room Controller"
Cohesion: 0.11
Nodes (25): GetMapping, RequestMapping, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag (+17 more)

### Community 7 - "Validacao de Senha"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 8 - "Servico de Convites Redis"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 9 - "Anotacoes de Validacao"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 10 - "Config de Teste Web"
Cohesion: 0.19
Nodes (11): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, Test, TestConfiguration (+3 more)

### Community 11 - "ResponseDTO e ExceptionHandler"
Cohesion: 0.27
Nodes (9): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, GlobalExceptionHandler (+1 more)

### Community 12 - "Infra Docker e Autorizacao de Mestre"
Cohesion: 0.19
Nodes (16): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), findRoomAsMaster(UUID roomId) helper (rationale: centralizes master authorization — auth + RoomNotFoundException 404 + RoomAccessDeniedException 403 — so every future master-only route (delete, rename, kick/ban, email invite) must reuse it instead of duplicating the check), getAuthenticatedUser() helper (extracts email from SecurityContextHolder, loads User, UserNotFoundException if absent), Join Room Flow (GET /rooms/join/{hash}: authenticated player joins via invite hash; 404 invalid/expired invite, 409 room full or player already in room), RedisInviteService (invite management in Redis: getOrCreateInvite, hash key invite:{roomId} with inviteHash/masterId/createdAt fields, reverse lookup, TTL 8h), Room Creation Flow (POST /rooms: authenticated user becomes Master, currentPlayers=1, maxPlayers=10) (+8 more)

### Community 13 - "Docs de Autenticacao"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 14 - "DotenvConfig"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 15 - "SwaggerConfig"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 16 - "JacksonConfig"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 17 - "Teste de Contexto Spring"
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 18 - "Tema Hunter x Hunter"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 19 - "Script Gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **15 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `Registro de Usuario` to `Anotacoes de Validacao`, `Config de Teste Web`?**
  _High betweenness centrality (0.150) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Seguranca e JWT` to `Login Controller`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `ResponseDTO` connect `ResponseDTO e ExceptionHandler` to `Login Controller`, `Registro de Usuario`, `Excecoes de Negocio`, `Room Controller`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Salas: Entidades e DTOs` be split into smaller, more focused modules?**
  _Cohesion score 0.06060606060606061 - nodes in this community are weakly interconnected._
- **Should `Login Controller` be split into smaller, more focused modules?**
  _Cohesion score 0.06398809523809523 - nodes in this community are weakly interconnected._