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

const ACTION_LABELS: Record<string, { label: string; color: string }> = {
    SUSPEND_USER: { label: 'Suspend User', color: 'bg-yellow-600' },
    REACTIVATE_USER: { label: 'Reactivate User', color: 'bg-green-600' },
    DELETE_USER: { label: 'Delete User', color: 'bg-red-600' },
    DEACTIVATE_ITEM: { label: 'Deactivate Item', color: 'bg-orange-600' },
    REACTIVATE_ITEM: { label: 'Reactivate Item', color: 'bg-blue-600' },
};

export default function AdminAuditPage() {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<string>('');

    const fetchLogs = async () => {
        try {
            const token = localStorage.getItem('token');
            let url = `${process.env.NEXT_PUBLIC_API_URL}/api/admin/audit`;
            if (filter) {
                url += `?action=${filter}`;
            }

            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error('Failed to fetch audit logs');
            const data = await response.json();
            setLogs(data);
        } catch (err) {
            toast.error('Failed to load audit logs');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, [filter]);

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString();
    };

    const parseDetails = (details: string | null): Record<string, string> | null => {
        if (!details) return null;
        try {
            return JSON.parse(details);
        } catch {
            return null;
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    return (
        <div>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold text-white">Audit Log</h1>

                <select
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    className="bg-gray-800 text-white border border-gray-700 rounded-lg px-4 py-2"
                >
                    <option value="">All Actions</option>
                    <option value="SUSPEND_USER">Suspend User</option>
                    <option value="REACTIVATE_USER">Reactivate User</option>
                    <option value="DELETE_USER">Delete User</option>
                    <option value="DEACTIVATE_ITEM">Deactivate Item</option>
                    <option value="REACTIVATE_ITEM">Reactivate Item</option>
                </select>
            </div>

            {logs.length === 0 ? (
                <div className="bg-gray-800 rounded-xl border border-gray-700 p-8 text-center text-gray-400">
                    No audit logs found
                </div>
            ) : (
                <div className="space-y-4">
                    {logs.map((log) => {
                        const actionInfo = ACTION_LABELS[log.action] || { label: log.action, color: 'bg-gray-600' };
                        const details = parseDetails(log.details);

                        return (
                            <div
                                key={log.id}
                                className="bg-gray-800 rounded-xl border border-gray-700 p-6 hover:border-gray-600 transition-colors"
                            >
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-4">
                                        <span className={`${actionInfo.color} px-3 py-1 rounded text-xs font-medium text-white`}>
                                            {actionInfo.label}
                                        </span>
                                        <div>
                                            <p className="text-white">
                                                <span className="font-medium">{log.adminUsername}</span>
                                                <span className="text-gray-400"> {actionInfo.label.toLowerCase()} </span>
                                                <span className="font-medium">{log.targetName}</span>
                                                <span className="text-gray-500"> ({log.targetType} #{log.targetId})</span>
                                            </p>
                                            {details?.reason && (
                                                <p className="text-gray-400 text-sm mt-1">
                                                    Reason: {details.reason}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                    <p className="text-gray-500 text-sm">{formatDate(log.createdAt)}</p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
