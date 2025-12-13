"use client";

import { useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useState, Suspense } from "react";
import { Item } from "@/app/items/types";
import {
  BackButton,
  ItemSummary,
  BookingDateForm,
  BookingSkeleton,
  BookingError,
  BookingCalendar,
  ItemReviews,
} from "../components";

function BookingFormContent() {
  const searchParams = useSearchParams();
  const itemId = searchParams.get("itemId");

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    data: item,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["item", itemId],
    queryFn: async () => {
      const res = await fetch(`${process.env.API_URL}/api/items/${itemId}`);
      if (!res.ok) throw new Error("Failed to fetch item");
      return res.json() as Promise<Item>;
    },
    enabled: !!itemId,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const bookingData = {
        itemId,
        startDate,
        endDate,
        userId: 1,
      };

      console.log("Creating booking:", bookingData);
      alert("Booking functionality coming soon!");
    } catch (error) {
      console.error("Error creating booking:", error);
      alert("Failed to create booking");
    } finally {
      setIsSubmitting(false);
    }
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
