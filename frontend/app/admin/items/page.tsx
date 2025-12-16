'use client';

import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

interface Category {
    id: string;
    displayName: string;
    topLevel: boolean;
}

interface Item {
    id: number;
    name: string;
    description: string;
    category: Category;
    price: number;
    available: boolean;
    active: boolean;
    deactivationReason: string | null;
    owner: { id: number; username: string };
}

export default function AdminItemsPage() {
    const [items, setItems] = useState<Item[]>([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState<number | null>(null);

    const fetchItems = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/items`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) throw new Error('Failed to fetch items');
            setItems(await response.json());
        } catch {
            toast.error('Failed to load items');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchItems(); }, []);

    const handleDeactivate = async (itemId: number) => {
        const reason = prompt('Enter deactivation reason (required):');
        if (!reason) { toast.error('Deactivation reason is required'); return; }
        setActionLoading(itemId);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/items/${itemId}/deactivate`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify({ reason }),
            });
            if (!response.ok) throw new Error('Failed');
            toast.success('Item deactivated successfully');
            fetchItems();
        } catch { toast.error('Failed to deactivate item'); } finally { setActionLoading(null); }
    };

    const handleReactivate = async (itemId: number) => {
        setActionLoading(itemId);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/items/${itemId}/reactivate`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` },
            });
            if (!response.ok) throw new Error('Failed');
            toast.success('Item reactivated successfully');
            fetchItems();
        } catch { toast.error('Failed to reactivate item'); } finally { setActionLoading(null); }
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
                <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">Item Management</h1>
                <p className="text-gray-500 mt-1">Manage platform listings and item status</p>
            </div>

            <div className="backdrop-blur-xl bg-white/70 rounded-2xl border border-white/50 shadow-xl overflow-hidden">
                <table className="w-full">
                    <thead>
                        <tr className="bg-gradient-to-r from-gray-50/80 to-blue-50/80 border-b border-gray-200/50">
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Item</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Owner</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Price</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                            <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100/50">
                        {items.map((item) => (
                            <tr key={item.id} className="hover:bg-blue-50/30 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-md">
                                            <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="text-gray-800 font-medium">{item.name}</p>
                                            <p className="text-gray-500 text-sm">{item.category?.displayName}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-gray-700">{item.owner?.username || 'Unknown'}</td>
                                <td className="px-6 py-4 text-gray-800 font-semibold">€{item.price?.toFixed(2)}</td>
                                <td className="px-6 py-4">
                                    <span className={`px-3 py-1.5 rounded-full text-xs font-semibold text-white shadow-sm ${item.active ? 'bg-gradient-to-r from-emerald-500 to-green-600' : 'bg-gradient-to-r from-rose-500 to-red-600'}`}>
                                        {item.active ? 'Active' : 'Deactivated'}
                                    </span>
                                    {!item.active && item.deactivationReason && <p className="text-xs text-gray-500 mt-1 truncate max-w-[150px]">{item.deactivationReason}</p>}
                                </td>
                                <td className="px-6 py-4">
                                    {item.active ? (
                                        <button onClick={() => handleDeactivate(item.id)} disabled={actionLoading === item.id}
                                            className="px-3 py-1.5 bg-gradient-to-r from-rose-500 to-red-600 text-white text-xs font-medium rounded-lg shadow-sm disabled:opacity-50">
                                            {actionLoading === item.id ? '...' : 'Deactivate'}
                                        </button>
                                    ) : (
                                        <button onClick={() => handleReactivate(item.id)} disabled={actionLoading === item.id}
                                            className="px-3 py-1.5 bg-gradient-to-r from-emerald-500 to-green-600 text-white text-xs font-medium rounded-lg shadow-sm disabled:opacity-50">
                                            {actionLoading === item.id ? '...' : 'Reactivate'}
                                        </button>
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
