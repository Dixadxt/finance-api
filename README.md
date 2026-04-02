# Finance Data Processing and Access Control Backend

A production-ready REST API for a finance dashboard system, built with **Spring Boot 3**, **Spring Security (JWT)**, **JPA**, and **MySQL**.

---

## Tech Stack

| Layer        | Technology                  |
|--------------|-----------------------------|
| Framework    | Spring Boot 3.2             |
| Language     | Java 17                     |
| Security     | Spring Security + JWT (JJWT)|
| Persistence  | Spring Data JPA + Hibernate |
| Database     | MySQL 8                     |
| Build Tool   | Maven                       |

---

## Assumptions Made

- A user can hold exactly one role at a time (VIEWER, ANALYST, or ADMIN).
- Analysts can create and update records, but only Admins can delete them. This reflects a realistic separation where data entry is decoupled from destructive operations.
- Deleting a record performs a **soft delete** (sets `deleted = true`) so data is never permanently lost.
- Deactivating a user (DELETE `/api/users/{id}`) also performs a soft operation — the user is marked inactive, not removed from the database.
- The `monthly/weekly trends` endpoints are restricted to ANALYST and ADMIN roles, since these represent deeper analytical insights beyond basic summary data that Viewers can access.
- JWT tokens expire after 24 hours (configurable).
- Three default users are seeded on startup for immediate testing (see below).

---

## Setup

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+

### 2. Database
```sql
CREATE DATABASE financedb;
```

### 3. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financedb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Run
```bash
mvn spring-boot:run
```

Server starts at **http://localhost:8080**

---

## Seeded Users (auto-created on startup)

| Role    | Email                  | Password     |
|---------|------------------------|--------------|
| ADMIN   | admin@finance.com      | admin123     |
| ANALYST | analyst@finance.com    | analyst123   |
| VIEWER  | viewer@finance.com     | viewer123    |

---

## Role Permissions Matrix

| Endpoint                        | VIEWER | ANALYST | ADMIN |
|---------------------------------|--------|---------|-------|
| POST `/api/auth/login`          | ✅     | ✅      | ✅    |
| GET `/api/records`              | ✅     | ✅      | ✅    |
| GET `/api/records/{id}`         | ✅     | ✅      | ✅    |
| POST `/api/records`             | ❌     | ✅      | ✅    |
| PUT `/api/records/{id}`         | ❌     | ✅      | ✅    |
| DELETE `/api/records/{id}`      | ❌     | ❌      | ✅    |
| GET `/api/dashboard/summary`    | ✅     | ✅      | ✅    |
| GET `/api/dashboard/categories` | ✅     | ✅      | ✅    |
| GET `/api/dashboard/recent`     | ✅     | ✅      | ✅    |
| GET `/api/dashboard/trends/*`   | ❌     | ✅      | ✅    |
| GET `/api/users`                | ❌     | ❌      | ✅    |
| POST `/api/users`               | ❌     | ❌      | ✅    |
| PUT `/api/users/{id}`           | ❌     | ❌      | ✅    |
| DELETE `/api/users/{id}`        | ❌     | ❌      | ✅    |

---

## API Reference

### Authentication

#### `POST /api/auth/login`
```json
// Request
{
  "email": "admin@finance.com",
  "password": "admin123"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "Admin User",
  "email": "admin@finance.com",
  "role": "ADMIN"
}
```

Use the token in all subsequent requests:
```
Authorization: Bearer <token>
```

---

### Users (ADMIN only)

#### `GET /api/users`
Returns list of all users.

#### `GET /api/users/{id}`
Returns a specific user.

#### `POST /api/users`
```json
{
  "email": "newuser@finance.com",
  "password": "securepass",
  "name": "New User",
  "role": "ANALYST"
}
```

#### `PUT /api/users/{id}`
```json
{
  "name": "Updated Name",
  "role": "VIEWER",
  "active": true
}
```

#### `DELETE /api/users/{id}`
Deactivates the user (soft delete). Returns `204 No Content`.

---

### Financial Records

#### `GET /api/records`
Supports optional query parameters:

| Param      | Type     | Example              |
|------------|----------|----------------------|
| `type`     | Enum     | `INCOME` / `EXPENSE` |
| `category` | String   | `rent`               |
| `from`     | ISO date | `2025-01-01`         |
| `to`       | ISO date | `2025-12-31`         |

#### `GET /api/records/{id}`

#### `POST /api/records` *(ANALYST or ADMIN)*
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2025-04-01",
  "notes": "April salary"
}
```

#### `PUT /api/records/{id}` *(ANALYST or ADMIN)*
Any field is optional — only provided fields are updated.
```json
{
  "amount": 5500.00,
  "notes": "April salary revised"
}
```

#### `DELETE /api/records/{id}` *(ADMIN only)*
Soft-deletes the record. Returns `204 No Content`.

---

### Dashboard

#### `GET /api/dashboard/summary`
```json
{
  "totalIncome": 15000.00,
  "totalExpenses": 8200.00,
  "netBalance": 6800.00
}
```

#### `GET /api/dashboard/categories`
```json
[
  { "category": "Rent", "type": "EXPENSE", "total": 3000.00 },
  { "category": "Salary", "type": "INCOME", "total": 15000.00 }
]
```

#### `GET /api/dashboard/recent?limit=10`
Returns the most recent N financial records.

#### `GET /api/dashboard/trends/monthly?year=2025` *(ANALYST or ADMIN)*
```json
[
  { "month": "JANUARY", "income": 5000.00, "expense": 2000.00 },
  { "month": "FEBRUARY", "income": 4800.00, "expense": 3200.00 }
]
```

#### `GET /api/dashboard/trends/weekly` *(ANALYST or ADMIN)*
Returns income vs expense for the past 4 weeks.

---

## Error Responses

All errors follow a consistent structure:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this action",
  "details": null,
  "timestamp": "2025-04-01T10:30:00"
}
```

Validation errors include field-level detail:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "amount": "Amount must be positive",
    "date": "must not be null"
  },
  "timestamp": "2025-04-01T10:30:00"
}
```

---

## Project Structure

```
src/main/java/com/example/financeapi/
├── FinanceApiApplication.java
├── config/
│   ├── SecurityConfig.java        # JWT filter chain + role-based URL rules
│   └── DataInitializer.java       # Seeds default users on startup
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── FinancialRecordController.java
│   └── DashboardController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── FinancialRecordService.java
│   └── DashboardService.java      # Analytics + @PreAuthorize for analyst-only endpoints
├── repository/
│   ├── UserRepository.java
│   ├── FinancialRecordRepository.java  # Custom JPQL aggregate queries
│   └── FinancialRecordSpecification.java  # Dynamic filter builder
├── model/
│   ├── User.java                  # Roles: VIEWER, ANALYST, ADMIN
│   └── FinancialRecord.java       # Types: INCOME, EXPENSE; soft-delete support
├── dto/
│   └── Dtos.java                  # All request/response DTOs
├── security/
│   ├── JwtUtils.java              # Token generation + validation
│   ├── JwtAuthFilter.java         # Per-request JWT extraction
│   └── UserDetailsServiceImpl.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── ConflictException.java
    └── GlobalExceptionHandler.java  # Centralised error formatting
```

---

## Design Decisions & Tradeoffs

- **Two-layer access control**: URL-level rules in `SecurityConfig` handle broad role gating; `@PreAuthorize` in `DashboardService` handles fine-grained method-level restrictions (e.g., analyst-only trends). This makes each layer easy to reason about independently.
- **Soft deletes everywhere**: Both users and financial records are soft-deleted. This preserves referential integrity and audit history.
- **JPA Specification for filtering**: Dynamic multi-parameter filtering on records uses the Specification pattern instead of a growing list of repository method signatures.
- **Seeded users**: Three role archetypes are auto-seeded on startup so the API is immediately testable without any manual setup.
- **BigDecimal for money**: All monetary values use `BigDecimal` (never `double`) to avoid floating-point precision issues.
