# UrbanNexus Backend Architecture (Spring Boot)

The `backend` module serves as the command center for the UrbanNexus property system. It has been fully migrated into a robust, enterprise-grade **Java Spring Boot (3.5.13)** application using **Java 17**.

## 🧩 Architectural Design (N-Tier ORM)

The backend rigorously applies a clean N-Tier architecture with **Constructor-based Dependency Injection** (via Lombok `@RequiredArgsConstructor`) for improved testability and maintenance:

- **Entity Layer (`com.urbannexus.model`)**: Data schemas are mapped as strict JPA (`@Entity`) beans with complex relational joints (`@ManyToOne`, `@OneToOne`).
- **Data Access Layer (`com.urbannexus.repository`)**: Database interaction is handled via `JpaRepository` and native `@Procedure` hooks.
- **Business Layer (`com.urbannexus.service`)**: Core logic with transactional boundaries (`@Transactional`). Features include automated technician booking and cron-scheduled payment processing.
- **Controller Layer (`com.urbannexus.controller`)**: The HTTP boundary intercepting raw JSON using `Jackson` Object-Mappers mapping neatly into RESTful operations (`GET`, `POST`, `PUT`, `DELETE`).

## 🔐 Authentication & Session Flow

The logic bypasses traditional sticky sessions for pure stateless logic matching the isolated frontend React clients:

- **Spring Security:** A customized `SecurityFilterChain` restricts `/api/**` channels safely parsing unauthenticated attempts immediately out of the cycle.
- **JWT Authorization:** Once logged in using standard `BCrypt` hashes via `AuthController`, the payload embeds into an `Authorization: Bearer <TOKEN>` chain.
- **Role Isolation:** Every `RestController` hook specifies strict scope boundaries globally protecting Administrative routing channels from basic Resident keys.

## 🛠 Integrating Stored Procedures into JPA

A primary directive of the backend is honoring the raw, heavy-processing algorithms built natively into the MySQL structure. Because Stored Procedures (`CALL AutoBookTechnician`) bypass standard table-locking, they cannot be handled easily by generic ORM `save()` blocks.

We integrated these beautifully into Spring Data via the `@Procedure` map:

```java
// Safely passing atomic executions natively matching JPA repository standards
@Procedure(procedureName = "AutoBookTechnician")
Map<String, Object> autoBookTechnician(Long resident_id, String skill, Integer slot, String assign_date);
```

This isolates constraint complexities safely without cluttering the Java build path.

## 🚀 Running the Server

### 1. Configuration

Modify `src/main/resources/application.yml` targeting your local connection:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/urbannexus_db
    username: root
    password: password
```

### 2. Compilation and Launch

From the `backend/` directory, simply package and run locally utilizing Maven:

```bash
mvn clean install
mvn spring-boot:run
```

The server will default successfully to bridging API traffic listening to port **4720**.
