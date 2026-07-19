# Graph Report - .  (2026-07-19)

## Corpus Check
- 81 files · ~108,392 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 714 nodes · 1434 edges · 68 communities (25 shown, 43 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 170 edges (avg confidence: 0.81)
- Token cost: 39,002 input · 0 output

## Community Hubs (Navigation)
- JsonInclude / User.java / .existsByEmail
- AfterEach / BeforeEach / BusinessExcepti
- UserRepository / .findByEmail() / LoginC
- DataJpaTest / JpaRepository / Lock
- Claims / SecretKey / JwtAuthenticationFi
- Import / InvalidInviteException / Invite
- ApiResponses / DeleteMapping / GetMappin
- OncePerRequestFilter / RateLimitFilter.j
- Docker Compose Infrastructure / postgres
- NullAndEmptySource / ParameterizedTest /
- HashOperations / RedisInviteService.java
- ConstraintValidator / PasswordMatch.java
- ExceptionHandler / InvalidCredentialsExc
- Configuration / CorsConfigurationSource 
- BusinessException.java / BusinessExcepti
- InvalidCredentialsException / JwtAuthent
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
- Bean
- Flyway Schema Migrations Policy
- Planned Observability
- HttpSecurity
- RedisInviteService
- ResponseEntity
- RoomPlayerRepository
- RoomRepository
- SecurityFilterChain
- Configuration
- PasswordEncoder
- ApiResponses
- CreateRoomDTO
- Operation
- PostMapping
- RestController
- RoomResponseDTO
- Tag
- UpdateRoomDTO
- CreateRoomDTO
- Room
- RoomResponseDTO
- Service
- Transactional
- UpdateRoomDTO
- Import
- MockMvc
- TestConfiguration
- WebMvcTest
- AfterEach
- BeforeEach
- ExtendWith
- Test
- User
- UserRepository

## God Nodes (most connected - your core abstractions)
1. `RoomServiceTest` - 35 edges
2. `RoomControllerTest` - 31 edges
3. `RoomService` - 23 edges
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
- `redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume)` --shares_data_with--> `Security Model (BCrypt, JWT 8h, Redis Sessions, Rate Limiting)`  [INFERRED]
  docker-compose.yml → README.MD
- `GET /rooms/{id}/invite — Secure Invite System` --documented_in--> `Secure Invite System (Redis TTL 8h)`  [EXTRACTED]
  README.MD → docs/rooms.md
- `GET /rooms — List My Rooms` --documented_in--> `List My Rooms Flow (GET /rooms)`  [INFERRED]
  README.MD → docs/rooms.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **JWT Authentication Flow** — docs_authentication_login_flow, docs_authentication_jwt_service, docs_authentication_redis_session_service, docs_authentication_jwt_authentication_filter, docs_authentication_rate_limit_filter, docs_authentication_security_config [EXTRACTED 1.00]
- **Redis TTL-Backed State (sessions, rate limits, invites)** — docs_authentication_redis_session_service, docs_authentication_rate_limit_filter, docs_initial_setup_redis_infrastructure [INFERRED 0.85]

## Communities (68 total, 43 thin omitted)

### Community 0 - "JsonInclude / User.java / .existsByEmail"
Cohesion: 0.05
Nodes (42): JsonInclude, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, RegisterController (+34 more)

### Community 1 - "AfterEach / BeforeEach / BusinessExcepti"
Cohesion: 0.06
Nodes (27): AfterEach, BeforeEach, BusinessException, CreateRoomDTO, ExtendWith, Room, Service, InviteResponseDTO (+19 more)

### Community 2 - "UserRepository / .findByEmail() / LoginC"
Cohesion: 0.06
Nodes (36): UserRepository, ApiResponses, Operation, PostMapping, ResponseEntity, RestController, Tag, LoginController (+28 more)

### Community 3 - "DataJpaTest / JpaRepository / Lock"
Cohesion: 0.06
Nodes (37): DataJpaTest, JpaRepository, Lock, Query, RoomPlayer, AllArgsConstructor, Builder, Data (+29 more)

### Community 4 - "Claims / SecretKey / JwtAuthenticationFi"
Cohesion: 0.09
Nodes (21): Claims, SecretKey, Component, FilterChain, HttpServletRequest, HttpServletResponse, Override, JwtAuthenticationFilter (+13 more)

### Community 5 - "Import / InvalidInviteException / Invite"
Cohesion: 0.10
Nodes (17): Import, InvalidInviteException, InviteResponseDTO, MockMvc, ResponseDTO, RoomAccessDeniedException, RoomNotFoundException, RoomResponseDTO (+9 more)

### Community 6 - "ApiResponses / DeleteMapping / GetMappin"
Cohesion: 0.11
Nodes (30): ApiResponses, DeleteMapping, GetMapping, Operation, PatchMapping, PostMapping, RequestMapping, RestController (+22 more)

### Community 7 - "OncePerRequestFilter / RateLimitFilter.j"
Cohesion: 0.12
Nodes (18): OncePerRequestFilter, Component, FilterChain, HttpServletRequest, HttpServletResponse, ObjectMapper, Override, StringRedisTemplate (+10 more)

### Community 8 - "Docker Compose Infrastructure / postgres"
Cohesion: 0.08
Nodes (31): Docker Compose Infrastructure (PostgreSQL + Redis with healthchecks and persistent volumes), postgres service (postgres:15-alpine, env-driven DB_NAME/DB_USERNAME/DB_PASSWORD, pg_isready healthcheck, postgres_data volume), redis service (redis:7-alpine, AOF appendonly enabled, redis-cli ping healthcheck, redis_data volume), Configurable Max Players (2-10, default 10), Delete Room Flow (DELETE /rooms/{id}), findRoomAsMaster Helper (Master Authorization), getAuthenticatedUser Helper, INVITE_BASE_URL Configurable Base (+23 more)

### Community 9 - "NullAndEmptySource / ParameterizedTest /"
Cohesion: 0.14
Nodes (14): NullAndEmptySource, ParameterizedTest, Pattern, ConstraintValidatorContext, Override, PasswordValidator, Constraint, Documented (+6 more)

### Community 10 - "HashOperations / RedisInviteService.java"
Cohesion: 0.16
Nodes (10): HashOperations, Service, StringRedisTemplate, RedisInviteService, BeforeEach, ExtendWith, StringRedisTemplate, Test (+2 more)

### Community 11 - "ConstraintValidator / PasswordMatch.java"
Cohesion: 0.16
Nodes (12): ConstraintValidator, Constraint, Documented, Retention, Target, PasswordMatch, ConstraintValidatorContext, Override (+4 more)

### Community 12 - "ExceptionHandler / InvalidCredentialsExc"
Cohesion: 0.29
Nodes (9): ExceptionHandler, InvalidCredentialsException, MethodArgumentNotValidException, RestControllerAdvice, Slf4j, GlobalExceptionHandler, BusinessException, ResponseDTO (+1 more)

### Community 13 - "Configuration / CorsConfigurationSource "
Cohesion: 0.27
Nodes (10): Configuration, CorsConfigurationSource, EnableWebSecurity, JwtAuthenticationFilter, PasswordEncoder, RateLimitFilter, Bean, HttpSecurity (+2 more)

### Community 14 - "BusinessException.java / BusinessExcepti"
Cohesion: 0.13
Nodes (5): BusinessException, InvalidInviteException, PasswordMismatchException, RoomAccessDeniedException, RoomNotFoundException

### Community 15 - "InvalidCredentialsException / JwtAuthent"
Cohesion: 0.20
Nodes (14): InvalidCredentialsException (401 BUSINESS_ERROR), JwtAuthenticationFilter, JwtService (Token Generation and Validation), Login and JWT Authentication Flow, RateLimitFilter (Redis IP Rate Limiting), RedisSessionService (Redis Session Storage), SecurityConfig (Spring Security Configuration), DotenvConfig (.env Loading) (+6 more)

### Community 16 - "ApplicationContextInitializer / Configur"
Cohesion: 0.53
Nodes (4): ApplicationContextInitializer, ConfigurableApplicationContext, DotenvConfig, Override

### Community 17 - "OpenAPI / SwaggerConfig.java / Bean"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, SwaggerConfig

### Community 18 - "JacksonConfig.java / JacksonConfig / .ob"
Cohesion: 0.53
Nodes (4): JacksonConfig, Bean, Configuration, ObjectMapper

### Community 19 - "InviteResponseDTO.java / InviteResponseD"
Cohesion: 0.60
Nodes (5): InviteResponseDTO, AllArgsConstructor, Builder, Data, NoArgsConstructor

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
- **24 isolated node(s):** `users`, `rooms`, `rooms`, `rooms`, `room_players` (+19 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Killua README Image` and `README.md`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **Why does `RegisterDTO` connect `JsonInclude / User.java / .existsByEmail` to `ConstraintValidator / PasswordMatch.java`?**
  _High betweenness centrality (0.127) - this node is a cross-community bridge._
- **Why does `RoomService` connect `AfterEach / BeforeEach / BusinessExcepti` to `Import / InvalidInviteException / Invite`, `ApiResponses / DeleteMapping / GetMappin`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `JwtService` connect `Claims / SecretKey / JwtAuthenticationFi` to `UserRepository / .findByEmail() / LoginC`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **What connects `users`, `rooms`, `rooms` to the rest of the system?**
  _24 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `JsonInclude / User.java / .existsByEmail` be split into smaller, more focused modules?**
  _Cohesion score 0.05401234567901234 - nodes in this community are weakly interconnected._
- **Should `AfterEach / BeforeEach / BusinessExcepti` be split into smaller, more focused modules?**
  _Cohesion score 0.061621621621621624 - nodes in this community are weakly interconnected._