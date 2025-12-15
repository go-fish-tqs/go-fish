"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import toast, { Toast } from "react-hot-toast";

// Mock current user ID - in real app this would come from auth context
const CURRENT_USER_ID = 1;

interface Review {
    id: number;
    username: string;
    rating: number;
    comment: string;
    createdAt: string;
    userId: number;
    itemId: number;
    itemName: string;
}

interface ReviewsResponse {
    content: Review[];
    totalElements: number;
    totalPages: number;
}

interface ItemReviewsProps {
    itemId: string;
}

function StarRating({ rating, size = "sm", interactive = false, onRate }: {
    rating: number;
    size?: "sm" | "lg";
    interactive?: boolean;
    onRate?: (rating: number) => void;
}) {
    const [hoverRating, setHoverRating] = useState(0);
    const sizeClass = size === "lg" ? "w-5 h-5" : "w-3 h-3";

    return (
        <div className="flex gap-0.5">
            {[1, 2, 3, 4, 5].map((star) => (
                <svg
                    key={star}
                    className={`${sizeClass} transition-all duration-200 ${interactive ? "cursor-pointer hover:scale-110" : ""
                        } ${star <= (hoverRating || rating)
                            ? "text-amber-400 drop-shadow-[0_0_3px_rgba(251,191,36,0.5)]"
                            : "text-gray-300/50"
                        }`}
                    fill="currentColor"
                    viewBox="0 0 20 20"
                    onClick={() => interactive && onRate?.(star)}
                    onMouseEnter={() => interactive && setHoverRating(star)}
                    onMouseLeave={() => interactive && setHoverRating(0)}
                >
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
            ))}
        </div>
    );
}

function getInitials(name: string) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

function getAvatarGradient(name: string) {
    const gradients = [
        "from-violet-500 to-purple-600",
        "from-blue-500 to-cyan-500",
        "from-emerald-500 to-teal-500",
        "from-orange-500 to-amber-500",
        "from-pink-500 to-rose-500",
        "from-indigo-500 to-blue-500",
    ];
    const index = name.charCodeAt(0) % gradients.length;
    return gradients[index];
}

export default function ItemReviews({ itemId }: ItemReviewsProps) {
    const queryClient = useQueryClient();
    const [showForm, setShowForm] = useState(false);
    const [newRating, setNewRating] = useState(0);
    const [newComment, setNewComment] = useState("");

    // Fetch reviews from API
    const { data: reviewsData, isLoading, isError } = useQuery({
        queryKey: ["reviews", itemId],
        queryFn: async () => {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/reviews/item/${itemId}?page=0&size=10`);
            if (!res.ok) throw new Error("Failed to fetch reviews");
            return res.json() as Promise<ReviewsResponse>;
        },
        enabled: !!itemId,
    });

    // Fetch average rating
    const { data: averageRating } = useQuery({
        queryKey: ["reviews", itemId, "rating"],
        queryFn: async () => {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/reviews/item/${itemId}/rating`);
            if (!res.ok) throw new Error("Failed to fetch rating");
            return res.json() as Promise<number>;
        },
        enabled: !!itemId,
    });

    // Create review mutation
    const createReviewMutation = useMutation({
        mutationFn: async (data: { rating: number; comment: string }) => {
            const token = localStorage.getItem("authToken");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/reviews`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(token && { "Authorization": `Bearer ${token}` })
                },
                body: JSON.stringify({
                    itemId: Number(itemId),
                    rating: data.rating,
                    comment: data.comment,
                }),
            });
            if (!res.ok) {
                const error = await res.json();
                throw new Error(error.message || "Failed to create review");
            }
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["reviews", itemId] });
            queryClient.invalidateQueries({ queryKey: ["reviews", itemId, "rating"] });
            setShowForm(false);
            setNewRating(0);
            setNewComment("");
            toast.success("Review submitted successfully!");
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to submit review");
        },
    });

    // Delete review mutation
    const deleteReviewMutation = useMutation({
        mutationFn: async (reviewId: number) => {
            const token = localStorage.getItem("authToken");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/reviews/${reviewId}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    ...(token && { "Authorization": `Bearer ${token}` })
                },
            });
            if (!res.ok && res.status !== 204) {
                const error = await res.json();
                throw new Error(error.message || "Failed to delete review");
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["reviews", itemId] });
            queryClient.invalidateQueries({ queryKey: ["reviews", itemId, "rating"] });
            toast.success("Review deleted successfully!");
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to delete review");
        },
    });

    const reviews = reviewsData?.content || [];
    const totalReviews = reviewsData?.totalElements || 0;
    const avgRating = averageRating ?? 0;

    // Check if current user has already reviewed
    const userHasReviewed = reviews.some(r => r.userId === CURRENT_USER_ID);

    const handleSubmitReview = (e: React.FormEvent) => {
        e.preventDefault();
        if (newRating === 0) {
            toast.error("Please select a rating");
            return;
        }
        createReviewMutation.mutate({ rating: newRating, comment: newComment });
    };

    return (
        <div className="relative h-full flex flex-col overflow-hidden rounded-2xl">
            {/* Glassmorphism Background */}
            <div className="absolute inset-0 bg-gradient-to-br from-white/80 via-white/70 to-indigo-50/60 backdrop-blur-xl" />
            <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 via-transparent to-purple-500/5" />
            <div className="absolute inset-0 border border-white/60 rounded-2xl" />

            {/* Content */}
            <div className="relative z-10 flex flex-col h-full p-4">
                {/* Header */}
                <div className="mb-4">
                    <div className="flex items-center gap-2 mb-3">
                        <div className="p-1.5 rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 shadow-lg shadow-amber-500/20">
                            <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                            </svg>
                        </div>
                        <h3 className="text-sm font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">
                            Customer Reviews
                        </h3>
                    </div>

                    {/* Rating Summary */}
                    <div className="flex items-center gap-3 p-3 rounded-xl bg-gradient-to-r from-amber-50/80 to-orange-50/80 border border-amber-200/40">
                        <div className="text-2xl font-bold bg-gradient-to-br from-amber-600 to-orange-600 bg-clip-text text-transparent">
                            {avgRating.toFixed(1)}
                        </div>
                        <div className="flex flex-col gap-0.5">
                            <StarRating rating={Math.round(avgRating)} size="lg" />
                            <span className="text-[10px] text-gray-500 font-medium">
                                {totalReviews} review{totalReviews !== 1 ? 's' : ''}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Add Review Button / Form */}
                {!userHasReviewed && (
                    <div className="mb-4">
                        {!showForm ? (
                            <button
                                onClick={() => setShowForm(true)}
                                className="w-full py-2 px-3 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-xs font-semibold shadow-lg shadow-emerald-500/25 hover:shadow-xl hover:from-emerald-600 hover:to-teal-700 transition-all duration-300 flex items-center justify-center gap-1.5"
                            >
                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                </svg>
                                Write a Review
                            </button>
                        ) : (
                            <form onSubmit={handleSubmitReview} className="p-3 rounded-xl bg-white/70 border border-emerald-200/50 space-y-3">
                                <div>
                                    <label className="text-[10px] font-semibold text-gray-600 uppercase tracking-wide mb-1 block">
                                        Your Rating
                                    </label>
                                    <StarRating
                                        rating={newRating}
                                        size="lg"
                                        interactive
                                        onRate={setNewRating}
                                    />
                                </div>
                                <div>
                                    <label className="text-[10px] font-semibold text-gray-600 uppercase tracking-wide mb-1 block">
                                        Comment (optional)
                                    </label>
                                    <textarea
                                        value={newComment}
                                        onChange={(e) => setNewComment(e.target.value)}
                                        placeholder="Share your experience..."
                                        className="w-full px-3 py-2 text-xs rounded-lg bg-white/80 border border-gray-200 focus:border-emerald-400 focus:ring-2 focus:ring-emerald-200 outline-none transition-all resize-none"
                                        rows={2}
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        type="submit"
                                        disabled={createReviewMutation.isPending || newRating === 0}
                                        className="flex-1 py-2 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-xs font-semibold disabled:opacity-50 disabled:cursor-not-allowed hover:from-emerald-600 hover:to-teal-700 transition-all"
                                    >
                                        {createReviewMutation.isPending ? "Submitting..." : "Submit"}
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setShowForm(false);
                                            setNewRating(0);
                                            setNewComment("");
                                        }}
                                        className="px-3 py-2 rounded-lg bg-gray-100 text-gray-600 text-xs font-medium hover:bg-gray-200 transition-all"
                                    >
                                        Cancel
                                    </button>
                                </div>
                                {createReviewMutation.isError && (
                                    <p className="text-[10px] text-red-500 font-medium">
                                        {createReviewMutation.error.message}
                                    </p>
                                )}
                            </form>
                        )}
                    </div>
                )}

                {/* Reviews List */}
                <div className="flex-1 overflow-y-auto space-y-2.5 min-h-0 pr-1 scrollbar-thin scrollbar-thumb-gray-300/50 scrollbar-track-transparent">
                    {isLoading ? (
                        <div className="flex items-center justify-center py-8">
                            <div className="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                        </div>
                    ) : isError ? (
                        <div className="text-center py-6 text-gray-500 text-xs">
                            Failed to load reviews
                        </div>
                    ) : reviews.length === 0 ? (
                        <div className="text-center py-6 text-gray-400 text-xs">
                            No reviews yet. Be the first to review!
                        </div>
                    ) : (
                        reviews.map((review, idx) => (
                            <div
                                key={review.id}
                                className={`group relative p-3 rounded-xl bg-white/60 border border-white/80 hover:bg-white/80 hover:border-blue-200/50 hover:shadow-lg hover:shadow-blue-500/5 transition-all duration-300 ${review.userId === CURRENT_USER_ID ? 'ring-2 ring-emerald-300/50' : ''
                                    }`}
                                style={{ animationDelay: `${idx * 100}ms` }}
                            >
                                <div className="flex items-start gap-2.5">
                                    {/* Avatar */}
                                    <div className={`flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-br ${getAvatarGradient(review.username)} flex items-center justify-center shadow-md`}>
                                        <span className="text-[10px] font-bold text-white">
                                            {getInitials(review.username)}
                                        </span>
                                    </div>

                                    {/* Content */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center justify-between gap-2 mb-1">
                                            <span className="font-semibold text-xs text-gray-800 truncate">
                                                {review.username}
                                                {review.userId === CURRENT_USER_ID && (
                                                    <span className="ml-1 text-[9px] text-emerald-600 font-medium">(You)</span>
                                                )}
                                            </span>
                                            <StarRating rating={review.rating} />
                                        </div>
                                        {review.comment && (
                                            <p className="text-[11px] text-gray-600 leading-relaxed line-clamp-2">
                                                {review.comment}
                                            </p>
                                        )}
                                        <div className="flex items-center justify-between mt-1.5">
                                            <p className="text-[9px] text-gray-400 font-medium">
                                                {new Date(review.createdAt).toLocaleDateString('en-US', {
                                                    month: 'short',
                                                    day: 'numeric',
                                                    year: 'numeric'
                                                })}
                                            </p>
                                            {review.userId === CURRENT_USER_ID && (
                                                <button
                                                    onClick={() => {
                                                        toast((t: Toast) => (
                                                            <div className="flex items-center gap-3">
                                                                <span className="text-sm">Delete this review?</span>
                                                                <div className="flex gap-2">
                                                                    <button
                                                                        onClick={() => {
                                                                            toast.dismiss(t.id);
                                                                            deleteReviewMutation.mutate(review.id);
                                                                        }}
                                                                        className="px-2 py-1 text-xs bg-red-500 text-white rounded-md hover:bg-red-600"
                                                                    >
                                                                        Delete
                                                                    </button>
                                                                    <button
                                                                        onClick={() => toast.dismiss(t.id)}
                                                                        className="px-2 py-1 text-xs bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
                                                                    >
                                                                        Cancel
                                                                    </button>
                                                                </div>
                                                            </div>
                                                        ), { duration: 5000 });
                                                    }}
                                                    disabled={deleteReviewMutation.isPending}
                                                    className="text-[9px] text-red-500 hover:text-red-700 font-medium flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-50"
                                                >
                                                    <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                    </svg>
                                                    Delete
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Footer */}
                {totalReviews > 10 && (
                    <div className="pt-3 mt-3 border-t border-gray-200/50">
                        <button className="w-full relative group overflow-hidden px-4 py-2 rounded-xl bg-gradient-to-r from-blue-500 to-indigo-600 text-white text-xs font-semibold shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 hover:from-blue-600 hover:to-indigo-700 transition-all duration-300">
                            <span className="relative z-10 flex items-center justify-center gap-1.5">
                                View All {totalReviews} Reviews
                                <svg className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                </svg>
                            </span>
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
