"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function Home() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem("authToken");
    if (token) {
      setIsAuthenticated(true);
      // Redirect to dashboard if already logged in
      router.push("/dashboard");
    } else {
      setIsLoading(false);
    }
  }, [router]);

  if (isLoading || isAuthenticated) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-slate-900 dark:to-slate-800">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="relative h-full w-full overflow-hidden">
      {/* Animated Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-slate-900 dark:via-slate-900 dark:to-indigo-950" />

      {/* Animated Gradient Orbs */}
      <div className="absolute top-1/4 -left-20 w-96 h-96 bg-blue-400/30 rounded-full blur-3xl animate-pulse" />
      <div className="absolute bottom-1/4 -right-20 w-96 h-96 bg-indigo-400/30 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-gradient-radial from-blue-200/20 via-indigo-200/10 to-transparent rounded-full blur-3xl" />

      {/* Content */}
      <div className="relative z-10 h-full flex flex-col items-center justify-center px-6">
        {/* Logo Badge */}
        <div className="inline-flex items-center gap-3 px-5 py-2.5 mb-8 rounded-full bg-gradient-to-r from-blue-100/40 to-indigo-100/40 dark:from-blue-900/30 dark:to-indigo-900/30 backdrop-blur-sm">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500/80 to-indigo-600/80 flex items-center justify-center">
            <svg className="w-5 h-5 text-white/90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12a8 8 0 01-8 8m8-8a8 8 0 00-8-8m8 8h-8m0 8a8 8 0 01-8-8m8 8v-8m-8 0a8 8 0 018-8m-8 8h8m0-8v8" />
            </svg>
          </div>
          <span className="text-sm font-semibold text-blue-700/80 dark:text-blue-300/80">
            GoFish Platform
          </span>
        </div>

        {/* Heading */}
        <h1 className="text-5xl sm:text-7xl font-bold text-center mb-6 bg-clip-text text-transparent bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 dark:from-blue-400 dark:via-indigo-400 dark:to-purple-400">
          Welcome to GoFish
        </h1>

        {/* Subtitle */}
        <p className="text-xl sm:text-2xl text-gray-600 dark:text-gray-300 text-center max-w-2xl mb-12">
          Rent premium fishing gear from local owners. Start your fishing adventure today!
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 w-full max-w-md">
          <Link
            href="/register"
            className="flex-1 group relative px-8 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold rounded-xl shadow-lg transition-all duration-200 hover:shadow-2xl hover:scale-105 text-center"
          >
            <span className="relative z-10">Create Account</span>
            <div className="absolute inset-0 rounded-xl bg-gradient-to-r from-blue-700 to-indigo-700 opacity-0 group-hover:opacity-100 transition-opacity duration-200" />
          </Link>

          <Link
            href="/login"
            className="flex-1 px-8 py-4 bg-white/80 dark:bg-slate-800/80 hover:bg-white dark:hover:bg-slate-800 text-blue-600 dark:text-blue-400 font-semibold rounded-xl shadow-lg backdrop-blur-sm border border-blue-200/50 dark:border-blue-800/50 transition-all duration-200 hover:shadow-2xl hover:scale-105 text-center"
          >
            Sign In
          </Link>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mt-16 max-w-4xl">
          <div className="p-6 rounded-2xl bg-white/60 dark:bg-slate-800/60 backdrop-blur-sm border border-blue-100/50 dark:border-blue-900/50">
            <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Browse Gear</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Explore a wide variety of fishing equipment available for rent
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white/60 dark:bg-slate-800/60 backdrop-blur-sm border border-blue-100/50 dark:border-blue-900/50">
            <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Easy Booking</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Reserve your gear with just a few clicks and flexible dates
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white/60 dark:bg-slate-800/60 backdrop-blur-sm border border-blue-100/50 dark:border-blue-900/50">
            <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4">
              <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Secure Payments</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Safe and secure transactions with our trusted payment system
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}