# Employee Management System

A Spring Boot REST API for managing employee records, built with a clean layered
architecture (**Controller → Service → Repository**).

## Tech Stack

| Component   | Version / Choice                     |
|-------------|--------------------------------------|
| Language    | Java 21                              |
| Framework   | Spring Boot 3.4.1                    |
| Build       | Maven                               |
| Database    | MySQL                               |
| Security    | Spring Security (HTTP Basic, stateless) |
| Persistence | Spring Data JPA / Hibernate         |
| Utilities   | Lombok, Bean Validation, DevTools   |

## Architecture

Request flow is strictly layered and never skips a layer:

```
Controller  →  Service (interface)  →  Service Impl  →  Repository  →  Database
```

- Controllers depend only on **service interfaces** — they never touch repositories.
- Repositories are accessed **only** from the `service.impl` layer.
- Business logic lives in `service.impl`, not in controllers.

### Package structure (`com.ashil.ems`)

```
com.ashil.ems
├── EmployeeManagementSystemApplication.java
├── controller    REST endpoints (EmployeeController)
├── service       Service interfaces (EmployeeService)
│   └── impl      Service implementations (EmployeeServiceImpl)
├── repository    Spring Data JPA repositories (EmployeeRepository)
├── entity        JPA entities (Employee)
├── dto           Request/response DTOs (EmployeeRequest, EmployeeResponse)
├── exception     GlobalExceptionHandler + custom exceptions + ErrorResponse
├── config        Infrastructure configuration beans
├── security      SecurityConfig (filter chain, BCrypt encoder)
└── util          Mappers / helpers (EmployeeMapper)
```

## API Endpoints

Base path: `/api/v1/employees`

| Method   | Path     | Description            | Success |
|----------|----------|------------------------|---------|
| `POST`   | `/`      | Create an employee     | 201     |
| `GET`    | `/{id}`  | Get employee by id     | 200 / 404 |
| `GET`    | `/`      | List all employees     | 200     |
| `PUT`    | `/{id}`  | Update an employee     | 200 / 404 |
| `DELETE` | `/{id}`  | Delete an employee     | 204 / 404 |

All endpoints require authentication (HTTP Basic). On startup Spring Security
prints a generated dev password, or configure your own users in `SecurityConfig`.

### Error handling

A global `@RestControllerAdvice` (`GlobalExceptionHandler`) returns a consistent
`ErrorResponse` JSON for:

- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- Validation failures → 400 (with per-field messages)
- Any other exception → 500

### Null-safety

`getById` returns `Optional<EmployeeResponse>` from the service. The controller
resolves it with `.map(ResponseEntity::ok).orElseThrow(...)` so values are never
dereferenced directly — avoiding `NullPointerException`.

## Configuration

Edit [`src/main/resources/application.properties`](src/main/resources/application.properties):

- **MySQL** — defaults to `localhost:3306/employee_management_system`
  (`createDatabaseIfNotExist=true`), username/password `root`/`root`.
  **Update these to match your local MySQL before running.**
- **JPA** — `ddl-auto=update`, SQL logging on, `open-in-view=false`.
- **Logging** — app package at DEBUG, console pattern + file at `logs/`.

## Getting Started

```bash
# Build
mvn clean compile

# Run tests
mvn test

# Run the app
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Testing

- **`EmployeeServiceImplTest`** — Mockito unit tests for the service layer
  (CRUD, duplicate/not-found paths, `Optional` behaviour).
- **`EmployeeControllerTest`** — `@WebMvcTest` + `MockMvc` web-layer tests with a
  mocked service (`@MockitoBean`), covering status codes, validation, and 404.

Current suite: **14 tests, all passing.**
