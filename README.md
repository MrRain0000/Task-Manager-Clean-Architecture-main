 # Task Management System (Clean Architecture)

Đây là hệ thống quản lý công việc (Task Management) được xây dựng dựa trên nguyên tắc **Clean Architecture**. Hệ thống cung cấp các RESTful API giúp người dùng phân quyền, quản lý dự án, mời thành viên nhóm và theo dõi tiến độ công việc một cách hiệu quả và bảo mật.

## 🚀 Tính năng chính (Core Features)

### 1. Xác thực & Phân quyền (Authentication & Authorization)
- Đăng ký và đăng nhập tài khoản hệ thống bằng Email/Password.
- Hỗ trợ đăng nhập thông qua **Google OAuth2**.
- Bảo vệ các endpoint API bằng **JWT (JSON Web Token)** (`Authorization: Bearer <TOKEN>`).
- Xử lý lỗi bảo mật tập trung bằng Custom Exception (như `JwtAuthEntryPoint`) để trả về JSON chuẩn khi không có quyền truy cập (Lỗi 401/403).

### 2. Quản lý Dự án (Project Management)
- Khởi tạo dự án mới: Người tạo tự động trở thành `OWNER` và tự động được phê duyệt (`ACCEPTED`).
- Xóa dự án (Chỉ dành cho quyền `OWNER`): Hệ thống tự động xóa liên đới (cascade delete) dữ liệu của Công việc (Tasks) và Thành viên (Members) để giữ sạch CSDL.

### 3. Thành viên & Lời mời (Project Invitations)
- Mời người dùng khác tham gia vào dự án (Chỉ `OWNER` mới có quyền mời).
- Quản lý trạng thái lời mời: Người được mời có thể xem danh sách lời mời (PENDING) và thực hiện Chấp nhận (ACCEPT) hoặc Từ chối (REJECT).
- Đảm bảo người dùng phải xác thực tài khoản (Verified) mới có thể tham gia dự án.

### 4. Quản lý Công việc (Task Management)
- Khởi tạo Task trong một Project cụ thể: Tự động gán trạng thái khởi tạo là `TODO`.
- Quản lý vị trí tự động: Task mới sinh ra sẽ tự động được gán `position` nối tiếp cuối danh sách.
- Phân luồng Status linh hoạt: Quản lý vòng đời trạng thái (`TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`).
- Kiểm tra quyền chặt chẽ: Các API liên quan tới Task yêu cầu Caller phải là thành viên chính thức (`ACCEPTED`) của Dự án.

#### 🎯 Kanban Board - Drag & Drop
- **Di chuyển Task linh hoạt**: Hỗ trợ kéo thả task trong cùng column hoặc giữa các column khác nhau.
- **Tự động cập nhật Position**: Khi di chuyển task, hệ thống tự động tính toán và cập nhật lại thứ tự các task còn lại (normalization).
- **Tự động chuyển Status**: Khi kéo task sang column khác, status tự động cập nhật theo column đích.
- **Bảo toàn dữ liệu**: Các thao tác reorder được thực hiện trong transaction để đảm bảo tính nhất quán.

#### 📋 Business Rules cho Task
- **State Transitions (Bảo vệ chuyển đổi trạng thái)**:
  - `start()`: Chỉ cho phép từ `TODO` → `IN_PROGRESS`
  - `complete()`: Chỉ cho phép từ `IN_PROGRESS` → `DONE`
  - `cancel()`: Không cho phép từ `DONE` → `CANCELLED`
- **Position Management**: Position là số nguyên liên tục, không có khoảng trống.
- **Authorization**: Chỉ thành viên `ACCEPTED` mới được di chuyển task.

## 🏗️ Kiến trúc & Thiết kế mã nguồn (Architecture)

Dự án tuân thủ nghiêm ngặt **Clean Architecture** để đảm bảo tính độc lập, dễ dàng bảo trì và mở rộng:
- **Domain Layer**: Chứa trực tiếp các thực thể (Entities) và các hàm thực thi business rule độc lập.
  - **Domain Entities**: `Task`, `Project`, `User`, `ProjectMember` với các business methods (`start()`, `complete()`, `validateMove()`, `increasePosition()`...)
  - **Domain Services**: `TaskOrderService` xử lý logic phức tạp về reordering task, tách biệt khỏi Use Case.
- **Application Layer**: Chứa Use Cases, các Interface (Ports) của Repositories, các DTOs phục vụ giao tiếp.
  - **CQRS Pattern**: Tách `TaskRepository` thành `TaskQueryRepository` (read) và `TaskCommandRepository` (write) để tối ưu và rõ ràng hóa trách nhiệm.
- **Infrastructure Layer**: Triển khai các tính năng tầng logic dưới như Database (Spring Data JPA), Bảo mật (Spring Security), kết nối tới Third-Party APIs.
- **Interface Layer**: Control dòng dữ liệu vào hệ thống (Controllers), nhận HTTP Requests và gọi trực tiếp xuống Application layer.

## 📝 Document API

Chi tiết về các URL, tham số, Headers và dữ liệu mẫu của các API, vui lòng xem tại file tài liệu:
👉 **[API_DOCS.md](./API_DOCS.md)**
