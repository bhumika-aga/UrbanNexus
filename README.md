# UrbanNexus — Residential Operations Platform

UrbanNexus is a full-stack residential complex management system built with Spring Boot 3 and React 19. It covers the complete operational lifecycle of a residential community: resident and technician management, amenity and technician booking, payment tracking with 18% GST, and an administrative audit trail.

---

## Architecture

### Backend: Spring Boot 3 + Java 21

The backend is a RESTful JSON API with JWT-based stateless authentication. Key layers:

- **Controller** — validates role-based access, maps HTTP requests to service calls
- **Service** — contains all business logic (`@Transactional` boundary guards multi-step operations)
- **Repository** — Spring Data JPA interfaces; cross-DB native queries use ANSI SQL
- **Security** — `JwtAuthenticationFilter` extracts claims from every request; `UserPrincipal` carries `role`, `residentId`, `techId`
- **Model** — JPA entities with a `@PrePersist` lifecycle hook on `Payment` that applies 18% GST before insert

### Frontend: React 19 + Vite + TypeScript

- Material UI (MUI) for the component library
- Axios with a JWT interceptor for authenticated requests
- React Router for role-based view routing (Admin dashboard, Resident portal, Technician portal)

### Database Strategy

| Profile | Database | Purpose |
|---------|----------|---------|
| `dev` (default for local) | H2 file-based (`urbannexus_db.mv.db`) with `MODE=MySQL` | Local development — no external DB required |
| (no profile / production) | MySQL | Production deployment |

Both profiles run `schema.sql` and `data.sql` on startup via Spring SQL initialization. The H2 MySQL compatibility mode ensures the same DDL and queries run identically on both engines.

---

## Business Logic

### Roles

| Role | Capabilities |
|------|-------------|
| `SuperAdmin` | Full CRUD on residents/technicians/amenities; admin dashboard; process overdue payments |
| `Resident` | View and pay own dues; book technicians and amenities; view booking history |
| `Technician` | View own task assignments; update task status; toggle availability |

### Booking Flow

1. Resident requests a technician booking (`skill`, `slot`, `assign_date`)
2. System finds the first available technician with matching skill who has no conflicting slot on that date
3. A `Payment` record is created with base price — `@PrePersist` automatically applies 18% GST
4. An assignment record is created in `technician_management` linking resident, technician, and payment
5. The response returns `total_with_gst` (GST-inclusive cost), `trans_no`, and `assignment_id`

Amenity booking follows the same pattern: capacity validation → payment with GST → booking record.

### Payment Lifecycle

```
Pending → Paid       (resident pays via /api/payments/{trans_no}/pay)
Pending → Overdue    (admin triggers /api/admin/process-overdue — marks payments >30 days old)
```

### GST via @PrePersist

The `Payment` entity applies an 18% GST multiplier before every insert:

```java
@PrePersist
public void calculateGST() {
    this.cost = this.cost.multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);
    if (this.paymentDate == null) this.paymentDate = LocalDateTime.now();
}
```

Seed data in `data.sql` stores costs at face value (pre-GST); the hook fires on every `save()`.

### Audit Trail

All critical mutations (resident/technician add/delete, task status changes) are written to `audit_log` via `AuditService`. The MySQL `LogResidentDeletion` trigger provides a DB-level fallback for direct SQL deletes. The H2 dev profile handles this via the Java service layer.

---

## Running Locally

### Prerequisites

- JDK 21
- Maven 3.8+ (or use the project's `mvnw` wrapper)
- Node.js 18+

### Backend (H2 dev mode)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The H2 database file (`urbannexus_db.mv.db`) is created automatically in `backend/`. The H2 web console is available at `http://localhost:8080/h2-console`.

On first start, `schema.sql` creates all tables and `data.sql` seeds 26 residents, 16 technicians, 5 amenities, and pricing.

### Backend (MySQL production)

```bash
# Set environment variables or update application.yml
export DB_HOST=localhost DB_NAME=UrbanNexus DB_USER=root DB_PASSWORD=yourpassword
export JWT_SECRET=your-base64-secret

cd backend
mvn spring-boot:run
```

Run `schema-mysql.sql` manually in MySQL to create the stored procedures (Spring SQL init does not support the `DELIMITER` syntax required for MySQL procedures).

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173` and proxies API calls to `http://localhost:8080`.

---

## API Reference

Import `UrbanNexus_Postman_Collection.json` into Postman. The Login request auto-saves the JWT to `{{jwt_token}}`.

### Credentials

| Role | Username | Password |
|------|----------|----------|
| SuperAdmin | `admin` | `Bhumika@1234` |
| Resident | `jane_s` | `pwd123#` |
| Technician | `tech_tom` | `pwd123#` |

All resident accounts follow `<firstname>_<initial>` pattern; technician accounts follow `tech_<firstname>`. Default password for auto-provisioned accounts is `pwd123#`.

### Endpoints

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/login` | Public | Login; returns JWT |
| GET | `/api/profile/me` | Any | Get own profile |
| PUT | `/api/profile/update` | Any | Update own contact info |
| POST | `/api/residents` | SuperAdmin | Add resident + login account |
| GET | `/api/residents/me/dues` | Resident | View pending/overdue invoices |
| GET | `/api/residents/me/bookings` | Resident | View own bookings |
| DELETE | `/api/residents/{id}` | SuperAdmin | Delete resident + cascade |
| POST | `/api/technicians` | SuperAdmin | Add technician + login account |
| GET | `/api/technicians/me` | Technician | Get own profile |
| GET | `/api/technicians/me/tasks` | Technician | Get assigned tasks |
| PUT | `/api/technicians/tasks/{id}/status` | Technician | Update task status |
| PUT | `/api/technicians/me/availability` | Technician | Toggle availability |
| DELETE | `/api/technicians/{id}` | SuperAdmin | Remove technician |
| GET | `/api/amenities` | Any | List all amenities |
| POST | `/api/amenities` | SuperAdmin | Add amenity |
| PUT | `/api/amenities/{id}` | SuperAdmin | Update amenity |
| DELETE | `/api/amenities/{id}` | SuperAdmin | Delete amenity |
| POST | `/api/bookings/technician` | Resident | Book a technician |
| POST | `/api/bookings/amenity` | Resident | Book an amenity |
| POST | `/api/payments/{trans_no}/pay` | Resident, SuperAdmin | Mark payment as Paid |
| GET | `/api/admin/stats` | SuperAdmin | Dashboard stats |
| GET | `/api/admin/residents/search` | SuperAdmin | Search residents by name/block |
| GET | `/api/admin/technicians` | SuperAdmin | List all technicians |
| GET | `/api/admin/transactions` | SuperAdmin | Transaction ledger with filters |
| POST | `/api/admin/process-overdue` | SuperAdmin | Mark 30-day-old payments as Overdue |
| GET | `/api/admin/audit-logs` | SuperAdmin | System audit trail |
| GET | `/api/admin/assignments` | SuperAdmin | All technician assignments |
| GET | `/api/admin/amenities/bookings` | SuperAdmin | All amenity bookings |
| GET | `/api/admin/technicians/bookings` | SuperAdmin | All technician service history |

### Booking Request Bodies

**Book Technician** (`POST /api/bookings/technician`):
```json
{
  "skill": "Plumber",
  "slot": 1,
  "assign_date": "2026-06-10"
}
```

**Book Amenity** (`POST /api/bookings/amenity`):
```json
{
  "amenity_id": 2,
  "date": "2026-06-15",
  "slot": 1,
  "capacity_booked": 4
}
```

Slots are integers 1–3 (Morning / Afternoon / Evening).

---

## Project Structure

```
UrbanNexus/
├── backend/
│   ├── src/main/java/com/urbannexus/
│   │   ├── config/          SecurityConfig, JwtConfig
│   │   ├── controller/      AuthController, AdminController, ResidentController,
│   │   │                    TechnicianController, BookingController,
│   │   │                    AmenityController, PaymentController
│   │   ├── service/         AuthService, AdminService, ResidentService,
│   │   │                    TechnicianService, BookingService, AuditService,
│   │   │                    PaymentService
│   │   ├── repository/      JPA repositories (Spring Data)
│   │   ├── model/           JPA entities (Payment has @PrePersist GST hook)
│   │   ├── security/        JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal
│   │   └── dto/             Request/Response DTOs
│   └── src/main/resources/
│       ├── application.yml          Production (MySQL) config
│       ├── application-dev.yml      Development (H2) config
│       ├── schema.sql               Universal DDL (H2 + MySQL compatible)
│       ├── data.sql                 Seed data (residents, technicians, pricing)
│       └── schema-mysql.sql         MySQL stored procedures (run manually)
├── frontend/
│   └── src/
│       ├── views/           AdminDashboard, ResidentPortal, TechnicianPortal, Login
│       ├── api/             axiosClient.ts with JWT interceptor
│       └── components/      Shared MUI components
└── UrbanNexus_Postman_Collection.json
```

---

## Key Design Decisions

**No stored procedures for booking logic** — The `AutoBookTechnician` and `AutoBookAmenity` MySQL procedures exist for direct DB use but are not called by the Java service layer. Booking logic lives in `BookingService.java` inside a `@Transactional` boundary, which is portable across H2 and MySQL and provides proper exception handling.

**`@PrePersist` for GST** — Rather than multiplying cost at each call site, the `Payment` entity applies GST exactly once before every insert. This eliminates double-GST bugs and keeps pricing logic in one place.

**H2 MySQL mode** — `MODE=MySQL` in the H2 JDBC URL enables MySQL-compatible syntax (case-insensitive identifiers, `CONCAT`, etc.) so the same `schema.sql` and queries work in both dev and production without modification.

**JWT in query layer** — `UserPrincipal` carries `residentId` and `techId` extracted from the JWT, so controllers never need to query the admin table again to determine who is making a request.

---

© 2026 Bhumika Agarwal — UrbanNexus
