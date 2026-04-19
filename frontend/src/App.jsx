import {
  Navigate,
  Route,
  BrowserRouter as Router,
  Routes,
} from "react-router-dom";
import Dashboard from "./components/Dashboard";
import Login from "./components/Login";

function App() {
  return (
    <Router>
      <Routes>
        {/* Default route redirects to login */}
        <Route path="/" element={<Navigate to="/login" />} />

        {/* The Login Page */}
        <Route path="/login" element={<Login />} />

        {/* The Dynamic Dashboard */}
        <Route path="/dashboard" element={<Dashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
