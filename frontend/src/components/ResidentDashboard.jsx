import { useState } from 'react';
import { CreditCard, Calendar, Wrench, ChevronLeft, Loader2 } from 'lucide-react';
import BookAmenity from './BookAmenity';
import BookTechnician from './BookTechnician';
import api from '../api';

export default function ResidentDashboard() {
    const [view, setView] = useState('menu'); // 'menu', 'dues', 'book-amenity', 'book-tech'
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchDues = async () => {
        setLoading(true);
        try {
            const response = await api.get('/residents/me/dues');
            setData(response.data.invoices);
            setView('dues');
        } catch (error) { alert(error.response?.data?.error || error.message); }
        finally { setLoading(false); }
    };

    const handlePayment = async (transNo) => {
        try {
            await api.post(`/payments/${transNo}/pay`);
            alert("Payment successful! Transaction confirmed.");
            fetchDues();
        } catch (error) { alert("Payment failed."); }
    };

    if (view === 'book-amenity') return <BookAmenity onBack={() => setView('menu')} />;
    if (view === 'book-tech') return <BookTechnician onBack={() => setView('menu')} />;

    if (view === 'dues') {
        return (
            <div className="p-8 max-w-4xl mx-auto">
                <button onClick={() => setView('menu')} className="flex items-center text-blue-600 mb-6 hover:underline font-medium">
                    <ChevronLeft className="w-4 h-4 mr-1" /> Back to Paddock
                </button>
                <h2 className="text-2xl font-bold mb-6 italic tracking-tighter">PENDING INVOICES</h2>
                <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <table className="w-full text-left">
                        <thead className="bg-gray-50 text-[10px] uppercase font-bold text-gray-400">
                        <tr><th className="px-6 py-4">Transaction #</th><th className="px-6 py-4">Type</th><th className="px-6 py-4">Amount (Incl. GST)</th><th className="px-6 py-4">Action</th></tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                        {data.length === 0 ? (
                            <tr><td colSpan="4" className="text-center py-10 text-gray-400 italic">No pending dues. Clear for racing!</td></tr>
                        ) : data.map((inv) => (
                            <tr key={inv.trans_no}>
                                <td className="px-6 py-4 font-mono text-sm">{inv.trans_no}</td>
                                <td className="px-6 py-4 text-sm font-semibold text-gray-700">{inv.service_type}</td>
                                <td className="px-6 py-4 font-bold text-blue-600">₹{inv.cost}</td>
                                <td className="px-6 py-4">
                                    <button onClick={() => handlePayment(inv.trans_no)} className="bg-green-600 text-white px-4 py-1.5 rounded-md text-xs font-bold uppercase tracking-widest hover:bg-green-700 transition-colors">PAY NOW</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

    return (
        <div className="p-8 space-y-8 max-w-7xl mx-auto">
            <header>
                <h1 className="text-3xl font-bold text-gray-900 italic tracking-tighter">RESIDENT PORTAL</h1>
                <p className="text-gray-500 text-sm">Welcome to the Paddock. Access your services below.</p>
            </header>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div onClick={fetchDues} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 hover:border-blue-500 transition-all cursor-pointer group relative">
                    {loading && <div className="absolute inset-0 bg-white/50 flex items-center justify-center rounded-2xl backdrop-blur-sm"><Loader2 className="animate-spin text-blue-600" /></div>}
                    <CreditCard className="w-10 h-10 text-blue-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold mb-2">My Dues</h3>
                    <p className="text-gray-500 text-sm">Review your maintenance bills and clear pending invoices.</p>
                </div>

                <div onClick={() => setView('book-amenity')} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 hover:border-green-500 transition-all cursor-pointer group">
                    <Calendar className="w-10 h-10 text-green-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold mb-2">Reserve Facility</h3>
                    <p className="text-gray-500 text-sm">Secure the Paddock Club or the Monaco Pool.</p>
                </div>

                <div onClick={() => setView('book-tech')} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 hover:border-orange-500 transition-all cursor-pointer group">
                    <Wrench className="w-10 h-10 text-orange-600 mb-6 group-hover:scale-110 transition-transform" />
                    <h3 className="text-xl font-bold mb-2">Request Crew</h3>
                    <p className="text-gray-500 text-sm">Dispatch technical support to your unit.</p>
                </div>
            </div>
        </div>
    );
}