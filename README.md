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
├── repository    Spring Data JPA repositories (Employee/Role/Department)
├── entity        JPA entities (Employee, Role, Department) + enums
├── dto           Request/response DTOs (EmployeeRequest, EmployeeResponse)
├── exception     GlobalExceptionHandler + custom exceptions + ErrorResponse
├── config        Infrastructure beans (JpaAuditingConfig, ...)
├── security      SecurityConfig (filter chain, in-memory user, BCrypt encoder)
└── util          Mappers / helpers (EmployeeMapper)
```

## Data Model

Three entities with the following relationships:

- **Department** `1 — ∞` **Employee** (an employee belongs to one department)
- **Role** `1 — ∞` **Employee** (an employee belongs to one role)

| Entity | Fields |
|--------|--------|
| **Employee** | `id`, `firstName`, `lastName`, `email` (unique), `phoneNumber`, `salary` (BigDecimal), `joiningDate`, `status` (enum), `role` (FK), `department` (FK), `createdAt`, `updatedAt` |
| **Department** | `id`, `departmentName`, `departmentCode` (unique) |
| **Role** | `id`, `roleName` (enum) |

Enums: `EmployeeStatus { ACTIVE, INACTIVE }`, `RoleName { ADMIN, HR, EMPLOYEE }`.

`createdAt` / `updatedAt` are populated automatically via Spring Data JPA auditing
(`@EnableJpaAuditing` + `AuditingEntityListener`). Money uses `BigDecimal`; enums
are persisted as strings (`@Enumerated(EnumType.STRING)`).

## API Endpoints

Base path: `/api/v1/employees`

| Method   | Path     | Description            | Success |
|----------|----------|------------------------|---------|
| `POST`   | `/`      | Create an employee     | 201     |
| `GET`    | `/{id}`  | Get employee by id     | 200 / 404 |
| `GET`    | `/`      | List all employees     | 200     |
| `PUT`    | `/{id}`  | Update an employee     | 200 / 404 |
| `DELETE` | `/{id}`  | Delete an employee     | 204 / 404 |

All endpoints require authentication (**HTTP Basic**). A single in-memory user is
configured in `SecurityConfig`, defaulting to **`admin` / `admin123`** for local dev
(override with `app.security.username` / `app.security.password`). Note: a custom
`BCryptPasswordEncoder` bean is defined, so the in-memory user's password is encoded
with it — Spring Boot's auto-generated default password is **not** used.

Example:

```bash
curl -u admin:admin123 http://localhost:8080/api/v1/employees
```

An employee references existing `role` and `department` rows, so seed those first:

```sql
INSERT INTO roles (role_name) VALUES ('EMPLOYEE');
INSERT INTO departments (department_name, department_code) VALUES ('Engineering','ENG');
```

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

- **MySQL** — `localhost:3306/employee_management_system`
  (`createDatabaseIfNotExist=true`). Credentials come from env vars
  `DB_USERNAME` / `DB_PASSWORD` (see below) — no secrets are committed.
- **JPA** — `ddl-auto=update`, SQL logging on, `open-in-view=false`.
- **Logging** — app package at DEBUG, console pattern + file at `logs/`.
- **Security** — login user via `app.security.username` / `app.security.password`
  (default `admin` / `admin123`).

## Getting Started

Database credentials are read from environment variables (see [`.env.example`](.env.example)):

| Variable      | Purpose            | Local-dev fallback |
|---------------|--------------------|--------------------|
| `DB_USERNAME` | MySQL username     | `root`             |
| `DB_PASSWORD` | MySQL password     | _(empty)_          |

Set them before running (Spring Boot does not auto-load `.env`):

```bash
# PowerShell
$env:DB_USERNAME = "root"; $env:DB_PASSWORD = "your-password"

# bash
export DB_USERNAME=root DB_PASSWORD=your-password
```

Then:

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

Current suite: **15 tests, all passing.** Verified end-to-end against a live MySQL
instance (full CRUD + auth + validation + 404/409 error paths).
