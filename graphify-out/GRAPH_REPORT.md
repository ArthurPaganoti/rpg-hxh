# Graph Report - .  (2026-08-01)

## Corpus Check
- 84 files · ~110,062 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 728 nodes · 1509 edges · 69 communities (27 shown, 42 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 192 edges (avg confidence: 0.81)
- Token cost: 41,263 input · 0 output

## Community Hubs (Navigation)
- AfterEach / Bean / BusinessException
- JsonInclude / User.java / AllArgsConstru
- RoomPlayer / Service / Room
- Claims / SecretKey / .findByEmail()
- LoginController.java / ApiResponses / Op
- ApiResponses / DeleteMapping / GetMappin
- DataJpaTest / JpaRepository / Lock
- OncePerRequestFilter / RateLimitFilter.j
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- ConstraintValidator / PasswordMatch.java
- RegisterControllerTest.java / Bean / Htt
- Docker Compose Infrastructure / postgres
- ExceptionHandler / InvalidCredentialsExc
- BusinessException.java / BusinessExcepti
- Configuration / CorsConfigurationSource 
- InvalidCredentialsException / JwtAuthent
- CreateRoomDTO.java / CreateRoomDTO / All
- ApplicationContextInitializer / Configur
- OpenAPI / SwaggerConfig.java / Bean
- JacksonConfig.java / JacksonConfig / .ob
- InviteResponseDTO.java / InviteResponseD
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
- BeforeEach
- Flyway Schema Migrations Policy
- Planned Observability
- RedisInviteService
- ResponseEntity
- RoomPlayerRepository
- RoomRepository
- Configuration
- PasswordEncoder
- ApiResponses
- Operation
- PostMapping
- RestController
- RoomResponseDTO
- Tag
- UpdateRoomDTO
- RoomPlayerRepository
- RoomResponseDTO
- Service
- Transactional
- UpdateRoomDTO
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
1. `RoomServiceTest` - 39 edges
2. `RoomControllerTest` - 35 edges
3. `RoomService` - 25 edges
4. `JwtService` - 22 edges
5. `RateLimitFilterTest` - 22 edges
6. `User` - 20 edges
7. `UserRepository` - 17 edges
8. `RegisterDTO` - 17 edges
9. `RegisterControllerTest` - 16 edges
10. `RedisInviteServiceTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Test application.yaml (H2 + create-drop + Flyway off)` --implements--> `Test Environment Configuration (H2, Flyway Disabled)`  [INFERRED]
  src/test/resources/application.yaml → docs/initial_setup.md
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `RedisInviteService`  [INFERRED]
  docker-compose.yml → docs/rooms.md
- `RPG HxH Backend API` --documents--> `List My Rooms Flow`  [EXTRACTED]
  README.MD → docs/rooms.md
- `Bug Report Issue Template` --semantically_similar_to--> `Feature Request Issue Template`  [INFERRED] [semantically similar]
  .github/bug_report.yaml → .github/feature_request.yaml
- `RPG HxH Backend API` --documents--> `Delete Room Flow`  [EXTRACTED]
  README.MD → docs/rooms.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (69 total, 42 thin omitted)

### Community 0 - "AfterEach / Bean / BusinessException"
Cohesion: 0.06
Nodes (29): AfterEach, Bean, BusinessException, CreateRoomDTO, ExtendWith, HttpSecurity, Import, InvalidInviteException (+21 more)

### Community 1 - "JsonInclude / User.java / AllArgsConstru"
Cohesion: 0.06
Nodes (39): JsonInclude, AllArgsConstructor, Builder, Data, Entity, NoArgsConstructor, Table, User (+31 more)

### Community 2 - "RoomPlayer / Service / Room"
Cohesion: 0.08
Nodes (18): RoomPlayer, Service, Room, RoomPlayerRepository, CreateRoomDTO, InviteResponseDTO, RedisInviteService, ResponseDTO (+10 more)

### Community 3 - "Claims / SecretKey / .findByEmail()"
Cohesion: 0.07
Nodes (26): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, JwtAuthenticationFilter (+18 more)

### Community 4 - "LoginController.java / ApiResponses / Op"
Cohesion: 0.07
Nodes (30): ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController, AllArgsConstructor (+22 more)

### Community 5 - "ApiResponses / DeleteMapping / GetMappin"
Cohesion: 0.11
Nodes (31): ApiResponses, DeleteMapping, GetMapping, Operation, PatchMapping, PostMapping, RequestMapping, RestController (+23 more)

### Community 6 - "DataJpaTest / JpaRepository / Lock"
Cohesion: 0.07
Nodes (27): DataJpaTest, JpaRepository, Lock, Query, AllArgsConstructor, Builder, Data, Entity (+19 more)

### Community 7 - "OncePerRequestFilter / RateLimitFilter.j"
Cohesion: 0.12
Nodes (18): OncePerRequestFilter, Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate (+10 more)

### Community 8 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 9 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 10 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 11 - "RegisterControllerTest.java / Bean / Htt"
Cohesion: 0.19
Nodes (11): Bean, HttpSecurity, Import, MockMvc, ObjectMapper, SecurityFilterChain, Test, TestConfiguration (+3 more)

### Community 12 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.13
Nodes (22): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), Configurable Max Players, Delete Room Flow, findRoomAsMaster Helper, findRoomAsMember Helper, getAuthenticatedUser Helper (+14 more)

### Community 13 - "ExceptionHandler / InvalidCredentialsExc"
Cohesion: 0.29
Nodes (9): ExceptionHandler, InvalidCredentialsException, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, GlobalExceptionHandler, BusinessException, ResponseDTO (+1 more)

### Community 14 - "BusinessException.java / BusinessExcepti"
Cohesion: 0.11
Nodes (6): BusinessException, InvalidInviteException, PasswordMismatchException, RoomAccessDeniedException, RoomNotFoundException, UserNotFoundException

### Community 15 - "Configuration / CorsConfigurationSource "
Cohesion: 0.27
Nodes (10): Configuration, CorsConfigurationSource, EnableWebSecurity, JwtAuthenticationFilter, PasswordEncoder, RateLimitFilter, Bean, HttpSecurity (+2 more)

### Community 16 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 17 - "CreateRoomDTO.java / CreateRoomDTO / All"
Cohesion: 0.52
Nodes (6): CreateRoomDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema

### Community 18 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 19 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 20 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 21 - "InviteResponseDTO.java / InviteResponseD"
Cohesion: 0.60
Nodes (5): InviteResponseDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor

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
- **18 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+13 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `JsonInclude / User.java / AllArgsConstru` to `ConstraintValidator / PasswordMatch.java`, `RegisterControllerTest.java / Bean / Htt`?**
  _High betweenness centrality (0.121) - this node is a cross-community bridge._
- **Why does `UserRepository` connect `JsonInclude / User.java / AllArgsConstru` to `Claims / SecretKey / .findByEmail()`, `LoginController.java / ApiResponses / Op`, `DataJpaTest / JpaRepository / Lock`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Why does `RoomPlayerRepository` connect `RoomPlayer / Service / Room` to `AfterEach / Bean / BusinessException`, `DataJpaTest / JpaRepository / Lock`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _18 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AfterEach / Bean / BusinessException` be split into smaller, more focused modules?**
  _Cohesion score 0.05754527162977867 - nodes in this community are weakly interconnected._
- **Should `JsonInclude / User.java / AllArgsConstru` be split into smaller, more focused modules?**
  _Cohesion score 0.06057945566286216 - nodes in this community are weakly interconnected._