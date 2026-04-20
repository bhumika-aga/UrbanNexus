# UrbanNexus: Enterprise Residential Operations Ecosystem

UrbanNexus is a high-bandwidth, full-stack ERP designed for premium residential complexes. It orchestrates the
mission-critical intersection of human resources (Technicians), infrastructure (Amenities), and community (Residents)
through a unified relational engine that prioritizes transactional integrity and a premium, modern user experience.

## 🏛 Technical Logic & Architecture

### 1. Persistence & Data Integrity (Cross-DB Engine)

The cornerstone of UrbanNexus is **Database Agnosticism**. To support seamless deployment across both MySQL and H2
environments, the system utilizes a normalized relational schema:

- **Universal DDL**: The schema scripts are optimized for compatibility with modern relational dialects, ensuring
  identical behavior across local development (MySQL) and standalone production (H2).
- **Persistent Production Hub**: In production mode, the system utilizes a **File-Based H2 Database** (
  `urbannexus_db.mv.db`). This allows the platform to maintain data persistence across restarts without requiring a
  dedicated external database server.
- **Boot Consistency**: The system utilizes Spring SQL Initialization (`schema.sql` and `data.sql`) to reconstruct the
  operational environment on every boot, ensuring a reliable state for field operations.

### 2. Business Engine (Spring Boot 3.5 & Java 17)

The backend acts as the intelligent orchestrator, housing the complex business logic that ensures community stability:

- **Service-Layer Transactionality**: Mission-critical operations like technician availability matching and amenity
  capacity checking have been refactored from legacy SQL procedures into **Java Services**. This ensures that
  transactional integrity is maintained via Spring's `@Transactional` boundary, regardless of the underlying database.
- **Automated Fiscal Rules**:
    - **Dynamic GST**: An 18% GST is automatically applied to all community payments via JPA lifecycle hooks (
      `@PrePersist`).
    - **Overdue Management**: Scheduled background tasks (`@Scheduled`) periodically audit the financial ledger to
      enforce payment status transitions.
- **Stateless RBAC Security**: JWT-based security extracts role claims (`SuperAdmin`, `Technician`, `Resident`) to
  enforce strict server-side permissions through Spring Security.

### 3. User Experience (React 19 & "Royal Indigo" Design)

- **Royal Indigo Design System (v1.0)**: A curated design language utilizing Indigo (`#6366f1`) and Slate (`#1e293b`)
  tokens.
- **Field Ops Control Center**: High-fidelity dashboard for technicians with real-time status toggling.
- **Resident Portal**: Intuitive interface for resource booking and ledger auditing.

## 🚀 Deployment & Operations

### Prerequisites

- **JDK 17** | **Maven 3.8+** | **Node.js v18+**

### Rapid Start (Production Mode - H2)

By default, the system starts in **Production Mode** using an embedded, persistent H2 database.

1. **Backend**:

   ```bash
   cd backend && mvn spring-boot:run -Dspring.profiles.active=prod
   ```

   _The database file will be created automatically in the project root._

2. **Frontend**:

   ```bash
   cd frontend && npm install && npm run dev
   ```

### Development Mode (MySQL)

To run with a local MySQL instance, use the `dev` profile:

1. Ensure a MySQL database named `UrbanNexus` exists.
2. Update `application.yml` with your local credentials.
3. Start the backend:

   ```bash
   mvn spring-boot:run -Dspring.profiles.active=dev
   ```

## 🧪 API Verification (Postman)

- **Path**: `resources/UrbanNexus_Postman_Collection.json`
- **Admin**: `admin` / `pwd123#`
- **Resident**: `john_doe` / `pwd123#`

---

© 2026 UrbanNexus Engineering. Engineered for the Future of Community Living.
