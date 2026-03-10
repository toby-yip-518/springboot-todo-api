# Secure Todo API

![Java](https://img.shields.io/badge/Java-21+-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![Spring Security](https://img.shields.io/badge/Security-SpringSecurity-green)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Swagger](https://img.shields.io/badge/API-Swagger-green)
![JUnit](https://img.shields.io/badge/Test-JUnit-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

A secure RESTful Todo API built with **Spring Boot**, demonstrating production-style backend architecture including JWT authentication, rate limiting, pagination, and comprehensive testing.

---

# Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Environment Variables](#environment-variables)
- [Future Improvements](#future-improvements)
- [License](#license)

---

# Features

## Authentication & Authorization

- User registration and login
- JWT access tokens + refresh token mechanism
- Stateless authentication using Spring Security
- Protected endpoints — users can only manage their own todos

---

## Todo Management

- Full CRUD: Create, Read, Update, Delete
- Pagination support
- Sorting support
- Filtering by completion status

---

## API Protection

- Rate limiting via **Bucket4j**
- Refresh token endpoint restricted for issuing new access tokens only

---

# Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 + |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT |
| Database | MySQL + Spring Data JPA |
| Rate Limiting | Bucket4j |
| Documentation | Swagger / OpenAPI |
| Testing | JUnit, Mockito, MockMvc, H2 |
| Build Tool | Maven |

---

# Architecture

The application follows a layered architecture separating request handling, business logic, and data persistence.


```
Client
↓
Controller → Handles HTTP requests/responses
↓
Service → Business logic & authorization checks
↓
Repository → Database interaction (Spring Data JPA)
↓
Database → MySQL (production) / H2 (tests)
```

DTOs are used throughout to separate API models from database entities.

---

## Project Structure

```
src/
├── main/
│   └── java/com/toby/todoapi/
│     ├── controller/ # REST controllers
│     ├── service/ # Business logic
│     ├── repository/ # Spring Data JPA repositories
│     ├── model/ # Database entities
│     ├── dto/ # Request/response models
│     ├── security/ # JWT filters & security configuration
│     ├── config/ # Application configuration (Swagger, rate limiting)
│     └── exception/ # Global exception handling
└── test/
    └── java/com/toby/todoapi/
    ├── service/ # Unit tests (Mockito)
    └── integration/ # Integration tests (MockMvc + H2)
```

---

# Prerequisites

- Java 21+
- MySQL 8+
- Maven 3.8+

---

# Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/toby-yip-518/springboot-todo-api.git
cd springboot-todo-api
```

**2. Create the database**
```sql
CREATE DATABASE todo_db;
```

**3. Configure `application.properties`**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

**4. Build and run**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Endpoints

### Auth

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| POST | `/api/auth/register` | ❌ | Register a new user |
| POST | `/api/auth/login` | ❌ | Login and receive JWT tokens |
| POST | `/api/auth/refresh` | Refresh Token | Generate new access token |

### Todos

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| GET | `/api/todos` | ✅ | Get all todos (paginated) |
| GET | `/api/todos/{id}` | ✅ | Get a single todo |
| POST | `/api/todos` | ✅ | Create a new todo |
| PUT | `/api/todos/{id}` | ✅ | Update a todo |
| DELETE | `/api/todos/{id}` | ✅ | Delete a todo |

**Query Parameters (GET /api/todos)**

| Param | Type | Description |
|-------|------|-------------|
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 10) |
| `sort` | string | Sort field (e.g. id, title) |
| `completed` | boolean | Filter by completion status |

**Example request:**
```bash
curl -X GET "http://localhost:8080/api/todos?page=0&size=5&completed=false" \
  -H "Authorization: Bearer <your_jwt_token>"
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

---

## Testing

The project includes both unit and integration tests.

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=TodoServiceTest

# Run only integration tests
mvn test -Dtest="*IntegrationTest"
```

### Unit Tests
Service layer tested in isolation using **Mockito** — no database or Spring context required.

### Integration Tests
Full request-to-database flow tested using **MockMvc** and an **H2 in-memory database**:
```
HTTP Request
→ Security Filter
→ Controller
→ Service
→ Repository
→ H2 Database
→ Response
```

---

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL connection string | `jdbc:mysql://localhost:3306/todo_db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `secret` |
| `JWT_SECRET` | JWT signing key | `mysupersecretkey` |
| `JWT_EXPIRATION` | Access token TTL (ms) | `86400000` (1 day) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) | `604800000` (7 days) |

---

## Future Improvements

- [ ] Docker + docker-compose setup
- [ ] Redis-backed distributed rate limiting
- [ ] Persistent refresh tokens (database-stored)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Role-based authorization (ADMIN / USER)

---

## Learning Focus

This project demonstrates:
- Secure REST API design patterns
- JWT authentication + refresh token flow
- Layered backend architecture with DTOs
- API rate limiting with Bucket4j
- Swagger/OpenAPI documentation
- Unit testing with Mockito
- Integration testing with MockMvc + H2

---

## License

This project is licensed under the [MIT License](LICENSE).
