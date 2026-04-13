import { useState } from 'react';
import { Calendar, ChevronLeft, Loader2, CheckCircle2 } from 'lucide-react';
import { jwtDecode } from 'jwt-decode';
import api from '../api';

export default function BookAmenity({ onBack }) {
    const [loading, setLoading] = useState(false);
    const [invoice, setInvoice] = useState(null);
    const [formData, setFormData] = useState({
        amenity_id: '1', // Default to 1 (Assuming INT in your DB, or 'AM-01' if VARCHAR)
        date: '',
        slot: 1,
        capacity_booked: 1
    });

    const handleBooking = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            const decoded = jwtDecode(token);

            // Your SQL expects Amenity IDs as integers (1, 2, 3) based on the DB_init schema
            const payload = {
                ...formData,
                resident_id: decoded.resident_id,
                amenity_id: parseInt(formData.amenity_id)
            };

            const response = await api.post('/bookings/amenity', payload);
            setInvoice(response.data.invoice);
        } catch (error) {
            alert("Booking Failed: " + (error.response?.data?.error || error.message));
        } finally {
            setLoading(false);
        }
    };

    if (invoice) {
        return (
            <div className="p-8 max-w-2xl mx-auto bg-white rounded-2xl shadow-sm border border-green-200 text-center">
                <CheckCircle2 className="w-16 h-16 text-green-500 mx-auto mb-4" />
                <h2 className="text-2xl font-bold text-gray-900 italic mb-2">RESERVATION CONFIRMED</h2>
                <p className="text-gray-500 mb-6">Your paddock pass has been generated.</p>
                <div className="bg-gray-50 p-6 rounded-xl text-left font-mono text-sm border border-gray-100">
                    <p><strong>Booking ID:</strong> {invoice.booking_id}</p>
                    <p><strong>Amenity:</strong> {invoice.amenity_name}</p>
                    <p><strong>Date / Slot:</strong> {new Date(invoice.booking_date).toLocaleDateString()} / Slot {invoice.slot}</p>
                    <p><strong>Total Cost (inc. GST):</strong> ₹{invoice.total_with_gst}</p>
                    <p><strong>Transaction #:</strong> {invoice.trans_no}</p>
                </div>
                <button onClick={onBack} className="mt-8 bg-gray-900 text-white px-6 py-2 rounded-lg font-bold">Return to Dashboard</button>
            </div>
        );
    }

    return (
        <div className="p-8 max-w-2xl mx-auto">
            <button onClick={onBack} className="flex items-center text-blue-600 mb-6 hover:underline font-medium">
                <ChevronLeft className="w-4 h-4 mr-1" /> Back to Paddock
            </button>
            <h2 className="text-2xl font-bold mb-6 italic tracking-tighter">RESERVE FACILITY</h2>

            <form onSubmit={handleBooking} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 space-y-6">
                <div>
                    <label className="block text-sm font-bold text-gray-700 mb-2 uppercase tracking-widest">Select Facility</label>
                    <select className="w-full p-3 border border-gray-200 rounded-xl bg-gray-50 focus:ring-2 focus:ring-blue-500 outline-none"
                            onChange={(e) => setFormData({...formData, amenity_id: e.target.value})}>
                        <option value="1">Paddock Club Lounge</option>
                        <option value="2">Monaco Rooftop Pool</option>
                        <option value="3">Parc Fermé Gym</option>
                    </select>
                </div>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-2 uppercase tracking-widest">Date</label>
                        <input type="date" required className="w-full p-3 border border-gray-200 rounded-xl bg-gray-50 focus:ring-2 focus:ring-blue-500 outline-none"
                               onChange={(e) => setFormData({...formData, date: e.target.value})} />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-gray-700 mb-2 uppercase tracking-widest">Time Slot (1-24)</label>
                        <input type="number" min="1" max="24" required className="w-full p-3 border border-gray-200 rounded-xl bg-gray-50 focus:ring-2 focus:ring-blue-500 outline-none"
                               onChange={(e) => setFormData({...formData, slot: parseInt(e.target.value)})} />
                    </div>
                </div>
                <div>
                    <label className="block text-sm font-bold text-gray-700 mb-2 uppercase tracking-widest">Guests (Capacity)</label>
                    <input type="number" min="1" required className="w-full p-3 border border-gray-200 rounded-xl bg-gray-50 focus:ring-2 focus:ring-blue-500 outline-none"
                           onChange={(e) => setFormData({...formData, capacity_booked: parseInt(e.target.value)})} />
                </div>

                <button type="submit" disabled={loading} className="w-full bg-blue-600 text-white py-4 rounded-xl font-bold uppercase tracking-widest hover:bg-blue-700 transition-colors flex items-center justify-center">
                    {loading ? <Loader2 className="animate-spin w-5 h-5 mr-2" /> : <Calendar className="w-5 h-5 mr-2" />}
                    Confirm Booking
                </button>
            </form>
        </div>
    );
}