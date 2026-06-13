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
repository     Spring Data JPA repositories
entity         JPA entities
dto            Request/response DTOs
exception      GlobalExceptionHandler, custom exceptions, ErrorResponse
config         Infrastructure config beans
security       SecurityConfig
util           Mappers / helpers
```

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
mvn test              # run all tests
mvn spring-boot:run   # run the app (needs MySQL configured)
```

## Testing approach

- Service layer: Mockito unit tests (`@ExtendWith(MockitoExtension.class)`,
  `@Mock` repository, `@InjectMocks` impl). See `EmployeeServiceImplTest`.
- Web layer: `@WebMvcTest` + `MockMvc`, service mocked with **`@MockitoBean`**
  (NOT the deprecated `@MockBean`), `@Import(SecurityConfig.class)` + `@WithMockUser`
  for the authenticated chain. See `EmployeeControllerTest`.

## Notes

- DB config is in `src/main/resources/application.properties` (MySQL,
  `ddl-auto=update`). Credentials default to `root`/`root` — adjust locally.
- Security is HTTP Basic + stateless; Spring prints a generated dev password on
  startup unless users are configured in `SecurityConfig`.
