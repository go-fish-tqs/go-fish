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
                const token = localStorage.getItem('token');
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
            <div className="m-6 backdrop-blur-xl bg-red-50/80 border border-red-200/50 rounded-2xl p-6 text-red-600">
                Error: {error}
            </div>
        );
    }

    const statCards = [
        { label: 'Active Bookings', value: stats?.activeBookings || 0, gradient: 'from-emerald-500 to-green-600', iconBg: 'bg-emerald-500/20' },
        { label: 'Pending Bookings', value: stats?.pendingBookings || 0, gradient: 'from-amber-500 to-orange-600', iconBg: 'bg-amber-500/20' },
        { label: 'Total Users', value: stats?.totalUsers || 0, gradient: 'from-blue-500 to-indigo-600', iconBg: 'bg-blue-500/20' },
        { label: 'Suspended Users', value: stats?.suspendedUsers || 0, gradient: 'from-rose-500 to-red-600', iconBg: 'bg-rose-500/20' },
        { label: 'Total Items', value: stats?.totalItems || 0, gradient: 'from-violet-500 to-purple-600', iconBg: 'bg-violet-500/20' },
        { label: 'Inactive Items', value: stats?.inactiveItems || 0, gradient: 'from-orange-500 to-amber-600', iconBg: 'bg-orange-500/20' },
    ];

    return (
        <div className="p-6 space-y-8">
            <div>
                <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">
                    Admin Dashboard
                </h1>
                <p className="text-gray-500 mt-1">Overview of your platform&apos;s performance</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {statCards.map((card) => (
                    <div
                        key={card.label}
                        className="backdrop-blur-xl bg-white/70 rounded-2xl p-6 border border-white/50 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-[1.02]"
                    >
                        <div className="flex items-center justify-between mb-4">
                            <span className={`p-3 rounded-xl ${card.iconBg}`}>
                                <svg className="w-7 h-7 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                </svg>
                            </span>
                            <span className={`bg-gradient-to-r ${card.gradient} px-3 py-1.5 rounded-full text-xs font-semibold text-white shadow-md`}>
                                {card.label}
                            </span>
                        </div>
                        <p className="text-4xl font-bold text-gray-800">{card.value}</p>
                    </div>
                ))}
            </div>

            <div className="relative overflow-hidden backdrop-blur-xl bg-gradient-to-br from-emerald-500/90 to-green-600/90 rounded-2xl p-8 border border-emerald-400/30 shadow-xl">
                <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-32 translate-x-32 blur-3xl" />
                <div className="relative z-10 flex items-center justify-between">
                    <div>
                        <p className="text-emerald-100 text-sm font-medium mb-1">Total Revenue</p>
                        <p className="text-5xl font-bold text-white">
                            €{stats?.totalRevenue?.toFixed(2) || '0.00'}
                        </p>
                    </div>
                    <span className="p-4 bg-white/20 backdrop-blur-xl rounded-2xl">
                        <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </span>
                </div>
            </div>
        </div>
    );
}
