"use client";

import { useEffect, useState } from "react";
import ProtectedRoute from "../components/ProtectedRoute";
import Link from "next/link";
import toast from "react-hot-toast";

interface Category {
    id: string;
    displayName: string;
    topLevel: boolean;
}

interface Material {
    id: string;
    displayName: string;
}

interface Item {
    id: number;
    name: string;
    description: string;
    price: number;
    photoUrls: string[];
    category: Category | null;
    material?: Material | null;
    active: boolean;
}

export default function MyItemsPage() {
    const [items, setItems] = useState<Item[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchMyItems();
    }, []);

    const fetchMyItems = async () => {
        try {
            const token = localStorage.getItem("authToken");

            if (!token) {
                setLoading(false);
                return;
            }

            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/items/my`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            if (res.ok) {
                const data = await res.json();
                setItems(data);
            } else {
                toast.error("Failed to load your items");
            }
        } catch (error) {
            console.error("Error fetching items:", error);
            toast.error("Failed to load items");
        } finally {
            setLoading(false);
        }
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat("en-EU", {
            style: "currency",
            currency: "EUR",
        }).format(price);
    };

    const ItemCard = ({ item }: { item: Item }) => {
        const firstPhoto = item.photoUrls && item.photoUrls.length > 0 ? item.photoUrls[0] : null;

        return (
            <Link
                href={`/items/${item.id}`}
                className="group bg-white/80 backdrop-blur-xl rounded-2xl border border-white/50 shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:scale-[1.02]"
            >
                {/* Image */}
                <div className="relative h-48 overflow-hidden">
                    {firstPhoto ? (
                        <img
                            src={firstPhoto}
                            alt={item.name}
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                        />
                    ) : (
                        <div className="w-full h-full bg-gradient-to-br from-blue-100 to-indigo-100 flex items-center justify-center">
                            <svg className="w-16 h-16 text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                            </svg>
                        </div>
                    )}
                    {/* Status Badge */}
                    <div className={`absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-semibold ${item.active
                        ? "bg-emerald-100 text-emerald-700 border border-emerald-200"
                        : "bg-red-100 text-red-700 border border-red-200"
                        }`}>
                        {item.active ? "Active" : "Inactive"}
                    </div>
                    {/* Price Badge */}
                    <div className="absolute bottom-3 right-3 px-3 py-1.5 rounded-xl bg-white/90 backdrop-blur-sm text-sm font-bold text-gray-800 shadow-lg">
                        {formatPrice(item.price)}/day
                    </div>
                </div>

                {/* Content */}
                <div className="p-4">
                    <h3 className="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors line-clamp-1">
                        {item.name}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1 line-clamp-2">
                        {item.description}
                    </p>
                    <div className="flex items-center gap-2 mt-3">
                        {item.category && (
                            <span className="px-2 py-1 bg-blue-50 text-blue-700 text-xs font-medium rounded-lg">
                                {item.category.displayName}
                            </span>
                        )}
                        {item.material && (
                            <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-lg">
                                {item.material.displayName}
                            </span>
                        )}
                    </div>
                </div>
            </Link>
        );
    };

    return (
        <ProtectedRoute>
            <div className="min-h-screen">
                {/* Background gradient */}
                <div className="fixed inset-0 -z-10 bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/50">
                    <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-blue-200/30 rounded-full blur-3xl"></div>
                    <div className="absolute bottom-0 left-0 w-[500px] h-[500px] bg-indigo-200/30 rounded-full blur-3xl"></div>
                </div>

                <div className="max-w-6xl mx-auto px-6 py-10 space-y-8">
                    {/* Header */}
                    <div className="flex items-end justify-between">
                        <div>
                            <h1 className="text-4xl font-black text-gray-900 tracking-tight">My Items</h1>
                            <p className="text-gray-500 mt-2 text-lg">Manage your published gear listings</p>
                        </div>
                        <Link
                            href="/items/add"
                            className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-xl hover:shadow-lg hover:shadow-blue-500/25 transition-all hover:scale-105"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                            Add New Item
                        </Link>
                    </div>

                    {/* Items Grid */}
                    {loading ? (
                        <div className="flex items-center justify-center py-20">
                            <div className="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
                        </div>
                    ) : items.length === 0 ? (
                        <div className="text-center py-20 bg-white/60 backdrop-blur-xl rounded-3xl border border-white/50 shadow-lg">
                            <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-amber-100 to-orange-200 flex items-center justify-center">
                                <svg className="w-10 h-10 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                </svg>
                            </div>
                            <h3 className="text-xl font-bold text-gray-800 mb-2">No items published yet</h3>
                            <p className="text-gray-500 mb-6">Start earning by listing your fishing gear for rent!</p>
                            <Link
                                href="/items/add"
                                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-xl hover:shadow-lg hover:shadow-blue-500/25 transition-all"
                            >
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                                </svg>
                                Add Your First Item
                            </Link>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {items.map((item) => (
                                <ItemCard key={item.id} item={item} />
                            ))}
                        </div>
                    )}

                    {/* Stats Summary */}
                    {items.length > 0 && (
                        <div className="bg-white/60 backdrop-blur-xl rounded-2xl border border-white/50 shadow-lg p-6">
                            <div className="grid grid-cols-3 gap-6 text-center">
                                <div>
                                    <p className="text-3xl font-bold text-gray-800">{items.length}</p>
                                    <p className="text-sm text-gray-500">Total Items</p>
                                </div>
                                <div>
                                    <p className="text-3xl font-bold text-emerald-600">{items.filter(i => i.active).length}</p>
                                    <p className="text-sm text-gray-500">Active</p>
                                </div>
                                <div>
                                    <p className="text-3xl font-bold text-red-600">{items.filter(i => !i.active).length}</p>
                                    <p className="text-sm text-gray-500">Inactive</p>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </ProtectedRoute>
    );
}
