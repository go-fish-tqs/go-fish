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
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error('Failed to fetch users');
            const data = await response.json();
            setUsers(data);
        } catch (err) {
            toast.error('Failed to load users');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleSuspend = async (userId: number) => {
        const reason = prompt('Enter suspension reason:');
        if (!reason) return;

        setActionLoading(userId);
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}/suspend`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ reason }),
            });

            if (!response.ok) throw new Error('Failed to suspend user');
            toast.success('User suspended successfully');
            fetchUsers();
        } catch (err) {
            toast.error('Failed to suspend user');
        } finally {
            setActionLoading(null);
        }
    };

    const handleReactivate = async (userId: number) => {
        setActionLoading(userId);
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}/reactivate`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error('Failed to reactivate user');
            toast.success('User reactivated successfully');
            fetchUsers();
        } catch (err) {
            toast.error('Failed to reactivate user');
        } finally {
            setActionLoading(null);
        }
    };

    const handleDelete = async (userId: number) => {
        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) return;
        const reason = prompt('Enter deletion reason:');
        if (!reason) return;

        setActionLoading(userId);
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ reason }),
            });

            if (!response.ok) throw new Error('Failed to delete user');
            toast.success('User deleted successfully');
            fetchUsers();
        } catch (err) {
            toast.error('Failed to delete user');
        } finally {
            setActionLoading(null);
        }
    };

    const getStatusBadge = (status: string) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-600 text-green-100';
            case 'SUSPENDED':
                return 'bg-yellow-600 text-yellow-100';
            case 'DELETED':
                return 'bg-red-600 text-red-100';
            default:
                return 'bg-gray-600 text-gray-100';
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
            <h1 className="text-3xl font-bold text-white mb-8">User Management</h1>

            <div className="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
                <table className="w-full">
                    <thead className="bg-gray-900">
                        <tr>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">User</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Role</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Items</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Bookings</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {users.map((user) => (
                            <tr key={user.id} className="hover:bg-gray-700/50 transition-colors">
                                <td className="px-6 py-4">
                                    <div>
                                        <p className="text-white font-medium">{user.username}</p>
                                        <p className="text-gray-400 text-sm">{user.email}</p>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 rounded text-xs font-medium ${user.role === 'ADMIN' ? 'bg-purple-600 text-purple-100' : 'bg-blue-600 text-blue-100'
                                        }`}>
                                        {user.role}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 rounded text-xs font-medium ${getStatusBadge(user.status)}`}>
                                        {user.status}
                                    </span>
                                    {user.statusReason && (
                                        <p className="text-xs text-gray-500 mt-1">{user.statusReason}</p>
                                    )}
                                </td>
                                <td className="px-6 py-4 text-white">{user.itemCount}</td>
                                <td className="px-6 py-4 text-white">{user.bookingCount}</td>
                                <td className="px-6 py-4">
                                    {user.role !== 'ADMIN' && user.status !== 'DELETED' && (
                                        <div className="flex gap-2">
                                            {user.status === 'ACTIVE' ? (
                                                <button
                                                    onClick={() => handleSuspend(user.id)}
                                                    disabled={actionLoading === user.id}
                                                    className="px-3 py-1 bg-yellow-600 hover:bg-yellow-700 text-white text-xs rounded disabled:opacity-50"
                                                >
                                                    {actionLoading === user.id ? '...' : 'Suspend'}
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={() => handleReactivate(user.id)}
                                                    disabled={actionLoading === user.id}
                                                    className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-xs rounded disabled:opacity-50"
                                                >
                                                    {actionLoading === user.id ? '...' : 'Reactivate'}
                                                </button>
                                            )}
                                            <button
                                                onClick={() => handleDelete(user.id)}
                                                disabled={actionLoading === user.id}
                                                className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white text-xs rounded disabled:opacity-50"
                                            >
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
