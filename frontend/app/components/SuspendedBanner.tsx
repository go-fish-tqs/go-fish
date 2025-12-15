'use client';

import { useUser } from '../context/UserContext';
import { useEffect } from 'react';
import { logout } from '../lib/auth';

export default function SuspendedBanner() {
    const { user } = useUser();

    const isSuspended = user?.status === 'SUSPENDED';

    useEffect(() => {
        if (isSuspended) {
            // Add a class to the body to prevent scrolling or other interactions if needed
            // For now, we'll just use the overlay to block clicks
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'auto';
        }

        return () => {
            document.body.style.overflow = 'auto';
        };
    }, [isSuspended]);

    if (!isSuspended) return null;

    return (
        <div className="fixed inset-0 z-[9999] pointer-events-none">
            {/* Banner at the top */}
            <div className="bg-red-600 text-white px-4 py-3 shadow-lg pointer-events-auto flex justify-between items-center">
                <div className="flex items-center gap-3">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <div>
                        <p className="font-bold">Account Suspended</p>
                        <p className="text-sm">Your account has been suspended. You can browse but cannot perform any actions.</p>
                    </div>
                </div>
                <button
                    onClick={() => logout()}
                    className="bg-white text-red-600 px-4 py-1 rounded font-medium hover:bg-gray-100 transition-colors"
                >
                    Logout
                </button>
            </div>

            {/* Invisible overlay to block interactions with the rest of the page */}
            <div className="absolute inset-0 top-[60px] bg-transparent pointer-events-auto cursor-not-allowed" />
        </div>
    );
}
