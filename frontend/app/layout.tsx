"use client";

import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Sidebar from "@/app/ui/Sidebar";
import Providers from "@/app/providers";
import { Toaster } from "react-hot-toast";
import CLSMonitor from "@/app/components/CLSMonitor";
import SuspendedBanner from "@/app/components/SuspendedBanner";
import { usePathname } from "next/navigation";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const isAuthPage = pathname === '/login' || pathname === '/register';

  return (
    <html lang="en" className="h-full">
      <head>
        <title>GoFish - Fishing Equipment Rental Platform</title>
        <meta name="description" content="Rent high-quality fishing equipment easily. Browse rods, reels, boats and more. Your perfect fishing adventure starts here." />
      </head>
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased h-full flex overflow-hidden`}>
        <CLSMonitor />
        <Providers>
          <SuspendedBanner />
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3000,
              style: {
                background: "rgba(255, 255, 255, 0.9)",
                backdropFilter: "blur(10px)",
                border: "1px solid rgba(255, 255, 255, 0.5)",
                borderRadius: "12px",
                padding: "12px 16px",
                fontSize: "14px",
              },
              success: {
                iconTheme: { primary: "#10b981", secondary: "#fff" },
              },
              error: {
                iconTheme: { primary: "#ef4444", secondary: "#fff" },
              },
            }}
          />
          {!isAuthPage && <Sidebar />}

          <main className={`flex-1 h-full min-w-0 overflow-y-auto bg-blue-100 ${isAuthPage ? 'w-full' : ''}`}>
            {children}
          </main>
        </Providers>
      </body>
    </html>
  );
}
