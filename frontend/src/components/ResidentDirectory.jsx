import { useState, useEffect } from 'react';
import { Search, UserPlus, Home, Phone, X, Trash2 } from 'lucide-react';
import api from '../api';

export default function ResidentDirectory() {
    const [residents, setResidents] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        name: '', house_block: '', house_floor: '', house_unit: '',
        ownership_status: 'Owner', contact: '', no_of_members: 1
    });

    const fetchResidents = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/admin/residents/search?name=${searchTerm}`);
            setResidents(response.data.residents);
        } catch (error) {
            console.error("Grid lookup failed:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddResident = async (e) => {
        e.preventDefault();
        try {
            await api.post('/residents', formData);
            setShowModal(false);
            setFormData({ name: '', house_block: '', house_floor: '', house_unit: '', ownership_status: 'Owner', contact: '', no_of_members: 1 });
            fetchResidents();
        } catch (error) {
            alert("Failed to sign resident: " + (error.response?.data?.error || error.message));
        }
    };

    // Deletion Logic
    const handleDelete = async (id, name) => {
        if (window.confirm(`Are you sure you want to remove ${name} from the grid? This will delete their login and all history.`)) {
            try {
                await api.delete(`/residents/${id}`);
                fetchResidents(); // Refresh the table after deletion
            } catch (error) {
                alert("Deletion failed: " + (error.response?.data?.error || error.message));
            }
        }
    };

    useEffect(() => { fetchResidents(); }, [searchTerm]);

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <h2 className="text-lg font-bold text-gray-800">Resident Directory</h2>
                <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                        type="text"
                        placeholder="Search drivers (e.g., Hamilton)..."
                        className="w-full pl-10 pr-4 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="flex items-center space-x-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                >
                    <UserPlus className="w-4 h-4" />
                    <span>Sign New Resident</span>
                </button>
            </div>

            {/* Directory Table */}
            <div className="overflow-x-auto">
                <table className="w-full text-left">
                    <thead className="bg-gray-50 text-gray-400 text-[10px] uppercase tracking-widest font-bold">
                    <tr>
                        <th className="px-6 py-4">Name</th>
                        <th className="px-6 py-4">Unit</th>
                        <th className="px-6 py-4">Status</th>
                        <th className="px-6 py-4">Contact</th>
                        <th className="px-6 py-4 text-right">Actions</th> {/* Added Actions Header */}
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm">
                    {loading ? (
                        <tr><td colSpan="5" className="text-center py-10 text-gray-400 italic">Scanning the grid...</td></tr>
                    ) : residents.map((resident) => (
                        <tr key={resident.resident_id} className="hover:bg-gray-50 transition-colors group">
                            <td className="px-6 py-4 font-semibold text-gray-900">{resident.name}</td>
                            <td className="px-6 py-4 text-gray-600 font-mono">{resident.house_block}-{resident.house_unit}</td>
                            <td className="px-6 py-4">
                                <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                                    resident.ownership_status === 'Owner' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'
                                }`}>
                                    {resident.ownership_status}
                                </span>
                            </td>
                            <td className="px-6 py-4 text-gray-500 font-mono">{resident.contact}</td>
                            <td className="px-6 py-4 text-right">
                                <button
                                    onClick={() => handleDelete(resident.resident_id, resident.name)}
                                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                                    title="Delete Resident"
                                >
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* Add Resident Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl w-full max-w-md p-8 relative">
                        <button onClick={() => setShowModal(false)} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"><X /></button>
                        <h3 className="text-xl font-bold mb-6">Register New Driver</h3>
                        <form onSubmit={handleAddResident} className="space-y-4">
                            <input type="text" placeholder="Full Name" required className="w-full p-2 border rounded-lg" onChange={(e) => setFormData({...formData, name: e.target.value})} />
                            <div className="grid grid-cols-3 gap-2">
                                <input type="text" placeholder="Block" required className="p-2 border rounded-lg" onChange={(e) => setFormData({...formData, house_block: e.target.value})} />
                                <input type="text" placeholder="Floor" required className="p-2 border rounded-lg" onChange={(e) => setFormData({...formData, house_floor: e.target.value})} />
                                <input type="text" placeholder="Unit" required className="p-2 border rounded-lg" onChange={(e) => setFormData({...formData, house_unit: e.target.value})} />
                            </div>
                            <input type="text" placeholder="Contact Number" required className="w-full p-2 border rounded-lg" onChange={(e) => setFormData({...formData, contact: e.target.value})} />
                            <select className="w-full p-2 border rounded-lg" onChange={(e) => setFormData({...formData, ownership_status: e.target.value})}>
                                <option value="Owner">Owner</option>
                                <option value="Tenant">Tenant</option>
                            </select>
                            <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded-lg font-bold">Add to Grid</button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}