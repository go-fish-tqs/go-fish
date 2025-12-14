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
} from "../_components";
import ProtectedRoute from "../../components/ProtectedRoute";
import { getAuthHeaders } from "../../lib/auth";

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
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // --- CALCULAR O INTERVALO DE PESQUISA (HOJE ATÉ 1 ANO) ---
  const today = new Date().toISOString().split("T")[0];
  const nextYearDate = new Date();
  nextYearDate.setFullYear(nextYearDate.getFullYear() + 1);
  const nextYear = nextYearDate.toISOString().split("T")[0];

  // 1. Query para buscar o Item (Blindada)
  const {
    data: item,
    isLoading: isLoadingItem,
    isError: isErrorItem,
    error: itemError,
  } = useQuery({
    queryKey: ["item", itemId],
    queryFn: async () => {
      // Se não houver ID, nem vale a pena ir lá
      if (!itemId) throw new Error("Item ID is missing");

      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const res = await fetch(`${apiUrl}/api/items/${itemId}`);

      // Lê o texto primeiro para não estoirar no json()
      const text = await res.text();

      if (!res.ok) {
        console.error("Erro no fetch do Item:", text);
        throw new Error(text || "Failed to fetch item");
      }

      // Tenta fazer o parse. Se falhar, é porque não é JSON válido
      try {
        return JSON.parse(text) as Item;
      } catch (err) {
        console.error("Recebi algo que nã é JSON no Item:", text);
        throw new Error("Invalid JSON response from server");
      }
    },
    enabled: !!itemId,
  });

  const { data: blockedDates = [], isLoading: isLoadingDates } = useQuery({
    queryKey: ["availability", itemId, today, nextYear],
    queryFn: async () => {
      if (!itemId) return [];

      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const res = await fetch(
        `${apiUrl}/api/items/${itemId}/unavailability?from=${today}&to=${nextYear}`
      );

      const text = await res.text();

      if (!res.ok) {
        console.error("Erro no fetch das Datas:", text);
        throw new Error("Failed to fetch availability");
      }

      // TRUQUE DO ALGARVE: Se o texto vier vazio (""), devolve array vazio
      // O Java às vezes devolve nada se a lista for vazia, e o JSON.parse estoira.

      if (!text) return [];
      console.log("here");

      try {
        console.log(JSON.parse(text));
        return JSON.parse(text) as string[];
      } catch (err) {
        console.error("Recebi algo que nã é JSON nas Datas:", text);
        return []; // Assume vazio se der erro de parse para não partir a app
      }
    },
    enabled: !!itemId,
  });

  // Create booking mutation
  const createBookingMutation = useMutation({
    mutationFn: async (data: { itemId: string; startDate: string; endDate: string }) => {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || ''}/api/bookings`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
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
    setError(null);
    setSuccess(false);

    // Validate dates
    if (!startDate || !endDate) {
      setError("Please select both start and end dates");
      return;
    }

    if (new Date(endDate) <= new Date(startDate)) {
      setError("End date must be after start date");
      return;
    }

    setIsSubmitting(true);

    createBookingMutation.mutate({
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
    // Booking remains in PENDING status - it was already created via mutation
    toast("Booking saved. You can complete payment later.", { icon: "📝" });
  };

  if (!itemId) {
    return <BookingError message="No item selected for booking." />;
  }

  // Mostra loading se estiver a carregar o item OU as datas
  if (isLoadingItem || isLoadingDates) {
    return <BookingSkeleton />;
  }

  if (isErrorItem || !item) {
    const errorMessage = itemError
      ? (itemError as any).message || "Failed to load item details"
      : "Failed to load item details";
    console.error("Item loading failed:", errorMessage);
    return <BookingError message={errorMessage} linkLabel="Back to Items" />;
  }

  return (
    <div className="h-full w-full overflow-y-auto scrollbar-hide">
      <div className="min-h-full p-6 lg:p-10 space-y-6">
        <div className="max-w-6xl mx-auto animate-fade-in">
          <BackButton href="/items" label="Back to Items" />

          <div className="backdrop-blur-2xl bg-gradient-to-br from-white/70 via-white/60 to-blue-50/40 border border-white/50 rounded-3xl p-6 lg:p-10 shadow-2xl shadow-blue-900/5">
            <div className="mb-8">
              <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-gray-900 via-gray-800 to-gray-900 bg-clip-text text-transparent">
                Book This Item
              </h1>
              <p className="text-gray-500 mt-2">
                Complete your reservation details below
              </p>
            </div>

            <div className="flex flex-col lg:flex-row gap-8 mb-8">
              {/* Item Summary */}
              <div className="lg:w-1/2">
                <ItemSummary item={item} />
              </div>

              {/* Booking Form */}
              <div className="lg:w-1/2">
                {/* Success Message */}
                {success && (
                  <div className="mb-6 p-4 rounded-xl bg-green-50 border border-green-200 shadow-sm">
                    <div className="flex items-center gap-3">
                      <svg
                        className="w-5 h-5 text-green-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                      <p className="text-green-800 font-medium">
                        Booking created successfully! Redirecting...
                      </p>
                    </div>
                  </div>
                )}

                {/* Error Message */}
                {error && (
                  <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 shadow-sm">
                    <div className="flex items-start gap-3">
                      <svg
                        className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <div className="flex-1">
                        <p className="text-red-800 font-medium">
                          Unable to create booking
                        </p>
                        <p className="text-red-700 text-sm mt-1">{error}</p>
                      </div>
                      <button
                        onClick={() => setError(null)}
                        className="text-red-600 hover:text-red-700 font-semibold text-sm flex-shrink-0"
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                )}

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
        </div>

        <div className="flex flex-col lg:flex-row gap-6 max-w-6xl mx-auto">
          <div className="lg:w-1/2">
            {/* AQUI ESTÁ A MAGIA: Passamos as datas bloqueadas ao calendário */}
            <BookingCalendar
              itemId={itemId}
              startDate={startDate}
              endDate={endDate}
              onStartDateChange={setStartDate}
              onEndDateChange={setEndDate}
              unavailableDates={blockedDates}
            />
          </div>
          <div className="lg:w-1/2">
            <ItemReviews itemId={itemId} />
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
    <ProtectedRoute>
      <Suspense fallback={<BookingSkeleton />}>
        <BookingFormContent />
      </Suspense>
    </ProtectedRoute>
  );
}
