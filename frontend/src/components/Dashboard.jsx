import { Navigate, useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { LogOut } from 'lucide-react';
import AdminDashboard from './AdminDashboard';
import ResidentDashboard from './ResidentDashboard';
import TechnicianDashboard from './TechnicianDashboard';

export default function Dashboard() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    if (!token) return <Navigate to="/login" />;

    let userRole = '';
    try {
        const decoded = jwtDecode(token);
        userRole = decoded.role;
    } catch (error) {
        localStorage.removeItem('token');
        return <Navigate to="/login" />;
    }

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow-sm border-b border-gray-200 px-8 py-4 flex justify-between items-center">
                <h1 className="text-xl font-bold text-blue-600 italic tracking-tighter uppercase">URBAN_NEXUS</h1>
                <div className="flex items-center space-x-4">
                    <span className="text-[10px] font-mono uppercase tracking-widest px-3 py-1 bg-blue-50 rounded-full text-blue-600 font-bold">
                        {userRole}
                    </span>
                    <button onClick={handleLogout} className="flex items-center text-gray-500 hover:text-red-600 transition-colors text-sm font-medium uppercase italic">
                        <LogOut className="w-4 h-4 mr-2" />
                        Logout
                    </button>
                </div>
            </nav>

            <main>
                {userRole === 'SuperAdmin' && <AdminDashboard />}
                {userRole === 'Resident' && <ResidentDashboard />}
                {userRole === 'Technician' && <TechnicianDashboard />}
            </main>
        </div>
    );
}