# Virtual Bank System — Microservices

A Java Spring Boot microservices banking system with a BFF layer and a WSO2 API Gateway in front. Built by a 2-person team.

## Architecture

```
Frontend (Postman / Web / Mobile)
        │
        ▼
  WSO2 API Gateway        (not yet built)
        │
        ▼
   BFF Service            (aggregates the 3 services below)   — port 8080
        │
   ┌────┼────────────┐
   ▼    ▼             ▼
 User  Account   Transaction
 8081   8082         8083

 Logging Service (Kafka consumer, dumps request/response logs) — port 8084
```

Every service is independent and owns its own database (database-per-service pattern) — no service ever reaches into another's tables directly. Cross-service data needs go through REST calls, not shared SQL.

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Build tool | Maven (via Maven Wrapper — no system-wide Maven needed) |
| Database | PostgreSQL 16, one container, one database per service |
| Schema management | JPA/Hibernate `ddl-auto: update` — **entities are the single source of truth for schema**, not manual SQL |
| Boilerplate reduction | Lombok |
| Password hashing | `spring-security-crypto` (`BCryptPasswordEncoder`) — **not** the full Spring Security starter, since WSO2 owns auth at the gateway |
| Containerization | Docker Desktop / Docker Compose |
| IDE | IntelliJ IDEA Ultimate |


## Prerequisites

Before doing anything, confirm you have:
- **JDK 21+** — `java -version`
- **Git** — `git --version`
- **Docker Desktop**, installed and *running* — `docker info` should show a populated `Server:` section, not an error
- **IntelliJ IDEA** (Ultimate recommended — free student license available, gives native Spring Boot run configs + a built-in DB tool window)

You do **not** need a system-wide Maven install — this repo includes a Maven Wrapper (`mvnw.cmd`) that downloads and pins the exact right version automatically.

## First-Time Setup

```powershell
git clone https://github.com/1Ferwiz/vbank-microservices.git
cd vbank-microservices
```

1. Copy the environment template and set your own local password:
   ```powershell
   copy .env.example .env
   ```
   Open `.env` and set a `POSTGRES_PASSWORD` of your choice. This file is gitignored — never commit it.

2. Start Postgres (creates 4 isolated databases automatically on first boot):
   ```powershell
   docker compose up -d
   ```

3. Verify all 4 databases exist:
   ```powershell
   docker exec -it vbank-postgres psql -U vbank_admin -l
   ```
   You should see `users_db`, `accounts_db`, `transactions_db`, `logs_db`.

4. Open the project root folder in IntelliJ (`File → Open`, select the `vbank-microservices` folder — the one containing this README and `pom.xml`).

## Running a Service

Use the provided script — it reads credentials straight from your `.env` so you never type them manually, and it always stays correct even after a database reset:

```powershell
.\run-service.ps1 -Module user-service
```

Swap `user-service` for whichever module you're working on.

To just confirm something compiles without running it:
```powershell
.\mvnw.cmd -pl <module-name> clean compile
```
Always use `clean compile` (not plain `compile`) when you genuinely need to confirm a build result — Maven's incremental compiler can report "nothing to compile" in misleading ways otherwise.

## Repository Structure

Monorepo, Maven multi-module. One root `pom.xml` (an aggregator — no application code, just the shared Spring Boot version and the list of modules), and one folder per microservice:

```
vbank-microservices/
├── pom.xml                        ← parent POM: Spring Boot version, Java version, module list
├── docker-compose.yml             ← single Postgres container, 4 databases
├── docker/postgres/init/          ← DB init script (creates the 4 databases + pgcrypto)
├── run-service.ps1                ← run any service with correct local env vars
├── .env.example / .env            ← local Postgres credentials (.env is gitignored)
├── user-service/
├── account-service/                
├── transaction-service/           
├── bff-service/                   
└── logging-service/               
```

### Package structure inside every service (the template — follow this exactly in every new module)

```
com.ejada.vbank.<servicename>
├── entity        JPA classes — the single source of truth for DB schema
├── repository    Spring Data JPA interfaces
├── dto           Request/response objects — NEVER expose entities directly over the API
├── service       Business logic (validation rules, password hashing, etc.)
├── controller    Thin REST layer — no business logic, just delegates to service
├── exception     Custom exceptions + one GlobalExceptionHandler per service
└── config        Bean definitions (e.g. BCryptPasswordEncoder)
```

## Database Conventions (apply to every entity, every service)

- **Tables:** snake_case, plural — `users`, `accounts`, `transactions`
- **Columns:** snake_case, auto-derived from Java camelCase field names (Hibernate does this automatically, no config needed)
- **Entity classes:** PascalCase, singular — `User`, `Account`, `Transaction`
- **Primary keys:** always `id`, type `UUID`, via `@GeneratedValue(strategy = GenerationType.UUID)`
- **Cross-service references:** suffix `Id`, plain UUID column, never a real `@ManyToOne`/physical foreign key (the referenced table lives in a different database) — e.g. `userId` inside `Account`
- **Timestamps:** every entity gets `createdAt`/`updatedAt` via `@CreationTimestamp`/`@UpdateTimestamp`
- **Money fields:** always `BigDecimal`, mapped to `NUMERIC(19,4)` — never `float`/`double` for currency
- **Constraints are explicit, always:** every `@Column` states `nullable`, `unique`, `length` where relevant — never left to defaults
- **`ddl-auto: update` only ever adds columns, never removes them.** If you rename or delete a field, manually drop the orphaned column afterward via the IntelliJ Database tool — don't let dead columns accumulate.
- **Important:** since Hibernate generates `INSERT`s lazily inside a transaction, reading a `@CreationTimestamp` field immediately after `save()` can return `null`. Use `saveAndFlush()` instead of `save()` whenever you need the generated value (id, timestamps) right away in the same method.

## Database Schema (Reference)

Since schema is generated from entities (`ddl-auto: update`), the entity is always the final source of truth — but here's the agreed plan every entity should follow, so we all start from the same page.

### `users_db.users` — ✅ implemented

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, generated |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### `accounts_db.accounts` — Impelementd

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, generated |
| user_id | UUID | NOT NULL — cross-service reference to `users.id`, **no physical FK** (different database) |
| account_number | VARCHAR(20) | NOT NULL, UNIQUE |
| account_type | VARCHAR(20) | NOT NULL — `SAVINGS` / `CHECKING` today; `SYSTEM` reserved for the optional interest job if we get to it |
| balance | NUMERIC(19,4) | NOT NULL, DEFAULT 0 — never `float`/`double` for money |
| status | VARCHAR(20) | NOT NULL, DEFAULT `ACTIVE` — `ACTIVE` / `INACTIVE` |
| last_activity_at | TIMESTAMP | NOT NULL — updated on every transaction; the hourly job reads this to flag idle (>24h) accounts as `INACTIVE` |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### `transactions_db.transactions` — Implemented

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK, generated |
| from_account_id | UUID | NOT NULL — cross-service reference, no physical FK |
| to_account_id | UUID | NOT NULL — cross-service reference, no physical FK |
| amount | NUMERIC(19,4) | NOT NULL |
| description | VARCHAR(255) | nullable |
| status | VARCHAR(20) | NOT NULL, DEFAULT `INITIATED` — `INITIATED` / `SUCCESS` / `FAILED` |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL — changes when status flips |

### `logs_db.log_dump` — Implemented

| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK — plain sequence is fine here, no UUID needed for internal logs |
| message | TEXT | NOT NULL — the escaped JSON request/response body |
| message_type | VARCHAR(20) | NOT NULL — `Request` / `Response` |
| event_time | TIMESTAMP | NOT NULL — the `dateTime` from the Kafka message |
| received_at | TIMESTAMP | NOT NULL, DEFAULT now() — when the consumer actually persisted it (lets us spot Kafka lag) |

## Lombok Usage

Used consistently across entities and DTOs to cut boilerplate:
- **Entities:** `@Getter` + selective `@Setter` only on fields that should be mutable (never blanket `@Data` — some fields like `id`/`createdAt` are intentionally immutable after creation). Keep any custom constructor that enforces a business rule (e.g. "only these 5 fields can be set at creation") hand-written — don't let `@AllArgsConstructor` undo that protection.
- **Request DTOs:** `@Getter @Setter` on everything.
- **Response DTOs:** `@Getter @AllArgsConstructor`, no setters — responses are immutable once built.

## Database Schema (per service)

Each table below is generated automatically by Hibernate from its entity (`ddl-auto: update`) — this is the **plan**, not something to hand-write in SQL. `users` is already implemented exactly as shown. The other three are suggestions for whoever builds that service — adjust if the real requirements need it, but keep the naming/type conventions above.

### `users_db.users` — ✅ implemented

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### `accounts_db.accounts` — implemented

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| user_id | UUID | NOT NULL — references `users.id`, no physical FK (different database) |
| account_number | VARCHAR(20) | NOT NULL, UNIQUE |
| account_type | VARCHAR(20) | NOT NULL — `SAVINGS` / `CHECKING` (leave room for `SYSTEM` later if the interest job gets added) |
| balance | NUMERIC(19,4) | NOT NULL, DEFAULT 0 — never float/double for money |
| status | VARCHAR(20) | NOT NULL, DEFAULT `ACTIVE` — `ACTIVE` / `INACTIVE` |
| last_activity_at | TIMESTAMP | NOT NULL — updated on every transaction touching this account; the hourly job checks this to mark accounts inactive after 24h idle |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### `transactions_db.transactions` — suggested

| Column | Type | Constraints |
|---|---|---|
| id | UUID | PK |
| from_account_id | UUID | NOT NULL |
| to_account_id | UUID | NOT NULL |
| amount | NUMERIC(19,4) | NOT NULL |
| description | VARCHAR(255) | nullable |
| status | VARCHAR(20) | NOT NULL, DEFAULT `INITIATED` — `INITIATED` / `SUCCESS` / `FAILED` |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL — changes when status flips |

### `logs_db.log_dump` — suggested

| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK — plain sequence is fine here, no need for UUID on internal logs |
| message | TEXT | NOT NULL — the escaped JSON request/response body from Kafka |
| message_type | VARCHAR(20) | NOT NULL — `Request` / `Response` |
| event_time | TIMESTAMP | NOT NULL — the `dateTime` from the Kafka message itself |
| received_at | TIMESTAMP | NOT NULL, DEFAULT now() — when the consumer actually persisted it, lets us spot Kafka lag |

## Git Workflow

- **Never push directly to `main`.**
- Branch naming: `feature/<service>-<short-description>`, e.g. `feature/account-service-crud`, `feature/transaction-service-transfer`.
- Open a Pull Request into `main` when a piece of work is complete and builds cleanly (`clean compile` passes, endpoints tested in Postman).
- The other teammate reviews and approves before merging.
- Keep commits scoped and descriptive — one logical change per commit, same style used throughout `user-service`'s history (check `git log` there for examples).

## How to Add a New Microservice

1. **File → New → Module → Spring Boot**, inside the already-open project.
   - Group: `com.ejada.vbank`, Artifact: `<service-name>`, Package: `com.ejada.vbank.<servicename>`
   - JDK 21, Spring Boot version closest to `4.0.7`
   - Confirm the **Location** field points inside `vbank-microservices\<service-name>` before clicking Create.
2. Delete the auto-generated `HELP.md`, `.gitignore`, `.gitattributes`, and the module's own `mvnw`/`mvnw.cmd`/`.mvn` (we use one shared wrapper at root only).
3. Fix the module's `pom.xml` — its `<parent>` must point to our root `vbank-microservices` pom (not directly to `spring-boot-starter-parent`):
   ```xml
   <parent>
       <groupId>com.ejada.vbank</groupId>
       <artifactId>vbank-microservices</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <relativePath>../pom.xml</relativePath>
   </parent>
   ```
4. Add `<module><service-name></module>` to the root `pom.xml`'s `<modules>` list.
5. Add `spring-security-crypto` and Lombok manually if the service needs password hashing (Spring Initializr doesn't offer the lightweight crypto-only dependency as a checkbox).
6. Write `application.yml` — datasource pointing at this service's own database, `ddl-auto: update`, its own assigned port (see table below), actuator health endpoint exposed.
7. Build the vertical slice in order: entity → repository → DTOs → service (business logic) → custom exceptions → controller → `GlobalExceptionHandler`.
8. `.\mvnw.cmd -pl <service-name> clean compile` after every meaningful addition — confirm real success, don't assume.
9. Test every endpoint in Postman before considering it done — including failure cases (duplicates, validation, not-found), not just the happy path.

## Port Assignments

| Service | Port | Database |
|---|---|---|
| BFF Service | 8080 | (none — calls the others) |
| User Service | 8081 | `users_db` |
| Account Service | 8082 | `accounts_db` |
| Transaction Service | 8083 | `transactions_db` |
| Logging Service | 8084 | `logs_db` |
| Postgres (Docker) | 5434 | (host machine mapping, container itself uses 5432) |

## Team Ownership


- **User Service:** ✅ Done — entity, repository, DTOs, service layer, controller, global exception handler, all endpoints tested in Postman (register, login, get-by-id, validation, duplicate, wrong-password cases)
- - **Account Service:** Done-  entity, repository, DTOs, service layer (create/get-by-id/get-by-user), exception handler, and controller done for 3 of 4 endpoints. **`PUT /accounts/transfer` (balance update) still pending** — this is what Transaction Service will call for debit/credit during transfer execution. Not yet tested in Postman, not yet merged to `main`.
- **Transaction Service:** Done
- **BFF Service:** Done
- **Logging Service:** Done
