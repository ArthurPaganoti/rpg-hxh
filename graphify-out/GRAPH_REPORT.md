# Graph Report - .  (2026-07-19)

## Corpus Check
- 8 files · ~107,437 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 698 nodes · 1430 edges · 72 communities (34 shown, 38 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 174 edges (avg confidence: 0.81)
- Token cost: 38,279 input · 0 output

## Community Hubs (Navigation)
- AfterEach / CreateRoomDTO / ExtendWith
- .findByEmail() / LoginController.java / 
- Claims / SecretKey / .SecurityConfig()
- Bean / HttpSecurity / Import
- ExceptionHandler / JsonInclude / MethodA
- OncePerRequestFilter / RateLimitFilter.j
- Docker Compose Infrastructure / postgres
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- ApiResponses / DeleteMapping / GetMappin
- ConstraintValidator / PasswordMatch.java
- RegisterControllerTest.java / Bean / Htt
- DataJpaTest / RoomPlayerRepositoryTest.j
- InvalidCredentialsException / JwtAuthent
- Lock / Query / Room.java
- JpaRepository / AllArgsConstructor / Bui
- RegisterMapper.java / Component / Regist
- EnableWebSecurity / SecurityConfig.java 
- RegisterController.java / ApiResponses /
- RoomPlayer.java / AllArgsConstructor / B
- .existsByEmail() / .existsByName() / .to
- RegisterDTO.java / AllArgsConstructor / 
- CreateRoomDTO.java / CreateRoomDTO / All
- UpdateRoomDTO.java / AllArgsConstructor 
- ApplicationContextInitializer / Configur
- OpenAPI / SwaggerConfig.java / Bean
- JacksonConfig.java / JacksonConfig / .ob
- InviteResponseDTO.java / InviteResponseD
- RoomResponseDTO.java / AllArgsConstructo
- RegisterServiceTest.java / BeforeEach / 
- SpringBootTest / RpgHxhApplicationTests.
- User.java / RegisterMapperTest.java / Te
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
- BeforeEach
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
- RoomPlayerRepository
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
- ExtendWith
- RoomPlayerRepository
- Test
- User
- UserRepository

## God Nodes (most connected - your core abstractions)
1. `RoomServiceTest` - 33 edges
2. `RoomControllerTest` - 31 edges
3. `RoomService` - 23 edges
4. `JwtService` - 22 edges
5. `ResponseDTO` - 22 edges
6. `RateLimitFilterTest` - 22 edges
7. `User` - 20 edges
8. `UserRepository` - 17 edges
9. `RegisterDTO` - 17 edges
10. `RegisterControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `RedisInviteService (invite hash storage + reverse index)`  [INFERRED]
  docker-compose.yml → docs/rooms.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (JWT, BCrypt, Rate Limiting, Stateless Sessions)`  [INFERRED]
  docker-compose.yml → README.MD
- `GET /rooms/{id}/invite Endpoint (Secure Invite)` --references--> `Secure Invite System (Redis, TTL 8h)`  [EXTRACTED]
  README.MD → docs/rooms.md
- `Bug Report Issue Template` --semantically_similar_to--> `Feature Request Issue Template`  [INFERRED] [semantically similar]
  .github/bug_report.yaml → .github/feature_request.yaml

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (72 total, 38 thin omitted)

### Community 0 - "AfterEach / CreateRoomDTO / ExtendWith"
Cohesion: 0.06
Nodes (31): AfterEach, CreateRoomDTO, ExtendWith, Room, RoomPlayer, Service, Room, User (+23 more)

### Community 1 - ".findByEmail() / LoginController.java / "
Cohesion: 0.06
Nodes (35): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController, AllArgsConstructor (+27 more)

### Community 2 - "Claims / SecretKey / .SecurityConfig()"
Cohesion: 0.09
Nodes (21): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, JwtAuthenticationFilter (+13 more)

### Community 3 - "Bean / HttpSecurity / Import"
Cohesion: 0.11
Nodes (14): Bean, HttpSecurity, Import, InviteResponseDTO, MockMvc, ResponseDTO, RoomResponseDTO, SecurityFilterChain (+6 more)

### Community 4 - "ExceptionHandler / JsonInclude / MethodA"
Cohesion: 0.10
Nodes (16): ExceptionHandler, JsonInclude, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, Getter, ResponseDTO, BusinessException (+8 more)

### Community 5 - "OncePerRequestFilter / RateLimitFilter.j"
Cohesion: 0.12
Nodes (18): OncePerRequestFilter, Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate (+10 more)

### Community 6 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.07
Nodes (33): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), Configurable maxPlayers (2-20, default 10), Delete Room Flow (Redis invite + room_players + room in one transaction), Rooms Feature Documentation, findRoomAsMaster() Helper (centralized master authorization), Rooms Flyway Migrations (V2-V5, rooms + room_players schema) (+25 more)

### Community 7 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 8 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 9 - "ApiResponses / DeleteMapping / GetMappin"
Cohesion: 0.21
Nodes (16): ApiResponses, DeleteMapping, GetMapping, Operation, PatchMapping, PostMapping, RequestMapping, ResponseEntity (+8 more)

### Community 10 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 11 - "RegisterControllerTest.java / Bean / Htt"
Cohesion: 0.19
Nodes (11): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, Test, TestConfiguration (+3 more)

### Community 12 - "DataJpaTest / RoomPlayerRepositoryTest.j"
Cohesion: 0.24
Nodes (8): DataJpaTest, BeforeEach, Room, RoomRepository, Test, User, UserRepository, RoomPlayerRepositoryTest

### Community 13 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 14 - "Lock / Query / Room.java"
Cohesion: 0.21
Nodes (10): Lock, Query, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor, Table (+2 more)

### Community 15 - "JpaRepository / AllArgsConstructor / Bui"
Cohesion: 0.24
Nodes (9): JpaRepository, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor, Table, User (+1 more)

### Community 16 - "RegisterMapper.java / Component / Regist"
Cohesion: 0.29
Nodes (6): Component, RegisterMapper, PasswordEncoder, Service, Transactional, RegisterService

### Community 17 - "EnableWebSecurity / SecurityConfig.java "
Cohesion: 0.36
Nodes (7): EnableWebSecurity, Bean, Configuration, HttpSecurity, PasswordEncoder, SecurityFilterChain, SecurityConfig

### Community 18 - "RegisterController.java / ApiResponses /"
Cohesion: 0.29
Nodes (7): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController

### Community 19 - "RoomPlayer.java / AllArgsConstructor / B"
Cohesion: 0.25
Nodes (7): AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor, Table, RoomPlayer

### Community 21 - "RegisterDTO.java / AllArgsConstructor / "
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema, RegisterDTO

### Community 22 - "CreateRoomDTO.java / CreateRoomDTO / All"
Cohesion: 0.52
Nodes (6): CreateRoomDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema

### Community 23 - "UpdateRoomDTO.java / AllArgsConstructor "
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema, UpdateRoomDTO

### Community 24 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 25 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 26 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 27 - "InviteResponseDTO.java / InviteResponseD"
Cohesion: 0.60
Nodes (5): InviteResponseDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor

### Community 28 - "RoomResponseDTO.java / AllArgsConstructo"
Cohesion: 0.60
Nodes (5): AllArgsConstructor, Builder, Data, NoArgsConstructor, RoomResponseDTO

### Community 29 - "RegisterServiceTest.java / BeforeEach / "
Cohesion: 0.53
Nodes (4): BeforeEach, ExtendWith, PasswordEncoder, RegisterServiceTest

### Community 30 - "SpringBootTest / RpgHxhApplicationTests."
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, RpgHxhApplicationTests

### Community 32 - "Hunter x Hunter Theme / Killua Zoldyck /"
Cohesion: 0.67
Nodes (4): Hunter x Hunter Theme, Killua Zoldyck (Hunter x Hunter character), README.md, Killua README Image

### Community 33 - "gradlew / gradlew script / die()"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Ambiguous Edges - Review These
- `Killua README Image` → `README.md`  [AMBIGUOUS]
  src/main/java/com/rpg/rpghxh/utils/imagens/KILLUA_IMAGEM_README.png · relation: references

## Knowledge Gaps
- **27 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+22 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `RegisterDTO.java / AllArgsConstructor / ` to `ConstraintValidator / PasswordMatch.java`, `RegisterControllerTest.java / Bean / Htt`, `RegisterMapper.java / Component / Regist`, `RegisterController.java / ApiResponses /`, `.existsByEmail() / .existsByName() / .to`, `RegisterServiceTest.java / BeforeEach / `?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `RoomService` connect `AfterEach / CreateRoomDTO / ExtendWith` to `ApiResponses / DeleteMapping / GetMappin`, `Bean / HttpSecurity / Import`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Claims / SecretKey / .SecurityConfig()` to `.findByEmail() / LoginController.java / `?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _27 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AfterEach / CreateRoomDTO / ExtendWith` be split into smaller, more focused modules?**
  _Cohesion score 0.06361570918532944 - nodes in this community are weakly interconnected._
- **Should `.findByEmail() / LoginController.java / ` be split into smaller, more focused modules?**
  _Cohesion score 0.06451612903225806 - nodes in this community are weakly interconnected._