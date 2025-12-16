"use client";

import { useEffect, useState } from "react";
import ProtectedRoute from "../components/ProtectedRoute";
import Link from "next/link";
import toast from "react-hot-toast";

interface Booking {
  id: number;
  startDate: string;
  endDate: string;
  status: string;
  price: number;
  itemId: number;
  itemName: string;
  itemPhotoUrl: string;
  userId: number;
  userName: string;
}

type TabType = "my-bookings" | "my-items";

const statusColors: Record<string, string> = {
  PENDING: "bg-amber-100 text-amber-800 border-amber-200",
  CONFIRMED: "bg-emerald-100 text-emerald-800 border-emerald-200",
  ACTIVE: "bg-blue-100 text-blue-800 border-blue-200",
  COMPLETED: "bg-gray-100 text-gray-800 border-gray-200",
  CANCELLED: "bg-red-100 text-red-800 border-red-200",
};

export default function BookingsPage() {
  const [myBookings, setMyBookings] = useState<Booking[]>([]);
  const [publishedBookings, setPublishedBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<TabType>("my-bookings");

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      // Check for token with either key (accessToken or token)
      const token =
        localStorage.getItem("accessToken") || localStorage.getItem("token");

      if (!token) {
        setLoading(false);
        return;
      }

      const headers = {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      };

      // Fetch both types of bookings
      const [myRes, publishedRes] = await Promise.all([
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/bookings/my`, { headers }),
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/bookings/my-items`, {
          headers,
        }),
      ]);

      if (myRes.ok) {
        const myData = await myRes.json();
        setMyBookings(myData);
      }

      if (publishedRes.ok) {
        const publishedData = await publishedRes.json();
        setPublishedBookings(publishedData);
      }
    } catch (error) {
      console.error("Error fetching bookings:", error);
      toast.error("Failed to load bookings");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (bookingId: number, newStatus: string) => {
    try {
      const token =
        localStorage.getItem("accessToken") || localStorage.getItem("token");
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/bookings/${bookingId}/status`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ status: newStatus }),
        }
      );

      if (res.ok) {
        toast.success(`Booking ${newStatus.toLowerCase()}`);
        fetchBookings(); // Refresh the list
      } else {
        const error = await res.json();
        toast.error(error.message || "Failed to update booking");
      }
    } catch (error) {
      console.error("Error updating booking:", error);
      toast.error("Failed to update booking");
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  const BookingCard = ({
    booking,
    isOwner,
  }: {
    booking: Booking;
    isOwner: boolean;
  }) => (
    <div className="bg-white/80 backdrop-blur-xl rounded-2xl border border-white/50 shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:scale-[1.01]">
      <div className="flex">
        {/* Image */}
        <div className="w-32 h-32 flex-shrink-0 relative overflow-hidden">
          {booking.itemPhotoUrl ? (
            <img
              src={booking.itemPhotoUrl}
              alt={booking.itemName}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-blue-100 to-indigo-100 flex items-center justify-center">
              <svg
                className="w-10 h-10 text-blue-300"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
                />
              </svg>
            </div>
          )}
        </div>

        {/* Content */}
        <div className="flex-1 p-4">
          <div className="flex items-start justify-between">
            <div>
              <Link
                href={`/items/${booking.itemId}`}
                className="text-lg font-bold text-gray-800 hover:text-blue-600 transition-colors"
              >
                {booking.itemName}
              </Link>
              <p className="text-sm text-gray-500 mt-1">
                {isOwner ? (
                  <>
                    Requested by{" "}
                    <span className="font-medium text-gray-700">
                      {booking.userName}
                    </span>
                  </>
                ) : (
                  <>Booking #{booking.id}</>
                )}
              </p>
            </div>
            <span
              className={`px-3 py-1 rounded-full text-xs font-semibold border ${
                statusColors[booking.status] || statusColors.PENDING
              }`}
            >
              {booking.status}
            </span>
          </div>

          <div className="flex items-center gap-4 mt-3 text-sm text-gray-600">
            <div className="flex items-center gap-1.5">
              <svg
                className="w-4 h-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <span>
                {formatDate(booking.startDate)} - {formatDate(booking.endDate)}
              </span>
            </div>
            {booking.price && (
              <div className="flex items-center gap-1.5">
                <svg
                  className="w-4 h-4 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span className="font-medium text-gray-800">
                  €{booking.price.toFixed(2)}
                </span>
              </div>
            )}
          </div>

          {/* Actions for owners on pending bookings */}
          {isOwner && booking.status === "PENDING" && (
            <div className="flex gap-2 mt-4">
              <button
                onClick={() => handleStatusUpdate(booking.id, "CONFIRMED")}
                className="px-4 py-2 bg-emerald-500 text-white text-sm font-semibold rounded-xl hover:bg-emerald-600 transition-colors"
              >
                Confirm
              </button>
              <button
                onClick={() => handleStatusUpdate(booking.id, "CANCELLED")}
                className="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-xl hover:bg-red-600 transition-colors"
              >
                Reject
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  const currentBookings =
    activeTab === "my-bookings" ? myBookings : publishedBookings;
  const isOwnerView = activeTab === "my-items";

  return (
    <ProtectedRoute>
      <div className="min-h-screen">
        {/* Background gradient */}
        <div className="fixed inset-0 -z-10 bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/50">
          <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-blue-200/30 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 left-0 w-[500px] h-[500px] bg-indigo-200/30 rounded-full blur-3xl"></div>
        </div>

        <div className="max-w-5xl mx-auto px-6 py-10 space-y-8">
          {/* Header */}
          <div>
            <h1 className="text-4xl font-black text-gray-900 tracking-tight">
              Bookings
            </h1>
            <p className="text-gray-500 mt-2 text-lg">
              Manage your reservations and rental requests
            </p>
          </div>

          {/* Tabs */}
          <div className="flex gap-2 bg-white/60 backdrop-blur-xl rounded-2xl p-1.5 border border-white/50 shadow-lg w-fit">
            <button
              onClick={() => setActiveTab("my-bookings")}
              className={`px-6 py-3 rounded-xl font-semibold text-sm transition-all duration-300 ${
                activeTab === "my-bookings"
                  ? "bg-gradient-to-r from-blue-500 to-indigo-600 text-white shadow-lg shadow-blue-500/25"
                  : "text-gray-600 hover:bg-gray-100"
              }`}
            >
              My Bookings
              {myBookings.length > 0 && (
                <span
                  className={`ml-2 px-2 py-0.5 rounded-full text-xs ${
                    activeTab === "my-bookings"
                      ? "bg-white/20"
                      : "bg-blue-100 text-blue-600"
                  }`}
                >
                  {myBookings.length}
                </span>
              )}
            </button>
            <button
              onClick={() => setActiveTab("my-items")}
              className={`px-6 py-3 rounded-xl font-semibold text-sm transition-all duration-300 ${
                activeTab === "my-items"
                  ? "bg-gradient-to-r from-blue-500 to-indigo-600 text-white shadow-lg shadow-blue-500/25"
                  : "text-gray-600 hover:bg-gray-100"
              }`}
            >
              Published Items
              {publishedBookings.length > 0 && (
                <span
                  className={`ml-2 px-2 py-0.5 rounded-full text-xs ${
                    activeTab === "my-items"
                      ? "bg-white/20"
                      : "bg-emerald-100 text-emerald-600"
                  }`}
                >
                  {publishedBookings.length}
                </span>
              )}
            </button>
          </div>

          {/* Tab Description */}
          <div className="text-sm text-gray-500">
            {activeTab === "my-bookings" ? (
              <p>Items you've booked from other users</p>
            ) : (
              <p>Bookings on items you've published for rent</p>
            )}
          </div>

          {/* Bookings List */}
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <div className="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
            </div>
          ) : currentBookings.length === 0 ? (
            <div className="text-center py-20 bg-white/60 backdrop-blur-xl rounded-3xl border border-white/50 shadow-lg">
              <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
                <svg
                  className="w-10 h-10 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">
                No bookings yet
              </h3>
              <p className="text-gray-500 mb-6">
                {activeTab === "my-bookings"
                  ? "Start exploring and book some amazing gear!"
                  : "Once someone books your items, they'll appear here."}
              </p>
              {activeTab === "my-bookings" && (
                <Link
                  href="/items"
                  className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-semibold rounded-xl hover:shadow-lg hover:shadow-blue-500/25 transition-all"
                >
                  Browse Items
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M17 8l4 4m0 0l-4 4m4-4H3"
                    />
                  </svg>
                </Link>
              )}
            </div>
          ) : (
            <div className="space-y-4">
              {currentBookings.map((booking) => (
                <BookingCard
                  key={booking.id}
                  booking={booking}
                  isOwner={isOwnerView}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </ProtectedRoute>
  );
}
