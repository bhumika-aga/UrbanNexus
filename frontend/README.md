# UrbanNexus Frontend Application (React)

The `frontend` module is the visual core of the UrbanNexus platform, providing isolated logical portals for various property stakeholders securely accessing the overarching database models.

## 📱 Tech Stack & Tooling

- **Core Framework:** React 19, utilizing functional components and hooks (`useState`, `useEffect`, `useContext`).
- **TypeScript:** Enforced strict typing across all components and API layers to ensure zero "any" types and robust compilation.
- **Vite Bundler:** High-speed HMR build tool for optimized development and bundling.
- **Component Library:** **Material-UI (MUI)**. Leverages a custom theme for a clean **Vercel/Apple-inspired** aesthetic, featuring glassmorphism and monochrome accents.
- **Icons:** `lucide-react` for consistent, lightweight vector iconography.
- **Routing:** `react-router-dom` for seamless SPA navigation.
- **API Bridging:** `Axios` with interceptors for automatic JWT handling.

## 🔀 Application Workflows

Rather than mixing user actions collectively, the React application intercepts the JWT generated through the Java Spring backend dynamically branching behavior securely into three boundaries:

1. **Resident Portal:**
   - Visualizes personal pending financial dues.
   - Manages personal maintenance service bookings (Technicians mapping Plumbers, Electricians).
   - Coordinates dynamic Amenity scheduling securely referencing backend capacities.

2. **Technician Portal:**
   - Dedicated simplified routing displaying currently mapped assignments scheduled via Admin hooks.
   - Allows instant status changes referencing `COMPLETED` tags back modifying raw Java hooks.

3. **Super Admin Dashboard:**
   - God-view interfaces querying the comprehensive Grid mapping (Tenants, Tasks, Amenities, Transactions).
   - Explicit buttons triggering administrative bulk backend cycles (ex: "Process Overdue Payments" mapping internally onto Java Cron Jobs natively).

## 🔑 Stateless Integration via Axios

All components rely on an `AuthContext` wrapper. Whenever a Resident logs in:

1. React parses the returning Spring Boot `token`.
2. Stores it successfully via browser storage (`localStorage`).
3. Dynamically attaches an intercept pipeline using Axios appending `Authorization: Bearer <token>` on all future `/api/**` traffic seamlessly bypassing internal 403 Forbidden constraints.

## 🚀 Running the Client

### 1. Verification

Ensure Node packages have been installed cleanly inside the `/frontend` working directory:

```bash
npm install
```

### 2. Environment Mapping

Standard connections check against `http://localhost:8080/api` natively mapping toward the Java API server. Ensure any `.env` mapping (`VITE_API_BASE_URL`) aligns sequentially with the backend Tomcat host.

### 3. Execution

Launch the Vite hot-reloading listener:

```bash
npm run dev
```

Navigate internally to the localhost target typically matching `localhost:5173`.
