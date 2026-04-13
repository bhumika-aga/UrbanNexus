import { Users, FileText, Wrench, Activity } from 'lucide-react';
import ResidentDirectory from './ResidentDirectory';
import AuditLog from './AuditLog'; // New Import

export default function AdminDashboard() {
    return (
        <div className="p-8 space-y-8 max-w-7xl mx-auto">
            <header>
                <h1 className="text-3xl font-bold text-gray-900 italic">PIT WALL: COMMAND CENTER</h1>
                <p className="text-gray-500 text-sm">Managing the grid and community operations.</p>
            </header>

            {/* ... stats grid remains the same ... */}

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2">
                    <ResidentDirectory />
                </div>
                <div className="lg:col-span-1">
                    <AuditLog />
                </div>
            </div>
        </div>
    );
}