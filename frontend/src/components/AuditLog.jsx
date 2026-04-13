import { useState, useEffect } from 'react';
import { ShieldCheck, Clock } from 'lucide-react';
import api from '../api';

export default function AuditLog() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchLogs = async () => {
            setLoading(true);
            try {
                const response = await api.get('/admin/audit-logs');
                setLogs(response.data.logs);
            } catch (error) { console.error("Forensic scan failed"); }
            finally { setLoading(false); }
        };
        fetchLogs();
    }, []);

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex items-center justify-between">
                <div className="flex items-center space-x-2">
                    <ShieldCheck className="text-blue-600 w-5 h-5" />
                    <h2 className="text-lg font-bold text-gray-800 uppercase tracking-tighter">Forensic Audit Ledger</h2>
                </div>
                <span className="text-[10px] bg-blue-50 text-blue-600 px-2 py-1 rounded-full font-bold">TAMPER-EVIDENT</span>
            </div>
            <div className="divide-y divide-gray-50 max-h-[400px] overflow-y-auto">
                {loading ? <div className="p-10 text-center text-gray-400 italic">Scanning logs...</div> :
                    logs.map((log) => (
                        <div key={log.log_id} className="p-4 hover:bg-gray-50 flex items-start space-x-4">
                            <Clock className="w-4 h-4 text-gray-300 mt-1" />
                            <div>
                                <p className="text-sm font-bold text-gray-900">
                                    <span className="text-blue-600 uppercase text-[10px] mr-2">[{log.action_type}]</span>
                                    {log.table_affected} (Record: {log.record_id})
                                </p>
                                <p className="text-xs text-gray-500 mt-1">{log.details}</p>
                                <p className="text-[10px] text-gray-400 mt-2 font-mono uppercase italic">{new Date(log.changed_at).toLocaleString()}</p>
                            </div>
                        </div>
                    ))}
            </div>
        </div>
    );
}