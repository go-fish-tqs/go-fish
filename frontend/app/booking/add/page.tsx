"use client";

import { useSearchParams, useRouter } from "next/navigation";
import { useQuery, useMutation } from "@tanstack/react-query";
import { useState, Suspense } from "react";
import toast from "react-hot-toast";
import { Item } from "@/app/items/types";
import {
  BackButton,
  ItemSummary,
  BookingDateForm,
  BookingSkeleton,
  BookingError,
  BookingCalendar,
  ItemReviews,
  PaymentModal,
} from "../components";

interface BookingResponse {
  id: number;
  status: string;
}

function BookingFormContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const itemId = searchParams.get("itemId");

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  const {
    data: item,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["item", itemId],
    queryFn: async () => {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || ''}/api/items/${itemId}`);
      if (!res.ok) throw new Error("Failed to fetch item");
      return res.json() as Promise<Item>;
    },
    enabled: !!itemId,
  });

  // Create booking mutation
  const createBookingMutation = useMutation({
    mutationFn: async (data: { userId: number; itemId: string; startDate: string; endDate: string }) => {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || ''}/api/bookings`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: data.userId,
          itemId: Number(data.itemId),
          startDate: new Date(data.startDate).toISOString(),
          endDate: new Date(data.endDate).toISOString(),
        }),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "Failed to create booking");
      }

      return response.json() as Promise<BookingResponse>;
    },
    onSuccess: (data) => {
      setBookingId(data.id);
      setShowPaymentModal(true);
      toast.success("Booking created! Please complete payment.");
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Failed to create booking");
      setIsSubmitting(false);
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!startDate || !endDate) {
      toast.error("Please select start and end dates");
      return;
    }

    if (new Date(startDate) >= new Date(endDate)) {
      toast.error("End date must be after start date");
      return;
    }

    setIsSubmitting(true);

    createBookingMutation.mutate({
      userId: 1, // TODO: Get from auth context
      itemId: itemId!,
      startDate,
      endDate,
    });
  };

  const handlePaymentSuccess = () => {
    setShowPaymentModal(false);
    toast.success("Payment successful! Your booking is confirmed.");
    router.push("/dashboard");
  };

  const handlePaymentClose = () => {
    setShowPaymentModal(false);
    setIsSubmitting(false);
    // Booking remains in PENDING status
    toast("Booking saved. You can complete payment later.", { icon: "📝" });
  };

  if (!itemId) {
    return <BookingError message="No item selected for booking." />;
  }

  if (isLoading) {
    return <BookingSkeleton />;
  }

  if (isError || !item) {
    return (
      <BookingError
        message="Failed to load item details."
        linkLabel="Back to Items"
      />
    );
  }

  return (
    <div className="h-full w-full overflow-y-auto scrollbar-hide">
      {/* Glass background layer */}
      <div className="min-h-full p-6 lg:p-10">
        <div className="max-w-6xl mx-auto animate-fade-in space-y-6">
          <BackButton href="/items" label="Back to Items" />

          {/* Main Glass Container */}
          <div className="backdrop-blur-2xl bg-gradient-to-br from-white/70 via-white/60 to-blue-50/40 border border-white/50 rounded-3xl p-6 lg:p-10 shadow-2xl shadow-blue-900/5">

            {/* Header */}
            <div className="mb-8">
              <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-gray-900 via-gray-800 to-gray-900 bg-clip-text text-transparent">
                Book This Item
              </h1>
              <p className="text-gray-500 mt-2">Complete your reservation details below</p>
            </div>

            {/* Two Column Layout */}
            <div className="flex flex-col lg:flex-row gap-8">
              <ItemSummary item={item} />

              <div className="lg:w-96">
                <BookingDateForm
                  startDate={startDate}
                  endDate={endDate}
                  onStartDateChange={setStartDate}
                  onEndDateChange={setEndDate}
                  onSubmit={handleSubmit}
                  isSubmitting={isSubmitting}
                />
              </div>
            </div>
          </div>

          {/* Calendar and Reviews Row */}
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="lg:flex-1">
              <BookingCalendar
                itemId={itemId}
                startDate={startDate}
                endDate={endDate}
                onStartDateChange={setStartDate}
                onEndDateChange={setEndDate}
              />
            </div>
            <div className="lg:w-80">
              <ItemReviews itemId={itemId} />
            </div>
          </div>
        </div>
      </div>

      {/* Payment Modal */}
      {item && (
        <PaymentModal
          isOpen={showPaymentModal}
          onClose={handlePaymentClose}
          onSuccess={handlePaymentSuccess}
          item={item}
          startDate={startDate}
          endDate={endDate}
          bookingId={bookingId}
        />
      )}
    </div>
  );
}

export default function AddBookingPage() {
  return (
    <Suspense fallback={<BookingSkeleton />}>
      <BookingFormContent />
    </Suspense>
  );
}
