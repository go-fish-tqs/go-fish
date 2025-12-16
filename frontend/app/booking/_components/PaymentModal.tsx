"use client";

import { useState, useEffect } from "react";
import { Elements } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import PaymentForm from "./PaymentForm";
import { Item } from "@/app/items/types";

const stripePromise = loadStripe(
    process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY!
);

interface PaymentModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    item: Item;
    startDate: string;
    endDate: string;
    bookingId: number | null;
}

export default function PaymentModal({
    isOpen,
    onClose,
    onSuccess,
    item,
    startDate,
    endDate,
    bookingId,
}: PaymentModalProps) {
    const [clientSecret, setClientSecret] = useState<string | null>(null);
    const [amount, setAmount] = useState<number>(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isProcessing, setIsProcessing] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Calculate days and amount
    useEffect(() => {
        if (isOpen && bookingId && item.price) {
            createPaymentIntent();
        }
    }, [isOpen, bookingId]);

    const createPaymentIntent = async () => {
        if (!bookingId || !item.price) return;

        setIsLoading(true);
        setError(null);

        try {
            // Calculate days
            const start = new Date(startDate);
            const end = new Date(endDate);
            const days = Math.max(1, Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)));
            const calculatedAmount = Math.round(item.price * 100 * days);
            setAmount(calculatedAmount);

            const token = localStorage.getItem("authToken");

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || ''}/api/payments/create-intent`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({
                    bookingId: bookingId,
                    amount: calculatedAmount,
                    currency: "eur",
                }),
            });

            if (!response.ok) {
                throw new Error("Failed to create payment intent");
            }

            const data = await response.json();
            setClientSecret(data.clientSecret);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Payment initialization failed");
        } finally {
            setIsLoading(false);
        }
    };

    const handlePaymentSuccess = async () => {
        // Confirm payment with backend
        if (clientSecret) {
            try {
                const token = localStorage.getItem("authToken");
                const paymentIntentId = clientSecret.split("_secret_")[0];
                await fetch(`${process.env.NEXT_PUBLIC_API_URL || ''}/api/payments/confirm`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                        paymentIntentId,
                        bookingId,
                    }),
                });
            } catch (err) {
                console.error("Error confirming payment:", err);
            }
        }
        setIsProcessing(false);
        onSuccess();
    };

    const handlePaymentError = (message: string) => {
        setError(message);
        setIsProcessing(false);
    };

    // Calculate display values
    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;
    const days = start && end ? Math.max(1, Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))) : 0;

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-sm"
                onClick={!isProcessing ? onClose : undefined}
            />

            {/* Modal */}
            <div className="relative z-10 w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
                <div className="backdrop-blur-2xl bg-gradient-to-br from-white/90 via-white/85 to-blue-50/70 border border-white/50 rounded-3xl shadow-2xl overflow-hidden">
                    {/* Header */}
                    <div className="p-6 border-b border-gray-200/50">
                        <div className="flex items-center justify-between">
                            <h2 className="text-2xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
                                Complete Payment
                            </h2>
                            {!isProcessing && (
                                <button
                                    onClick={onClose}
                                    className="p-2 rounded-full hover:bg-gray-100/80 transition-colors"
                                >
                                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Order Summary */}
                    <div className="p-6 border-b border-gray-200/50">
                        <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-4">Order Summary</h3>
                        <div className="space-y-3">
                            <div className="flex justify-between">
                                <span className="text-gray-600">{item.name}</span>
                                <span className="text-gray-900 font-medium">€{item.price?.toFixed(2)}/day</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-gray-600">Duration</span>
                                <span className="text-gray-900 font-medium">{days} {days === 1 ? 'day' : 'days'}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-gray-600">Dates</span>
                                <span className="text-gray-900 font-medium text-sm">
                                    {start?.toLocaleDateString()} - {end?.toLocaleDateString()}
                                </span>
                            </div>
                            <div className="pt-3 border-t border-gray-200">
                                <div className="flex justify-between">
                                    <span className="text-lg font-semibold text-gray-900">Total</span>
                                    <span className="text-lg font-bold text-emerald-600">€{(amount / 100).toFixed(2)}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Payment Form */}
                    <div className="p-6">
                        {isLoading ? (
                            <div className="flex flex-col items-center justify-center py-12">
                                <svg className="animate-spin w-8 h-8 text-emerald-500 mb-4" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                </svg>
                                <p className="text-gray-500">Initializing payment...</p>
                            </div>
                        ) : error ? (
                            <div className="backdrop-blur-xl bg-red-50/80 border border-red-200/50 rounded-xl p-6 text-center">
                                <svg className="w-12 h-12 mx-auto text-red-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <p className="text-red-600 font-medium mb-4">{error}</p>
                                <button
                                    onClick={createPaymentIntent}
                                    className="px-4 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 transition-colors"
                                >
                                    Try Again
                                </button>
                            </div>
                        ) : clientSecret ? (
                            <Elements
                                stripe={stripePromise}
                                options={{
                                    clientSecret,
                                    appearance: {
                                        theme: 'stripe',
                                        variables: {
                                            colorPrimary: '#10b981',
                                            borderRadius: '12px',
                                        },
                                    },
                                }}
                            >
                                <PaymentForm
                                    onSuccess={handlePaymentSuccess}
                                    onError={handlePaymentError}
                                    amount={amount}
                                    isProcessing={isProcessing}
                                    setIsProcessing={setIsProcessing}
                                />
                            </Elements>
                        ) : null}
                    </div>

                    {/* Test Card Info */}
                    <div className="px-6 pb-6">
                        <div className="backdrop-blur-md bg-blue-50/50 border border-blue-200/50 rounded-xl p-4">
                            <div className="flex items-start gap-3">
                                <svg className="w-5 h-5 text-blue-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <div>
                                    <p className="text-sm font-medium text-blue-800">Test Mode</p>
                                    <p className="text-xs text-blue-600 mt-1">Use card: 4242 4242 4242 4242</p>
                                    <p className="text-xs text-blue-600">Any future date, any 3-digit CVV</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
