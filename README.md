# UrbanNexus: Integrated Residential Operations & Resource System (urbannexus-dbms)

A high-concurrency database management system designed for modern residential complexes to orchestrate the lifecycle of units, residents, maintenance workflows, and shared community resources.

Unlike traditional property management tools that rely on fragmented spreadsheets, `UrbanNexus` replaces manual tracking with a unified relational architecture that prioritizes **data consistency** and **transactional atomicity**. Every interaction—from reporting a leak to reserving a community hall—is governed by strict database-level constraints to ensure 100% operational reliability and financial transparency.

## The Core Innovation: Atomic State Synchronization

Standard management systems often suffer from "race conditions" where two residents might book the same slot simultaneously, or a maintenance task is assigned to an unavailable technician. `UrbanNexus` mitigates these risks by implementing **Constraint-Driven Logic** directly within the SQL layer, ensuring the database remains the absolute source of truth.

The system utilizes a **Synchronized Resource Engine** to manage overlapping states:
* **Preventive Locking:** When a resident initiates an amenity booking, the system utilizes serializable isolation to lock the time slot during the "Pending" phase.
* **Atomic Completion:** The booking is only committed to the ledger if the associated payment transaction returns a success code, preventing "orphaned bookings" and revenue leakage.
* **Self-Healing Schedules:** Advanced SQL triggers automatically recalculate technician availability the moment a maintenance ticket is closed, eliminating administrative bottlenecks in the workforce.

## Technical Stack

* **Interface / Frontend:** Streamlit (Rapid, data-driven web app deployment natively in Python).
* **Backend Logic:** Python (Core business logic and transaction orchestration).
* **Database Driver:** `mysql-connector-python` (For executing parameterized queries, calling stored procedures, and handling commits/rollbacks).
* **Database:** MySQL (Relational database management, enforcing strict ACID properties and relational integrity).
* **State Management:** Streamlit Session State (`st.session_state`) for tracking resident sessions, multi-step booking flows, and real-time UI updates.

## Architecture & Division of Labor

This project utilizes a modular architecture to facilitate parallel development and robust data handling:

* **Database Administration (DBA):** Manages the 3NF/BCNF relational schema in MySQL, designs optimized table structures, enforces referential integrity (foreign keys), and develops trigger-based automation.
* **Backend Engineering (Python Core):** Develops modular Python functions to interact securely with the database via the MySQL Connector, orchestrates server-side transaction wrapping to prevent data anomalies, and handles user authentication logic.
* **Frontend Architecture (Streamlit):** Constructs the resident and administrative dashboards, focusing on integrating Python logic with Streamlit's interactive widgets, dynamic data visualization (e.g., Pandas dataframes, charts), and intuitive workflow design.
* **System Integration:** Connects the database logic layer to the Streamlit UI state, ensuring seamless bidirectional data flow, maintaining session security, and orchestrating automated, audit-ready reporting mechanisms.

## Key System Features

* **Intelligent Unit & Resident Mapping:** Dynamic tracking of ownership status, occupancy levels, and complex multi-resident relationships per unit.
* **Automated Maintenance Lifecycle:** End-to-end ticket management featuring priority-based queuing and automated technician assignment based on specialization.
* **Collision-Proof Amenity Engine:** Real-time scheduling for shared facilities with built-in logic to prevent double-bookings and enforce community usage rules.
* **Integrated Financial Ledger:** A transactional billing module that links service delivery to payment records with strict referential integrity.
* **Operational Analytics:** Automated reporting on facility utilization rates, technician response times, and community financial health directly within the Streamlit dashboard.