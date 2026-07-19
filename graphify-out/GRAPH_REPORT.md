# Graph Report - .  (2026-07-19)

## Corpus Check
- 7 files · ~105,969 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 643 nodes · 1300 edges · 61 communities (27 shown, 34 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 154 edges (avg confidence: 0.81)
- Token cost: 36,411 input · 0 output

## Community Hubs (Navigation)
- AfterEach / BeforeEach / CreateRoomDTO
- UserRepository / .findByEmail() / LoginC
- Claims / SecretKey / JwtAuthenticationFi
- User.java / .existsByEmail() / .existsBy
- OncePerRequestFilter / RateLimitFilter.j
- Bean / HttpSecurity / Import
- JpaRepository / Lock / Query
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- Docker Compose Infrastructure / postgres
- ConstraintValidator / PasswordMatch.java
- ExceptionHandler / JsonInclude / MethodA
- RegisterControllerTest.java / Bean / Htt
- ApiResponses / DeleteMapping / GetMappin
- InvalidCredentialsException / JwtAuthent
- EnableWebSecurity / SecurityConfig.java 
- CreateRoomDTO.java / CreateRoomDTO / All
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
- Operation
- PostMapping
- ResponseEntity
- RestController
- Tag
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
1. `RoomServiceTest` - 26 edges
2. `JwtService` - 22 edges
3. `ResponseDTO` - 22 edges
4. `RateLimitFilterTest` - 22 edges
5. `RoomControllerTest` - 22 edges
6. `RoomService` - 21 edges
7. `User` - 20 edges
8. `UserRepository` - 17 edges
9. `RegisterDTO` - 17 edges
10. `RegisterControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `RedisInviteService (invite hash + reverse index, TTL 8h)`  [INFERRED]
  docker-compose.yml → docs/rooms.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (JWT, BCrypt, Rate Limiting, Stateless Sessions)`  [INFERRED]
  docker-compose.yml → README.MD
- `Room Creation Flow (POST /rooms)` --implements--> `Functional Slices Architecture`  [EXTRACTED]
  docs/rooms.md → README.MD
- `Bug Report Issue Template` --semantically_similar_to--> `Feature Request Issue Template`  [INFERRED] [semantically similar]
  .github/bug_report.yaml → .github/feature_request.yaml

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Room deletion transaction: findRoomAsMaster -> removeInvite (Redis) -> deleteByRoom (room_players) -> delete room** — docs_rooms_delete_room_flow, docs_rooms_findroomasmaster, docs_rooms_redisinviteservice, docs_rooms_player_count_derivation [EXTRACTED 1.00]
- **Invite lifecycle: master generates via GET /rooms/{id}/invite -> RedisInviteService stores hash+reverse index (TTL 8h) -> player joins via GET /rooms/join/{hash}** — docs_rooms_invite_generation_flow, docs_rooms_redisinviteservice, docs_rooms_join_room_flow, docs_rooms_invite_base_url [EXTRACTED 1.00]
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (61 total, 34 thin omitted)

### Community 0 - "AfterEach / BeforeEach / CreateRoomDTO"
Cohesion: 0.05
Nodes (35): AfterEach, BeforeEach, CreateRoomDTO, ExtendWith, Room, RoomPlayer, Service, Room (+27 more)

### Community 1 - "UserRepository / .findByEmail() / LoginC"
Cohesion: 0.07
Nodes (36): UserRepository, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController (+28 more)

### Community 2 - "Claims / SecretKey / JwtAuthenticationFi"
Cohesion: 0.09
Nodes (21): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, JwtAuthenticationFilter (+13 more)

### Community 3 - "User.java / .existsByEmail() / .existsBy"
Cohesion: 0.08
Nodes (28): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController, AllArgsConstructor (+20 more)

### Community 4 - "OncePerRequestFilter / RateLimitFilter.j"
Cohesion: 0.12
Nodes (18): OncePerRequestFilter, Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate (+10 more)

### Community 5 - "Bean / HttpSecurity / Import"
Cohesion: 0.13
Nodes (14): Bean, HttpSecurity, Import, InviteResponseDTO, MockMvc, ResponseDTO, RoomResponseDTO, SecurityFilterChain (+6 more)

### Community 6 - "JpaRepository / Lock / Query"
Cohesion: 0.08
Nodes (25): JpaRepository, Lock, Query, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor (+17 more)

### Community 7 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 8 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 9 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.13
Nodes (25): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), Room Deletion Flow (DELETE /rooms/{id}, master-only, single transaction), findRoomAsMaster(UUID) helper, getAuthenticatedUser() helper, INVITE_BASE_URL configurable invite URL base, Invite Generation Flow (GET /rooms/{id}/invite, master-only) (+17 more)

### Community 10 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 11 - "ExceptionHandler / JsonInclude / MethodA"
Cohesion: 0.26
Nodes (9): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, GlobalExceptionHandler (+1 more)

### Community 12 - "RegisterControllerTest.java / Bean / Htt"
Cohesion: 0.19
Nodes (11): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, Test, TestConfiguration (+3 more)

### Community 13 - "ApiResponses / DeleteMapping / GetMappin"
Cohesion: 0.22
Nodes (14): ApiResponses, DeleteMapping, GetMapping, Operation, PostMapping, RequestMapping, ResponseEntity, RestController (+6 more)

### Community 14 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 15 - "EnableWebSecurity / SecurityConfig.java "
Cohesion: 0.31
Nodes (7): EnableWebSecurity, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain, SecurityConfig

### Community 16 - "CreateRoomDTO.java / CreateRoomDTO / All"
Cohesion: 0.52
Nodes (6): CreateRoomDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema

### Community 17 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 18 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 19 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 20 - "InviteResponseDTO.java / InviteResponseD"
Cohesion: 0.60
Nodes (5): InviteResponseDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor

### Community 21 - "RoomResponseDTO.java / AllArgsConstructo"
Cohesion: 0.60
Nodes (5): AllArgsConstructor, Builder, Data, NoArgsConstructor, RoomResponseDTO

### Community 22 - "SpringBootTest / RpgHxhApplicationTests."
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 23 - "Hunter x Hunter Theme / Killua Zoldyck /"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 24 - "gradlew / gradlew script / die()"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **21 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+16 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `User.java / .existsByEmail() / .existsBy` to `ConstraintValidator / PasswordMatch.java`, `RegisterControllerTest.java / Bean / Htt`?**
  _High betweenness centrality (0.124) - this node is a cross-community bridge._
- **Why does `RoomService` connect `AfterEach / BeforeEach / CreateRoomDTO` to `Bean / HttpSecurity / Import`, `ApiResponses / DeleteMapping / GetMappin`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Claims / SecretKey / JwtAuthenticationFi` to `UserRepository / .findByEmail() / LoginC`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _21 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AfterEach / BeforeEach / CreateRoomDTO` be split into smaller, more focused modules?**
  _Cohesion score 0.0526006464883926 - nodes in this community are weakly interconnected._
- **Should `UserRepository / .findByEmail() / LoginC` be split into smaller, more focused modules?**
  _Cohesion score 0.06547619047619048 - nodes in this community are weakly interconnected._