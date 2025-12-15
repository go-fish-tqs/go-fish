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
    owner: {
        id: number;
        username: string;
    };
}

export default function AdminItemsPage() {
    const [items, setItems] = useState<Item[]>([]);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState<number | null>(null);

    const fetchItems = async () => {
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/items`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error('Failed to fetch items');
            const data = await response.json();
            setItems(data);
        } catch (err) {
            toast.error('Failed to load items');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchItems();
    }, []);

    const handleDeactivate = async (itemId: number) => {
        const reason = prompt('Enter deactivation reason (required):');
        if (!reason) {
            toast.error('Deactivation reason is required');
            return;
        }

        setActionLoading(itemId);
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/items/${itemId}/deactivate`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ reason }),
            });

            if (!response.ok) throw new Error('Failed to deactivate item');
            toast.success('Item deactivated successfully');
            fetchItems();
        } catch (err) {
            toast.error('Failed to deactivate item');
        } finally {
            setActionLoading(null);
        }
    };

    const handleReactivate = async (itemId: number) => {
        setActionLoading(itemId);
        try {
            const token = localStorage.getItem('authToken');
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/items/${itemId}/reactivate`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error('Failed to reactivate item');
            toast.success('Item reactivated successfully');
            fetchItems();
        } catch (err) {
            toast.error('Failed to reactivate item');
        } finally {
            setActionLoading(null);
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
            <h1 className="text-3xl font-bold text-white mb-8">Item Management</h1>

            <div className="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
                <table className="w-full">
                    <thead className="bg-gray-900">
                        <tr>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Item</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Owner</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Price</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-4 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {items.map((item) => (
                            <tr key={item.id} className="hover:bg-gray-700/50 transition-colors">
                                <td className="px-6 py-4">
                                    <div>
                                        <p className="text-white font-medium">{item.name}</p>
                                        <p className="text-gray-400 text-sm">{item.category?.displayName}</p>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-gray-300">{item.owner?.username || 'Unknown'}</td>
                                <td className="px-6 py-4 text-white">€{item.price?.toFixed(2)}</td>
                                <td className="px-6 py-4">
                                    <div className="flex flex-col gap-1">
                                        <span className={`px-2 py-1 rounded text-xs font-medium inline-block w-fit ${item.active ? 'bg-green-600 text-green-100' : 'bg-red-600 text-red-100'
                                            }`}>
                                            {item.active ? 'Active' : 'Deactivated'}
                                        </span>
                                        {!item.active && item.deactivationReason && (
                                            <p className="text-xs text-gray-500">{item.deactivationReason}</p>
                                        )}
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    {item.active ? (
                                        <button
                                            onClick={() => handleDeactivate(item.id)}
                                            disabled={actionLoading === item.id}
                                            className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white text-xs rounded disabled:opacity-50"
                                        >
                                            {actionLoading === item.id ? '...' : 'Deactivate'}
                                        </button>
                                    ) : (
                                        <button
                                            onClick={() => handleReactivate(item.id)}
                                            disabled={actionLoading === item.id}
                                            className="px-3 py-1 bg-green-600 hover:bg-green-700 text-white text-xs rounded disabled:opacity-50"
                                        >
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
