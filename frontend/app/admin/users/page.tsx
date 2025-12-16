'use client';

import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

interface AdminUser {
    id: number;
    username: string;
    email: string;
    location: string;
    role: string;
    status: string;
    statusReason: string | null;
    itemCount: number;
    bookingCount: number;
}

export default function AdminUsersPage() {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState<number | null>(null);

    const fetchUsers = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) throw new Error('Failed to fetch users');
            setUsers(await response.json());
        } catch {
            toast.error('Failed to load users');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchUsers(); }, []);

    const handleSuspend = async (userId: number) => {
        const reason = prompt('Enter suspension reason:');
        if (!reason) return;
        setActionLoading(userId);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}/suspend`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify({ reason }),
            });
            if (!response.ok) throw new Error('Failed');
            toast.success('User suspended successfully');
            fetchUsers();
        } catch { toast.error('Failed to suspend user'); } finally { setActionLoading(null); }
    };

    const handleReactivate = async (userId: number) => {
        setActionLoading(userId);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}/reactivate`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) throw new Error('Failed');
            toast.success('User reactivated successfully');
            fetchUsers();
        } catch { toast.error('Failed to reactivate user'); } finally { setActionLoading(null); }
    };

    const handleDelete = async (userId: number) => {
        if (!confirm('Delete this user? This cannot be undone.')) return;
        const reason = prompt('Enter deletion reason:');
        if (!reason) return;
        setActionLoading(userId);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify({ reason }),
            });
            if (!response.ok) throw new Error('Failed');
            toast.success('User deleted successfully');
            fetchUsers();
        } catch { toast.error('Failed to delete user'); } finally { setActionLoading(null); }
    };

    const getStatusBadge = (status: string) => {
        const styles: Record<string, string> = {
            'ACTIVE': 'bg-gradient-to-r from-emerald-500 to-green-600',
            'SUSPENDED': 'bg-gradient-to-r from-amber-500 to-orange-600',
            'DELETED': 'bg-gradient-to-r from-rose-500 to-red-600',
        };
        return styles[status] || 'bg-gray-500';
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
            <div>
                <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">User Management</h1>
                <p className="text-gray-500 mt-1">Manage platform users and their access</p>
            </div>

            <div className="backdrop-blur-xl bg-white/70 rounded-2xl border border-white/50 shadow-xl overflow-hidden">
                <table className="w-full">
                    <thead>
                        <tr className="bg-gradient-to-r from-gray-50/80 to-blue-50/80 border-b border-gray-200/50">
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">User</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Role</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Items</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Bookings</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100/50">
                        {users.map((user) => (
                            <tr key={user.id} className="hover:bg-blue-50/30 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-semibold text-sm shadow-md">
                                            {user.username?.charAt(0).toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="text-gray-800 font-medium">{user.username}</p>
                                            <p className="text-gray-500 text-sm">{user.email}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`px-3 py-1.5 rounded-full text-xs font-semibold text-white shadow-sm ${user.role === 'ADMIN' ? 'bg-gradient-to-r from-violet-500 to-purple-600' : 'bg-gradient-to-r from-blue-500 to-indigo-600'}`}>
                                        {user.role}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`px-3 py-1.5 rounded-full text-xs font-semibold text-white shadow-sm ${getStatusBadge(user.status)}`}>{user.status}</span>
                                    {user.statusReason && <p className="text-xs text-gray-500 mt-1 truncate max-w-[150px]">{user.statusReason}</p>}
                                </td>
                                <td className="px-6 py-4 text-gray-700 font-medium">{user.itemCount}</td>
                                <td className="px-6 py-4 text-gray-700 font-medium">{user.bookingCount}</td>
                                <td className="px-6 py-4">
                                    {user.role !== 'ADMIN' && user.status !== 'DELETED' && (
                                        <div className="flex gap-2">
                                            {user.status === 'ACTIVE' ? (
                                                <button onClick={() => handleSuspend(user.id)} disabled={actionLoading === user.id}
                                                    className="px-3 py-1.5 bg-gradient-to-r from-amber-500 to-orange-600 text-white text-xs font-medium rounded-lg shadow-sm disabled:opacity-50">
                                                    {actionLoading === user.id ? '...' : 'Suspend'}
                                                </button>
                                            ) : (
                                                <button onClick={() => handleReactivate(user.id)} disabled={actionLoading === user.id}
                                                    className="px-3 py-1.5 bg-gradient-to-r from-emerald-500 to-green-600 text-white text-xs font-medium rounded-lg shadow-sm disabled:opacity-50">
                                                    {actionLoading === user.id ? '...' : 'Reactivate'}
                                                </button>
                                            )}
                                            <button onClick={() => handleDelete(user.id)} disabled={actionLoading === user.id}
                                                className="px-3 py-1.5 bg-gradient-to-r from-rose-500 to-red-600 text-white text-xs font-medium rounded-lg shadow-sm disabled:opacity-50">
                                                {actionLoading === user.id ? '...' : 'Delete'}
                                            </button>
                                        </div>
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
