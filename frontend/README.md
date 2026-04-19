# UrbanNexus Frontend Application (React)

The `frontend` module is the visual core of the UrbanNexus platform, providing isolated logical portals for various property stakeholders securely accessing the overarching database models.

## 📱 Tech Stack & Tooling

- **Core Framework:** React 18, integrated heavily with React Hooks (`useState`, `useEffect`, `useContext`).
- **Vite Bundler:** Serves as the HMR (Hot Module Replacement) build tool keeping compilation footprints and local testing exceptionally fast versus legacy Webpack.
- **Component Styling:** `Tailwind CSS`. We bypass complex traditional CSS files for scalable inline utility styling mapped via `postcss` generating a unified responsive application ecosystem regardless of screen dimension!
- **Icons & Nav:** `lucide-react` constructs unified vector representations, while `react-router-dom` powers seamless client-side single-page (SPA) navigation arrays seamlessly linking modules.
- **API Bridging:** Handled globally by `Axios`.

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

Standard connections check against `http://localhost:4720/api` natively mapping toward the Java API server. Ensure any `.env` mapping (`VITE_API_BASE_URL`) aligns sequentially with the backend Tomcat host.

### 3. Execution

Launch the Vite hot-reloading listener:

```bash
npm run dev
```

Navigate internally to the localhost target typically matching `localhost:5173`.
