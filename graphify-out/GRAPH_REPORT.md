# Graph Report - .  (2026-07-19)

## Corpus Check
- 8 files · ~106,834 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 671 nodes · 1365 edges · 62 communities (25 shown, 37 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 159 edges (avg confidence: 0.81)
- Token cost: 40,597 input · 0 output

## Community Hubs (Navigation)
- AfterEach / BeforeEach / CreateRoomDTO
- UserRepository / .findByEmail() / LoginC
- Claims / SecretKey / JwtAuthenticationFi
- EnableWebSecurity / OncePerRequestFilter
- User.java / .existsByEmail() / .existsBy
- Bean / HttpSecurity / Import
- JpaRepository / Lock / Query
- ExceptionHandler / JsonInclude / MethodA
- ApiResponses / DeleteMapping / GetMappin
- Docker Compose Infrastructure / postgres
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- ConstraintValidator / PasswordMatch.java
- RegisterControllerTest.java / Bean / Htt
- InvalidCredentialsException / JwtAuthent
- ApplicationContextInitializer / Configur
- OpenAPI / SwaggerConfig.java / Bean
- JacksonConfig.java / JacksonConfig / .ob
- InviteResponseDTO.java / InviteResponseD
- RoomResponseDTO.java / AllArgsConstructo
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
- ApiResponses
- CreateRoomDTO
- Operation
- PostMapping
- ResponseEntity
- RestController
- Tag
- CreateRoomDTO
- Room
- Service
- Transactional
- Bean
- HttpSecurity
- Import
- MockMvc
- SecurityFilterChain
- TestConfiguration
- WebMvcTest
- AfterEach
- BeforeEach
- ExtendWith
- Test
- User
- UserRepository

## God Nodes (most connected - your core abstractions)
1. `RoomServiceTest` - 30 edges
2. `RoomControllerTest` - 28 edges
3. `JwtService` - 22 edges
4. `ResponseDTO` - 22 edges
5. `RateLimitFilterTest` - 22 edges
6. `RoomService` - 22 edges
7. `User` - 20 edges
8. `UserRepository` - 17 edges
9. `RegisterDTO` - 17 edges
10. `RegisterControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `RedisInviteService (getOrCreateInvite, removeInvite, TTL 8h, reverse lookup)`  [INFERRED]
  docker-compose.yml → docs/rooms.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (JWT, BCrypt, Rate Limiting, Stateless Sessions)`  [INFERRED]
  docker-compose.yml → README.MD
- `docs/rooms.md — Rooms Feature Documentation` --implements--> `Functional Slices Architecture`  [EXTRACTED]
  docs/rooms.md → README.MD
- `POST /rooms — Room Creation Endpoint` --conceptually_related_to--> `Room Creation Flow (RoomController -> RoomService.createRoom -> RoomRepository)`  [INFERRED]
  README.MD → docs/rooms.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Master-only room operations authorized via findRoomAsMaster** — docs_rooms_findroomasmaster, docs_rooms_update_room_flow, docs_rooms_delete_room_flow [EXTRACTED 1.00]
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (62 total, 37 thin omitted)

### Community 0 - "AfterEach / BeforeEach / CreateRoomDTO"
Cohesion: 0.07
Nodes (26): AfterEach, BeforeEach, CreateRoomDTO, ExtendWith, Room, Service, InviteResponseDTO, RedisInviteService (+18 more)

### Community 1 - "UserRepository / .findByEmail() / LoginC"
Cohesion: 0.07
Nodes (36): UserRepository, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController (+28 more)

### Community 2 - "Claims / SecretKey / JwtAuthenticationFi"
Cohesion: 0.09
Nodes (20): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, Service (+12 more)

### Community 3 - "EnableWebSecurity / OncePerRequestFilter"
Cohesion: 0.09
Nodes (26): EnableWebSecurity, OncePerRequestFilter, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain, SecurityConfig (+18 more)

### Community 4 - "User.java / .existsByEmail() / .existsBy"
Cohesion: 0.08
Nodes (28): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController, AllArgsConstructor (+20 more)

### Community 5 - "Bean / HttpSecurity / Import"
Cohesion: 0.11
Nodes (14): Bean, HttpSecurity, Import, InviteResponseDTO, MockMvc, ResponseDTO, RoomResponseDTO, SecurityFilterChain (+6 more)

### Community 6 - "JpaRepository / Lock / Query"
Cohesion: 0.07
Nodes (29): JpaRepository, Lock, Query, RoomPlayer, AllArgsConstructor, Builder, Data, Entity (+21 more)

### Community 7 - "ExceptionHandler / JsonInclude / MethodA"
Cohesion: 0.12
Nodes (14): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, BusinessException (+6 more)

### Community 8 - "ApiResponses / DeleteMapping / GetMappin"
Cohesion: 0.13
Nodes (26): ApiResponses, DeleteMapping, GetMapping, Operation, PatchMapping, PostMapping, RequestMapping, ResponseEntity (+18 more)

### Community 9 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.11
Nodes (31): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), Configurable Room Capacity (CreateRoomDTO maxPlayers optional, @Min 2 @Max 20, default 10), Delete Room Flow (single transaction: RedisInviteService.removeInvite, deleteByRoom on room_players — FK has no cascade, then room), docs/rooms.md — Rooms Feature Documentation, findRoomAsMaster(UUID) helper (centralized master authorization: 404 RoomNotFound, 403 RoomAccessDenied), Rooms Flyway Migrations (V2 create rooms, V3 private default, V4 drop privacy/invite columns, V5 room_players unique(room_id,user_id)) (+23 more)

### Community 10 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 11 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 12 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 13 - "RegisterControllerTest.java / Bean / Htt"
Cohesion: 0.19
Nodes (11): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, Test, TestConfiguration (+3 more)

### Community 14 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 15 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 16 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 17 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 18 - "InviteResponseDTO.java / InviteResponseD"
Cohesion: 0.60
Nodes (5): InviteResponseDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor

### Community 19 - "RoomResponseDTO.java / AllArgsConstructo"
Cohesion: 0.60
Nodes (5): AllArgsConstructor, Builder, Data, NoArgsConstructor, RoomResponseDTO

### Community 20 - "SpringBootTest / RpgHxhApplicationTests."
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 21 - "Hunter x Hunter Theme / Killua Zoldyck /"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 22 - "gradlew / gradlew script / die()"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **19 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `User.java / .existsByEmail() / .existsBy` to `ConstraintValidator / PasswordMatch.java`, `RegisterControllerTest.java / Bean / Htt`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Why does `RoomService` connect `AfterEach / BeforeEach / CreateRoomDTO` to `ApiResponses / DeleteMapping / GetMappin`, `Bean / HttpSecurity / Import`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Claims / SecretKey / JwtAuthenticationFi` to `UserRepository / .findByEmail() / LoginC`, `EnableWebSecurity / OncePerRequestFilter`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AfterEach / BeforeEach / CreateRoomDTO` be split into smaller, more focused modules?**
  _Cohesion score 0.07086247086247087 - nodes in this community are weakly interconnected._
- **Should `UserRepository / .findByEmail() / LoginC` be split into smaller, more focused modules?**
  _Cohesion score 0.06547619047619048 - nodes in this community are weakly interconnected._