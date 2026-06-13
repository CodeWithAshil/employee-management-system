# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

Employee Management System — a Spring Boot REST API for managing employee records.

- **Java 21**, **Spring Boot 3.4.1**, **Maven**, **MySQL**
- Base package: `com.ashil.ems`
- Dependencies: Spring Web, Spring Data JPA, Spring Security, Validation, MySQL
  Driver, Lombok, DevTools

## Architecture rule (IMPORTANT)

Request flow must ALWAYS follow **Controller → Service → Repository**. Never skip a layer.

- Controllers depend only on **service interfaces** — never inject or call repositories directly.
- Repositories are accessed **only** from the `service.impl` layer.
- Business logic lives in `service.impl`, not in controllers.
- Keep the `service` (interface) + `service.impl` (implementation) split.

## Package structure (`com.ashil.ems`)

```
controller     REST endpoints
service        Service interfaces
service.impl   Service implementations
repository     Spring Data JPA repositories (Employee, Role, Department)
entity         JPA entities (Employee, Role, Department) + enums
dto            Request/response DTOs
exception      GlobalExceptionHandler, custom exceptions, ErrorResponse
config         Infrastructure config beans (JpaAuditingConfig)
security       SecurityConfig
util           Mappers / helpers
```

## Data model

- **Employee** (`@ManyToOne` → Role and Department, both `nullable=false`, `LAZY`):
  id, firstName, lastName, email (unique), phoneNumber, salary (`BigDecimal`),
  joiningDate (`LocalDate`), status (`EmployeeStatus`), createdAt, updatedAt.
- **Department** (`@OneToMany mappedBy="department"`): id, departmentName,
  departmentCode (unique).
- **Role** (`@OneToMany mappedBy="role"`): id, roleName (`RoleName`).
- Enums: `EmployeeStatus { ACTIVE, INACTIVE }`, `RoleName { ADMIN, HR, EMPLOYEE }`,
  stored as `@Enumerated(EnumType.STRING)`.
- Auditing: `@EnableJpaAuditing` (JpaAuditingConfig) + `@EntityListeners(AuditingEntityListener.class)`
  with `@CreatedDate` / `@LastModifiedDate`.
- Entities use `@Getter/@Setter` + `@Builder` (NOT `@Data`) and `@ToString.Exclude`
  on relationship fields to avoid infinite recursion. Collections use `@Builder.Default`.
- The service resolves `roleId` / `departmentId` from the DTO via RoleRepository /
  DepartmentRepository (404 if missing), then `EmployeeMapper.toEntity(request, role, department)`.

## Conventions

- Use **Lombok** (`@Getter/@Setter`, `@Builder`, `@RequiredArgsConstructor`,
  `@Slf4j`) consistently.
- DTOs (`EmployeeRequest`/`EmployeeResponse`) cross the controller boundary;
  entities do not leave the service layer. Map with `util.EmployeeMapper`.
- Prefer **`Optional`** over returning/handling nulls. Service `getById` returns
  `Optional<EmployeeResponse>`; the controller resolves it with
  `.map(ResponseEntity::ok).orElseThrow(...)`.
- All error responses go through `GlobalExceptionHandler` (`@RestControllerAdvice`)
  and return the `ErrorResponse` shape. Throw `ResourceNotFoundException` (404) or
  `DuplicateResourceException` (409) from the service layer.
- Validate request DTOs with Bean Validation annotations + `@Valid` in controllers.

## Build & test commands

```bash
mvn clean compile     # compile
mvn test              # run all tests (15 tests)
# Run the app (MySQL must be running; set DB_USERNAME / DB_PASSWORD first):
DB_USERNAME=root DB_PASSWORD=<pwd> mvn spring-boot:run
```

## Testing approach

- Service layer: Mockito unit tests (`@ExtendWith(MockitoExtension.class)`,
  `@Mock` repository, `@InjectMocks` impl). See `EmployeeServiceImplTest`.
- Web layer: `@WebMvcTest` + `MockMvc`, service mocked with **`@MockitoBean`**
  (NOT the deprecated `@MockBean`), `@Import(SecurityConfig.class)` + `@WithMockUser`
  for the authenticated chain. See `EmployeeControllerTest`.

## Notes

- DB config is in `src/main/resources/application.properties` (MySQL,
  `ddl-auto=update`). Credentials are externalized as `${DB_USERNAME:root}` /
  `${DB_PASSWORD:}` — never hardcode secrets here; see `.env.example`.
- Security is HTTP Basic + stateless. An in-memory user is defined in
  `SecurityConfig` (default `admin` / `admin123`, override via
  `app.security.username` / `app.security.password`). IMPORTANT: because a
  `BCryptPasswordEncoder` bean exists, the in-memory user's password is encoded
  with it — Spring Boot's auto-generated default password does NOT work for login.
- Verified working end-to-end against live MySQL: full CRUD, auth (401), validation
  (400), duplicate (409), not-found (404), JPA auditing timestamps.
