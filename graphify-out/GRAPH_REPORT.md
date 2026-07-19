# Graph Report - .  (2026-07-19)

## Corpus Check
- 5 files · ~105,488 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 601 nodes · 1251 edges · 43 communities (22 shown, 21 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 146 edges (avg confidence: 0.81)
- Token cost: 33,844 input · 0 output

## Community Hubs (Navigation)
- User.java / .existsByEmail() / .existsBy
- UserRepository / .findByEmail() / .findB
- AfterEach / BeforeEach / CreateRoomDTO
- EnableWebSecurity / OncePerRequestFilter
- Claims / SecretKey / JwtAuthenticationFi
- .success() / BusinessException.java / Bu
- GetMapping / RequestMapping / RoomContro
- JpaRepository / Lock / Query
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- ConstraintValidator / PasswordMatch.java
- ExceptionHandler / JsonInclude / MethodA
- Docker Compose Infrastructure / postgres
- InvalidCredentialsException / JwtAuthent
- ApplicationContextInitializer / Configur
- OpenAPI / SwaggerConfig.java / Bean
- JacksonConfig.java / JacksonConfig / .ob
- SpringBootTest / RpgHxhApplicationTests.
- Hunter x Hunter Theme / Killua Zoldyck /
- gradlew / gradlew script / die()
- SpringBootApplication / RpgHxhApplicatio
- Bug Report Issue Template / Feature Requ
- Test Environment Configuration / Test ap
- V1__Create_Users_Table.sql / users
- V2__Create_Rooms_Table.sql / rooms
- V3__Add_Invite_Hash_And_Default_Privacy.
- V4__Remove_Privacy_And_Invite_Code.sql /
- V5__Create_Room_Players_Table.sql / room
- Flyway Schema Migrations Policy
- Planned Observability
- RedisInviteService
- RoomPlayerRepository
- RoomRepository
- Service
- Transactional
- AfterEach
- BeforeEach
- ExtendWith
- Test
- User
- UserRepository

## God Nodes (most connected - your core abstractions)
1. `ResponseDTO` - 26 edges
2. `RoomServiceTest` - 23 edges
3. `JwtService` - 22 edges
4. `RateLimitFilterTest` - 22 edges
5. `User` - 20 edges
6. `RoomService` - 20 edges
7. `RoomControllerTest` - 18 edges
8. `UserRepository` - 17 edges
9. `RegisterDTO` - 17 edges
10. `RegisterControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `RedisInviteService`  [INFERRED]
  docker-compose.yml → docs/rooms.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (JWT, BCrypt, Rate Limiting)`  [INFERRED]
  docker-compose.yml → README.MD
- `RPG HxH Backend API` --references--> `Configurable INVITE_BASE_URL env var`  [EXTRACTED]
  README.MD → docs/rooms.md
- `Join Room Flow (GET /rooms/join/{hash})` --references--> `ResponseDTO Standard Response Format`  [INFERRED]
  docs/rooms.md → README.MD

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **hyper_invite_flow** — docs_rooms_secure_invite_system, docs_rooms_redisinviteservice, docs_rooms_join_room_flow, docs_rooms_invite_base_url [EXTRACTED 1.00]
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (43 total, 21 thin omitted)

### Community 0 - "User.java / .existsByEmail() / .existsBy"
Cohesion: 0.06
Nodes (39): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController, AllArgsConstructor (+31 more)

### Community 1 - "UserRepository / .findByEmail() / .findB"
Cohesion: 0.06
Nodes (36): UserRepository, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController (+28 more)

### Community 2 - "AfterEach / BeforeEach / CreateRoomDTO"
Cohesion: 0.09
Nodes (27): AfterEach, BeforeEach, CreateRoomDTO, ExtendWith, InviteResponseDTO, ResponseDTO, Room, RoomPlayer (+19 more)

### Community 3 - "EnableWebSecurity / OncePerRequestFilter"
Cohesion: 0.09
Nodes (26): EnableWebSecurity, OncePerRequestFilter, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain, SecurityConfig (+18 more)

### Community 4 - "Claims / SecretKey / JwtAuthenticationFi"
Cohesion: 0.09
Nodes (20): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, Service (+12 more)

### Community 5 - ".success() / BusinessException.java / Bu"
Cohesion: 0.08
Nodes (18): BusinessException, InvalidInviteException, PasswordMismatchException, PlayerAlreadyInRoomException, RoomAccessDeniedException, RoomFullException, RoomNotFoundException, Bean (+10 more)

### Community 6 - "GetMapping / RequestMapping / RoomContro"
Cohesion: 0.11
Nodes (25): GetMapping, RequestMapping, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag (+17 more)

### Community 7 - "JpaRepository / Lock / Query"
Cohesion: 0.09
Nodes (25): JpaRepository, Lock, Query, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor (+17 more)

### Community 8 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 9 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 10 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 11 - "ExceptionHandler / JsonInclude / MethodA"
Cohesion: 0.27
Nodes (9): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, GlobalExceptionHandler (+1 more)

### Community 12 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.20
Nodes (15): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), findRoomAsMaster(UUID) helper, getAuthenticatedUser() helper, Configurable INVITE_BASE_URL env var, Join Room Flow (GET /rooms/join/{hash}), Player Count Derivation via COUNT(room_players) (+7 more)

### Community 13 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 14 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 15 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 16 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 17 - "SpringBootTest / RpgHxhApplicationTests."
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 18 - "Hunter x Hunter Theme / Killua Zoldyck /"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 19 - "gradlew / gradlew script / die()"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **17 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `User.java / .existsByEmail() / .existsBy` to `ConstraintValidator / PasswordMatch.java`?**
  _High betweenness centrality (0.141) - this node is a cross-community bridge._
- **Why does `ResponseDTO` connect `ExceptionHandler / JsonInclude / MethodA` to `User.java / .existsByEmail() / .existsBy`, `UserRepository / .findByEmail() / .findB`, `.success() / BusinessException.java / Bu`, `GetMapping / RequestMapping / RoomContro`?**
  _High betweenness centrality (0.114) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Claims / SecretKey / JwtAuthenticationFi` to `UserRepository / .findByEmail() / .findB`, `EnableWebSecurity / OncePerRequestFilter`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `User.java / .existsByEmail() / .existsBy` be split into smaller, more focused modules?**
  _Cohesion score 0.058126619770455384 - nodes in this community are weakly interconnected._
- **Should `UserRepository / .findByEmail() / .findB` be split into smaller, more focused modules?**
  _Cohesion score 0.06498015873015874 - nodes in this community are weakly interconnected._