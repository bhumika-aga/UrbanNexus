# UrbanNexus Frontend Application (React)

The `frontend` module is the single-page web app for UrbanNexus. One login serves three portals — admin, resident and
technician — chosen from the role in the user's JWT. All data comes from the Spring Boot API in `../backend`.

## 📱 Tech Stack & Tooling

- **Core Framework:** React 19 with functional components and hooks.
- **TypeScript 5:** shared API types live in `src/types/index.ts`.
- **Vite 8:** dev server with hot reload, and the production build. Requires Node.js 20.19+ or 22.12+.
- **Component Library:** Material UI (MUI) v9 with a custom theme (`src/theme/`) for a clean, Vercel/Apple-inspired
  look.
- **Icons:** Font Awesome (`@fortawesome/react-fontawesome`), plus a few MUI icons.
- **Animation:** Framer Motion for page and component transitions.
- **Routing:** React Router 7 (`/login` and `/dashboard`; the dashboard renders the portal for the user's role).
- **API:** Axios (`src/api/axiosClient.ts`) and `jwt-decode` for reading the token on the client.
- **Quality:** ESLint 9 and Prettier 3.

## 🔀 Portals

1. **Resident Portal**
   - **My Dues:** pending, overdue and under-review invoices. There is no online payment; the resident pays offline
     (UPI, bank transfer, cheque), clicks **I've Paid** and enters the payment reference. The invoice shows "Under
     Review" until the administrator confirms it.
   - **Facilities:** reserve an amenity for a date (today or later), slot and headcount. The server refuses bookings
     that exceed the remaining capacity for that slot.
   - **Staff Support:** request a technician. The skill list and the GST-inclusive price of each service come from the
     API (`/bookings/technician-skills`), so they follow the price list.
   - **My Activity:** past and upcoming reservations and service tickets.

2. **Technician Portal**
   - The open jobs queue, with each job's date (past-due dates in red) and time slot, and a **Resolve Task** button to
     mark one's own job completed.
   - Counters for resolved, active, today's and past-due jobs, an archive of completed jobs, and a duty-status switch
     (available for new assignments or not).

3. **Admin Dashboard**
   - Summary cards (residents, payments to review, unpaid total, open assignments) that refresh after each action.
   - **Resident Directory:** search, add a resident (shows their one-time credentials), reset a password, delete a
     resident — with an offer to write off any money they still owe.
   - **Technical Crew:** add a technician, edit name, contact, skill or availability, reset a password.
   - **Financial Ledger:** search invoices; confirm or reject reported payments, record offline payments, and run the
     overdue scan.
   - **Open Assignments**, **Facility Bookings**, **Service History** and **Audit Logs** (who changed what, and when).

Every user also has a **Profile** page (contact details, family members for residents, password change). Technicians see
their skill but only the administrator can change it.

## 🔑 Authentication

`AuthProvider` (`src/context/`) holds the session:

1. The login page posts the credentials to `/api/login` (after clearing any old token) and stores the returned JWT in
   `localStorage`.
2. The Axios client adds `Authorization: Bearer <token>` to every request.
3. A `401` from the API — expired, revoked (password changed or reset elsewhere) or deleted account — clears the token
   and returns to the login page. A `403` only means that one action was refused; it does not sign the user out.
4. The session also ends on its own when the token expires (24 hours); an expired token found on page load is
   discarded.
5. Accounts on an admin-issued temporary password see only the **Set your own password** screen until they replace
   it. A successful password change returns a new token, which replaces the old (now revoked) one.

## 🚀 Running the Client

### 1. Install

```bash
npm install
```

### 2. Configure the API address

The app calls `http://localhost:8080/api` by default. To use another backend, set `VITE_API_URL`, for example in
`frontend/.env.local`:

```bash
VITE_API_URL=https://your-backend.example.com/api
```

The backend only accepts browser requests from origins listed in its `CORS_ALLOWED_ORIGINS` (locally any
`http://localhost` port is allowed).

### 3. Run

```bash
npm run dev
```

Then open `http://localhost:5173`. The seed accounts are listed in the main [README](../README.md#credentials).

### Scripts

| Command                | What it does                                       |
| ---------------------- | -------------------------------------------------- |
| `npm run dev`          | Dev server with hot reload                         |
| `npm run build`        | Production build into `dist/`                      |
| `npm run preview`      | Serve the production build locally                 |
| `npm run lint`         | ESLint (`npm run lint:fix` to fix what it can)     |
| `npx tsc --noEmit`     | Type-check                                         |
| `npm run format:check` | Check Prettier formatting (`npm run format` fixes) |

CI runs the type-check, lint and build on every push and pull request. The browser walkthrough in `../e2e` exercises
every screen against a running backend; see the main [README](../README.md#browser-walkthrough-end-to-end).
