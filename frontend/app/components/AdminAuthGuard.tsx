'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useUser } from '../context/UserContext';

interface AdminAuthGuardProps {
    children: React.ReactNode;
}

export default function AdminAuthGuard({ children }: AdminAuthGuardProps) {
    const { user, isLoading, isAdmin } = useUser();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading) {
            if (!user) {
                // Not logged in, redirect to login
                router.push('/login?redirect=/admin');
            } else if (!isAdmin) {
                // Logged in but not admin, redirect to home
                router.push('/');
            }
        }
    }, [user, isLoading, isAdmin, router]);

    // Show loading while checking auth status
    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-900">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    // Don't render children if not authenticated or not admin
    if (!user || !isAdmin) {
        return null;
    }

    return <>{children}</>;
}
