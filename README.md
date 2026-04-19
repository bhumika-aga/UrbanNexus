# UrbanNexus: Integrated Residential Operations & Resource System

Welcome to the **UrbanNexus** monolith repository!

UrbanNexus is a high-concurrency database management system and web application designed for modern residential complexes. It comprehensively orchestrates the lifecycle of housing units, residents, maintenance workflows, pending financial dues, and shared community amenity resources.

Unlike traditional property management tools that rely on fragmented spreadsheets, UrbanNexus implements a unified relational architecture prioritizing **data consistency** and **transactional atomicity**. Every interaction—from a resident reporting a structural leak to reserving a community hall—is governed by strict high-performance systems mapping securely from a React user interface down to strict database-level constraints.

---

## 🏗 Repository Structure

This project uses a decoupled Full-Stack architecture split cleanly across two main environments. Detailed documentation for each environment is located in their respective directories:

### 1. `backend/`

The API and Persistence layer. Originally built on Node.js/Express, the backend has been successfully overhauled into an Enterprise-Grade **Java Spring Boot Application**. This layer uses **Spring Data JPA (Hibernate)** acting directly on a MySQL database, merging Stored Procedures with type-safe server logic.
👉 [**Read the Backend Architecture Documentation**](./backend/README.md)

### 2. `frontend/`

The Interactive User Interface. A high-performance **React 19** application powered by **Vite**, **TypeScript**, and **Material-UI (MUI)**. It adopts a minimalist **Vercel/Apple-inspired** aesthetic with strict monochromatic themes and glassmorphism. It securely parses JWT tokens to provide tailored experiences for Residents, Technicians, and SuperAdmins.
👉 [**Read the Frontend Architecture Documentation**](./frontend/README.md)

---

## ⚙️ Core Operational Concepts

UrbanNexus mitigates standard property management "race conditions" where two residents might book the same physical slot simultaneously by implementing a **Synchronized Resource Engine**:

1. **JPA Entity Synchronization:** The complete MySQL schema relies on strictly mapped `@Entity` graphs across Java guaranteeing type-safe manipulation.
2. **Database-Level Locks:** Standard management systems check overlap in the code. UrbanNexus checks overlap natively via strict database `BEFORE INSERT` triggers and optimized **Stored Procedures** routed directly through Spring Data's `@Procedure` wrapper.
3. **Automated Subsystems:** Recurring logic such as checking for overdue residential payments is automated utilizing a Java `@Scheduled(cron)` workflow hitting batch transactions overnight, entirely bypassing manual admin calculation.

## 🚀 Getting Started

### Prerequisites

Before running the system, ensure you have the following installed locally:

- **Java Development Kit (JDK) 17** for compiling the Spring App.
- **Maven 3.8+** for backend dependency management.
- **Node.js (v18+)** and **npm** for frontend bundling.
- **MySQL Server (v8+)** initialized and running.

### Initialization Workflow

1. **Initialize the Database:** Run the scripts located in `resources/DB_init.sql` directly on your MySQL instance.
2. **Start the Backend Layer:** Follow the compilation limits in the [Backend README](./backend/README.md) to boot the `localhost:4720` Spring Tomcat server.
3. **Start the Frontend UI:** Follow run setups in the [Frontend README](./frontend/README.md) to bootstrap the Vite hot-reloading window!

_(For automated testing, a comprehensive `UrbanNexus_Postman_Collection.json` is provided in the repository root)._
