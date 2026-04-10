# UrbanNexus: Integrated Residential Operations & Resource System (urbannexus-dbms)

A high-concurrency database management system designed for modern residential complexes to orchestrate the lifecycle of units, residents, maintenance workflows, and shared community resources.

Unlike traditional property management tools that rely on fragmented spreadsheets, `UrbanNexus` replaces manual tracking with a unified relational architecture that prioritizes **data consistency** and **transactional atomicity**. Every interaction—from reporting a leak to reserving a community hall—is governed by strict database-level constraints to ensure 100% operational reliability and financial transparency.

## The Core Innovation: Atomic State Synchronization

Standard management systems often suffer from "race conditions" where two residents might book the same slot simultaneously, or a maintenance task is assigned to an unavailable technician. `UrbanNexus` mitigates these risks by implementing **Constraint-Driven Logic** directly within the SQL layer, ensuring the database remains the absolute source of truth.

The system utilizes a **Synchronized Resource Engine** to manage overlapping states:
* **Dynamic Availability Locking:** Utilizing `NOT EXISTS` subqueries and transactional stored procedures, the system verifies and locks resource slots in real-time, preventing double-booking without permanently freezing schedules.
* **Atomic Completion:** Workflows like technician booking are bundled into strict SQL transactions (`START TRANSACTION` / `COMMIT`). If any part of the process fails—from finding a technician to generating the invoice—the entire operation rolls back safely.
* **Automated Financial Triggers:** Database-level triggers intercept data before it is written, automatically calculating variables like GST tax to ensure the financial ledger remains pristine and tamper-proof.

## Technical Stack

* **Framework / Backend Core:** Node.js with Express.js (Provides a robust, non-blocking RESTful API architecture).
* **Database:** MySQL (Relational database management, enforcing strict ACID properties, stored procedures, and triggers).
* **Database Driver:** `mysql2/promise` (Implements asynchronous connection pooling and secure, parameterized queries to prevent SQL injection).
* **Authentication & Security:** JSON Web Tokens (JWT) for stateless, role-based session management, paired with `bcrypt` for cryptographic password hashing.
* **API Testing / Consumer:** Postman (For precise endpoint validation, header configuration, and JSON payload testing).

## Architecture & Division of Labor

This project utilizes a modular, backend-first architecture to facilitate rapid development and ironclad data handling:

* **Database Architecture (Data Layer):** Manages the normalized relational schema in MySQL, designs optimized table structures (like decoupled `pricing` lookups), and authors advanced DBMS logic including Triggers and Stored Procedures.
* **API Engineering (Routing Layer):** Develops asynchronous Express routes (`server.js`) to handle HTTP requests, parse JSON bodies, and securely interface with the MySQL connection pool.
* **Security Integration (Middleware Layer):** Implements "Bouncer-pattern" middleware (`authMiddleware.js`) to intercept requests, validate JWT signatures, and ensure strictly authenticated access control before any database interaction occurs.
* **Client Integration (Headless Architecture):** The backend serves as a headless API, outputting clean, structured JSON. This completely decouples the logic from the interface, allowing any modern frontend framework (React, Vue, HTML/JS) to easily consume the data.

## Key System Features

* **Role-Based Access Control:** Secure admin dashboard access governed by encrypted credentials and expiring web tokens.
* **Intelligent Unit & Resident Mapping:** Dynamic tracking of ownership status, occupancy levels, and complex multi-resident relationships per unit.
* **Automated Maintenance Lifecycle:** End-to-end ticket management featuring dynamic availability checks and automated technician assignment based on specialization.
* **Collision-Proof Amenity Engine:** Real-time scheduling for shared facilities with built-in trigger logic to prevent capacity overbooking.
* **Integrated Financial Ledger:** A transactional billing module that automatically applies taxes and links service delivery to dynamic pricing lookup tables.