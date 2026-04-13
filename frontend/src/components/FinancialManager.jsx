import { useState, useEffect } from 'react';
import { Search, RefreshCw, CheckCircle, Clock } from 'lucide-react';
import api from '../api';

export default function FinancialManager() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState('');

    const fetchTransactions = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/admin/transactions?resident_name=${search}`);
            setTransactions(response.data.transactions);
        } catch (error) { console.error("Ledger sync failed"); }
        finally { setLoading(false); }
    };

    const processPayment = async (transNo) => {
        try {
            await api.post(`/payments/${transNo}/pay`);
            fetchTransactions(); // Refresh
        } catch (error) { alert("Processing failed."); }
    };

    const triggerOverdueCheck = async () => {
        try {
            await api.post('/admin/process-overdue');
            alert("Overdue scan complete. Statuses updated.");
            fetchTransactions();
        } catch (error) { alert("Cursor execution failed."); }
    };

    useEffect(() => { fetchTransactions(); }, [search]);

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                        type="text" placeholder="Search by Driver Name..."
                        className="w-full pl-10 pr-4 py-2 border rounded-lg"
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
                <button
                    onClick={triggerOverdueCheck}
                    className="flex items-center space-x-2 bg-orange-100 text-orange-600 px-4 py-2 rounded-lg text-xs font-bold hover:bg-orange-200"
                >
                    <RefreshCw className="w-3 h-3" />
                    <span>Run Overdue Scan</span>
                </button>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left">
                    <thead className="bg-gray-50 text-[10px] uppercase font-bold text-gray-400">
                    <tr><th className="px-6 py-4">TXN #</th><th className="px-6 py-4">Driver</th><th className="px-6 py-4">Unit</th><th className="px-6 py-4">Amount</th><th className="px-6 py-4">Status</th><th className="px-6 py-4">Action</th></tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100 text-sm">
                    {transactions.map((tx) => (
                        <tr key={tx.trans_no}>
                            <td className="px-6 py-4 font-mono">{tx.trans_no}</td>
                            <td className="px-6 py-4 font-bold">{tx.resident_name}</td>
                            <td className="px-6 py-4 text-gray-500">{tx.house_block}-{tx.house_unit}</td>
                            <td className="px-6 py-4 font-bold text-blue-600">₹{tx.cost}</td>
                            <td className="px-6 py-4">
                                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                                        tx.status === 'Paid' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                                    }`}>{tx.status}</span>
                            </td>
                            <td className="px-6 py-4">
                                {tx.status !== 'Paid' && (
                                    <button onClick={() => processPayment(tx.trans_no)} className="text-blue-600 hover:underline font-bold text-xs uppercase">Process Pay</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}