'use client';

import { useEffect, useState } from 'react';

interface DashboardStats {
    activeBookings: number;
    pendingBookings: number;
    totalUsers: number;
    suspendedUsers: number;
    totalItems: number;
    inactiveItems: number;
    totalRevenue: number;
}

export default function AdminDashboard() {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const token = localStorage.getItem('authToken');
                const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/dashboard`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch dashboard stats');
                }

                const data = await response.json();
                setStats(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'An error occurred');
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-500/10 border border-red-500/50 rounded-lg p-4 text-red-400">
                Error: {error}
            </div>
        );
    }

    const statCards = [
        {
            label: 'Active Bookings',
            value: stats?.activeBookings || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            ),
            color: 'bg-green-600'
        },
        {
            label: 'Pending Bookings',
            value: stats?.pendingBookings || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            ),
            color: 'bg-yellow-600'
        },
        {
            label: 'Total Users',
            value: stats?.totalUsers || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
            ),
            color: 'bg-blue-600'
        },
        {
            label: 'Suspended Users',
            value: stats?.suspendedUsers || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                </svg>
            ),
            color: 'bg-red-600'
        },
        {
            label: 'Total Items',
            value: stats?.totalItems || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
            ),
            color: 'bg-purple-600'
        },
        {
            label: 'Inactive Items',
            value: stats?.inactiveItems || 0,
            icon: (
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                </svg>
            ),
            color: 'bg-orange-600'
        },
    ];

    return (
        <div>
            <h1 className="text-3xl font-bold text-white mb-8">Admin Dashboard</h1>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {statCards.map((card) => (
                    <div
                        key={card.label}
                        className="bg-gray-800 rounded-xl p-6 border border-gray-700 hover:border-gray-600 transition-colors"
                    >
                        <div className="flex items-center justify-between mb-4">
                            <span className="p-2 bg-white/10 rounded-lg">{card.icon}</span>
                            <span className={`${card.color} px-3 py-1 rounded-full text-xs font-medium text-white`}>
                                {card.label}
                            </span>
                        </div>
                        <p className="text-4xl font-bold text-white">{card.value}</p>
                    </div>
                ))}
            </div>

            {/* Revenue Card */}
            <div className="bg-gradient-to-r from-green-600 to-emerald-600 rounded-xl p-8 border border-green-500/50">
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-green-100 text-sm font-medium mb-1">Total Revenue</p>
                        <p className="text-4xl font-bold text-white">
                            €{stats?.totalRevenue?.toFixed(2) || '0.00'}
                        </p>
                    </div>
                    <span className="p-3 bg-white/20 rounded-xl">
                        <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </span>
                </div>
            </div>
        </div>
    );
}
