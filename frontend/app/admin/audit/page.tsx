'use client';

import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

interface AuditLog {
    id: number;
    adminId: number;
    adminUsername: string;
    action: string;
    targetType: string;
    targetId: number;
    targetName: string;
    details: string | null;
    createdAt: string;
}

const ACTION_STYLES: Record<string, { label: string; gradient: string }> = {
    SUSPEND_USER: { label: 'Suspend User', gradient: 'from-amber-500 to-orange-600' },
    REACTIVATE_USER: { label: 'Reactivate User', gradient: 'from-emerald-500 to-green-600' },
    DELETE_USER: { label: 'Delete User', gradient: 'from-rose-500 to-red-600' },
    DEACTIVATE_ITEM: { label: 'Deactivate Item', gradient: 'from-orange-500 to-amber-600' },
    REACTIVATE_ITEM: { label: 'Reactivate Item', gradient: 'from-blue-500 to-indigo-600' },
};

export default function AdminAuditPage() {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<string>('');

    useEffect(() => {
        const fetchLogs = async () => {
            try {
                const token = localStorage.getItem('token');
                let url = `${process.env.NEXT_PUBLIC_API_URL}/api/admin/audit`;
                if (filter) url += `?action=${filter}`;
                const response = await fetch(url, { headers: { 'Authorization': `Bearer ${token}` } });
                if (!response.ok) throw new Error('Failed');
                setLogs(await response.json());
            } catch {
                toast.error('Failed to load audit logs');
            } finally {
                setLoading(false);
            }
        };
        fetchLogs();
    }, [filter]);

    const formatDate = (dateString: string) => new Date(dateString).toLocaleString();

    const parseDetails = (details: string | null): Record<string, string> | null => {
        if (!details) return null;
        try { return JSON.parse(details); } catch { return null; }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    return (
        <div className="p-6 space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">Audit Log</h1>
                    <p className="text-gray-500 mt-1">Track all admin actions on the platform</p>
                </div>
                <select value={filter} onChange={(e) => setFilter(e.target.value)}
                    className="backdrop-blur-xl bg-white/70 text-gray-700 border border-white/50 rounded-xl px-4 py-2.5 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-400/50">
                    <option value="">All Actions</option>
                    <option value="SUSPEND_USER">Suspend User</option>
                    <option value="REACTIVATE_USER">Reactivate User</option>
                    <option value="DELETE_USER">Delete User</option>
                    <option value="DEACTIVATE_ITEM">Deactivate Item</option>
                    <option value="REACTIVATE_ITEM">Reactivate Item</option>
                </select>
            </div>

            {logs.length === 0 ? (
                <div className="backdrop-blur-xl bg-white/70 rounded-2xl border border-white/50 p-12 text-center shadow-lg">
                    <p className="text-gray-500 text-lg">No audit logs found</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {logs.map((log) => {
                        const actionInfo = ACTION_STYLES[log.action] || { label: log.action, gradient: 'from-gray-500 to-gray-600' };
                        const details = parseDetails(log.details);
                        return (
                            <div key={log.id} className="backdrop-blur-xl bg-white/70 rounded-2xl border border-white/50 p-6 shadow-lg hover:shadow-xl transition-all">
                                <div className="flex items-start justify-between gap-4">
                                    <div className="flex items-start gap-4">
                                        <span className={`bg-gradient-to-r ${actionInfo.gradient} px-3 py-1.5 rounded-full text-xs font-semibold text-white shadow-md`}>
                                            {actionInfo.label}
                                        </span>
                                        <div>
                                            <p className="text-gray-800">
                                                <span className="font-semibold">{log.adminUsername}</span>
                                                <span className="text-gray-500"> performed </span>
                                                <span className="font-medium">{actionInfo.label.toLowerCase()}</span>
                                                <span className="text-gray-500"> on </span>
                                                <span className="font-semibold">{log.targetName}</span>
                                                <span className="text-gray-400 text-sm ml-1">({log.targetType} #{log.targetId})</span>
                                            </p>
                                            {details?.reason && <p className="text-gray-500 text-sm mt-2 italic">&quot;{details.reason}&quot;</p>}
                                        </div>
                                    </div>
                                    <p className="text-gray-500 text-sm flex-shrink-0">{formatDate(log.createdAt)}</p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
