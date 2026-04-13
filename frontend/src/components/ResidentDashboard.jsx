import { useState, useEffect } from 'react';
import { CreditCard, Calendar, Wrench, ChevronLeft, Loader2, Settings } from 'lucide-react';
import BookAmenity from './BookAmenity';
import BookTechnician from './BookTechnician';
import api from '../api';

export default function ResidentDashboard() {
    const [view, setView] = useState('menu');
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [profile, setProfile] = useState({ name: '', contact: '', password: '' });

    // Fetch current profile info when entering settings
    useEffect(() => {
        if (view === 'settings') {
            const syncProfile = async () => {
                try {
                    const res = await api.get('/profile/me');
                    setProfile({ name: res.data.name, contact: res.data.contact, password: '' });
                } catch (err) { console.error("Profile sync failed"); }
            };
            syncProfile();
        }
    }, [view]);

    const fetchDues = async () => {
        setLoading(true);
        try {
            const res = await api.get('/residents/me/dues');
            setData(res.data.invoices);
            setView('dues');
        } catch (err) { alert(err.response?.data?.error || err.message); }
        finally { setLoading(false); }
    };

    const handleProfileUpdate = async (e) => {
        e.preventDefault();
        try {
            await api.put('/profile/update', profile);
            alert("Profile updated and synced with the grid!");
        } catch (err) { alert("Update failed"); }
    };

    if (view === 'book-amenity') return <BookAmenity onBack={() => setView('menu')} />;
    if (view === 'book-tech') return <BookTechnician onBack={() => setView('menu')} />;

    if (view === 'settings') {
        return (
            <div className="p-8 max-w-xl mx-auto">
                <button onClick={() => setView('menu')} className="flex items-center text-blue-600 mb-6 font-bold uppercase text-xs">
                    <ChevronLeft className="w-4 h-4 mr-1" /> Back to Paddock
                </button>
                <div className="bg-white p-8 rounded-2xl border border-gray-100 shadow-sm space-y-6">
                    <h2 className="text-2xl font-bold italic uppercase tracking-tighter">Account Tuning</h2>
                    <form onSubmit={handleProfileUpdate} className="space-y-4">
                        <div>
                            <label className="text-[10px] font-bold text-gray-400 uppercase">Display Name</label>
                            <input type="text" value={profile.name} className="w-full p-3 border rounded-xl bg-gray-50" onChange={e => setProfile({...profile, name: e.target.value})} />
                        </div>
                        <div>
                            <label className="text-[10px] font-bold text-gray-400 uppercase">Contact Number</label>
                            <input type="text" value={profile.contact} className="w-full p-3 border rounded-xl bg-gray-50" onChange={e => setProfile({...profile, contact: e.target.value})} />
                        </div>
                        <div>
                            <label className="text-[10px] font-bold text-gray-400 uppercase">New Password</label>
                            <input type="password" placeholder="••••••••" className="w-full p-3 border rounded-xl bg-gray-50" onChange={e => setProfile({...profile, password: e.target.value})} />
                        </div>
                        <button type="submit" className="w-full bg-blue-600 text-white py-3 rounded-xl font-bold uppercase hover:bg-blue-700">Save Changes</button>
                    </form>
                </div>
            </div>
        );
    }

    return (
        <div className="p-8 space-y-8 max-w-7xl mx-auto">
            <header><h1 className="text-3xl font-bold italic underline decoration-blue-500 uppercase">Resident Portal</h1></header>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                <div onClick={fetchDues} className="bg-white p-8 rounded-2xl border border-gray-100 hover:border-blue-500 transition-all cursor-pointer group relative">
                    {loading && <Loader2 className="animate-spin absolute top-4 right-4 text-blue-600" />}
                    <CreditCard className="w-10 h-10 text-blue-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold">My Dues</h3>
                </div>
                <div onClick={() => setView('book-amenity')} className="bg-white p-8 rounded-2xl border border-gray-100 hover:border-green-500 transition-all cursor-pointer group">
                    <Calendar className="w-10 h-10 text-green-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold">Facilities</h3>
                </div>
                <div onClick={() => setView('book-tech')} className="bg-white p-8 rounded-2xl border border-gray-100 hover:border-orange-500 transition-all cursor-pointer group">
                    <Wrench className="w-10 h-10 text-orange-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold">Pit Crew</h3>
                </div>
                <div onClick={() => setView('settings')} className="bg-white p-8 rounded-2xl border border-gray-100 hover:border-gray-900 transition-all cursor-pointer group">
                    <Settings className="w-10 h-10 text-gray-400 mb-6 group-hover:rotate-90 transition-transform" />
                    <h3 className="text-xl font-bold">Settings</h3>
                </div>
            </div>
        </div>
    );
}