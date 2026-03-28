 # Task Management System (Clean Architecture)

Hệ thống quản lý công việc (Task Management) xây dựng trên **Clean Architecture**, cung cấp RESTful API cho phân quyền, quản lý dự án và theo dõi tiến độ công việc.

---

## 📌 Overview

Dự án áp dụng **Clean Architecture** và **Domain-Driven Design (DDD)** thực tế:
- Business logic nằm trong **Domain Entities** (Task, Project, User)
- Tách biệt rõ ràng giữa **Application** (Use Cases) và **Infrastructure** (Database, Security)
- **CQRS Pattern** cho Task Repository (read/write separation)
- **Kanban Board** với drag & drop và transaction-safe reordering

---

## ⭐ Highlights

| Feature | Implementation |
|---------|----------------|
| **Clean Architecture** | 4 layers: Domain → Application → Infrastructure → Interface |
| **CQRS** | `TaskQueryRepository` (read) / `TaskCommandRepository` (write) |
| **DDD** | Business logic trong Entity (`start()`, `complete()`, `assignTo()`) |
| **Kanban Drag & Drop** | Transaction-safe reordering với `TaskOrderService` |
| **Security** | JWT + Google OAuth2, Role-based (OWNER / MEMBER) |
| **Email Verification** | Resend API, 24h token expiration, 60s cooldown |

---

## 🧰 Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security (JWT + OAuth2) |
| **Data** | JPA / Hibernate, MySQL |
| **Email** | Resend API (verification + password reset) |
| **Build** | Maven |
| **Docs** | Markdown (API_DOCS.md) |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         Interface Layer                 │
│    (Controllers, DTOs, Mappers)         │
├─────────────────────────────────────────┤
│        Application Layer                │
│   (Use Cases, Repository Interfaces)    │
│     CQRS: TaskQuery/CommandRepo         │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│  (Entities, Domain Services, Enums)     │
│  Task.start(), TaskOrderService         │
├─────────────────────────────────────────┤
│       Infrastructure Layer              │
│  (JPA Repositories, Security, Config)   │
└─────────────────────────────────────────┘
```

### Domain Layer
- **Entities**: `Task`, `Project`, `User`, `ProjectMember`, `VerificationToken`
- **Domain Services**: 
  - `Task.TaskOrderService`, `Task.TaskAssignerService`
  - `Email.EmailService`
- **Business Methods**: `start()`, `complete()`, `validateMove()`, `assignTo()`

### Application Layer
- **Use Cases**: `CreateTaskUseCase`, `MoveTaskUseCase`, `AssignTaskUseCase`
- **CQRS**: `TaskQueryRepository` (read ops) / `TaskCommandRepository` (write ops)

---

## 🚀 Features

### 🔐 Authentication
- JWT Bearer Token (`Authorization: Bearer <token>`)
- Google OAuth2 Login (auto-verified)
- Email Verification (Resend API, 24h token, 60s cooldown)
- Custom Exception Handling (401/403)

### 📁 Project Management
- **OWNER**: Full control, invite members, delete project
- **MEMBER**: View, create/move/assign tasks
- Auto-cascade delete (tasks + members)

### 👥 Member System
- **Invite**: PENDING → ACCEPT / REJECT
- **Role**: OWNER / MEMBER
- **Status**: ACCEPTED mới được thao tác

### 📋 Task Management
- **Status**: TODO → IN_PROGRESS → DONE (state transitions protected)
- **Kanban**: Drag & drop với position normalization
- **Assign**: Giao task cho member (hoặc unassign)

---

## 🛠️ Run Project

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+

### Database Setup
```sql
CREATE DATABASE task_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Configuration
```src/main/resources/
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_management
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Resend Email API
RESEND_API_KEY=your-resend-api-key
app.email.from=your-verified-domain@example.com
app.frontend.url=http://localhost:5173

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Google OAuth2 (optional)
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```

### Run
```bash
# Clone project
git clone <repo-url>
cd Task-Management_Clean-Architecture

# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run jar
java -jar target/task-management-*.jar
```

### Default Port
- Application: `http://localhost:8080`
- API Base: `/api`

---

## 📖 API Documentation

Chi tiết endpoints, request/response, business rules:

📄 **[API_DOCS.md](./API_DOCS.md)**

### Key Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register user (sends verification email) |
| POST | `/api/auth/login` | JWT Login |
| POST | `/api/auth/login/google` | Google OAuth2 Login |
| GET | `/api/auth/verify?token=xxx` | Verify email |
| POST | `/api/auth/resend-verification` | Resend verification email (60s cooldown) |
| POST | `/api/projects` | Create project |
| POST | `/api/projects/{id}/tasks` | Create task |
| POST | `/api/projects/{id}/tasks/{taskId}/move` | Move task (Kanban) |
| POST | `/api/projects/{id}/tasks/{taskId}/assign` | Assign task |

---

## 📁 Project Structure

```
src/main/java/com/example/task_management/
├── domain/                 # Business logic
│   ├── entities/          # Task, Project, User, VerificationToken
│   ├── services/          # Task.*, Email.*, Project.*
│   └── enums/             # TaskStatus, InvitationStatus
├── application/           # Use cases
│   ├── usecases/          # Interfaces
│   ├── usecases/impl/     # Implementations
│   ├── dto/              # Request/Response
│   └── repositories/     # Interfaces (Ports)
├── infrastructure/        # Adapters
│   ├── persistence/       # JPA Entities & Repos
│   └── security/          # JWT, OAuth2
└── interfaces/           # Controllers
    └── controllers/       # REST API
```

---

## 🔒 Security Best Practices

- ✅ Không log sensitive data (email, PII)
- ✅ JWT tokens với expiration
- ✅ Role-based authorization (OWNER/MEMBER)
- ✅ Transaction-safe cho multi-task operations
- ✅ Input validation với Jakarta Validation

---

## � License

MIT License
