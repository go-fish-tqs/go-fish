"use client";

interface Review {
    id: number;
    userName: string;
    rating: number;
    comment: string;
    date: string;
    avatar?: string;
}

interface ItemReviewsProps {
    itemId: string;
}

// Placeholder reviews data
const mockReviews: Review[] = [
    {
        id: 1,
        userName: "John D.",
        rating: 5,
        comment: "Excellent item, exactly as described!",
        date: "2024-12-10",
    },
    {
        id: 2,
        userName: "Sarah M.",
        rating: 4,
        comment: "Great quality, fast delivery.",
        date: "2024-12-08",
    },
    {
        id: 3,
        userName: "Mike R.",
        rating: 5,
        comment: "Would definitely rent again!",
        date: "2024-12-05",
    },
];

function StarRating({ rating, size = "sm" }: { rating: number; size?: "sm" | "lg" }) {
    const sizeClass = size === "lg" ? "w-4 h-4" : "w-3 h-3";
    return (
        <div className="flex gap-0.5">
            {[1, 2, 3, 4, 5].map((star) => (
                <svg
                    key={star}
                    className={`${sizeClass} transition-all duration-200 ${star <= rating
                            ? "text-amber-400 drop-shadow-[0_0_3px_rgba(251,191,36,0.5)]"
                            : "text-gray-300/50"
                        }`}
                    fill="currentColor"
                    viewBox="0 0 20 20"
                >
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
            ))}
        </div>
    );
}

function getInitials(name: string) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
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
    // TODO: Replace with actual API call
    const reviews = mockReviews;
    const averageRating = reviews.reduce((acc, r) => acc + r.rating, 0) / reviews.length;

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
                            {averageRating.toFixed(1)}
                        </div>
                        <div className="flex flex-col gap-0.5">
                            <StarRating rating={Math.round(averageRating)} size="lg" />
                            <span className="text-[10px] text-gray-500 font-medium">
                                {reviews.length} reviews
                            </span>
                        </div>
                    </div>
                </div>

                {/* Reviews List */}
                <div className="flex-1 overflow-y-auto space-y-2.5 min-h-0 pr-1 scrollbar-thin scrollbar-thumb-gray-300/50 scrollbar-track-transparent">
                    {reviews.map((review, idx) => (
                        <div
                            key={review.id}
                            className="group relative p-3 rounded-xl bg-white/60 border border-white/80 hover:bg-white/80 hover:border-blue-200/50 hover:shadow-lg hover:shadow-blue-500/5 transition-all duration-300"
                            style={{ animationDelay: `${idx * 100}ms` }}
                        >
                            <div className="flex items-start gap-2.5">
                                {/* Avatar */}
                                <div className={`flex-shrink-0 w-8 h-8 rounded-full bg-gradient-to-br ${getAvatarGradient(review.userName)} flex items-center justify-center shadow-md`}>
                                    <span className="text-[10px] font-bold text-white">
                                        {getInitials(review.userName)}
                                    </span>
                                </div>

                                {/* Content */}
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center justify-between gap-2 mb-1">
                                        <span className="font-semibold text-xs text-gray-800 truncate">
                                            {review.userName}
                                        </span>
                                        <StarRating rating={review.rating} />
                                    </div>
                                    <p className="text-[11px] text-gray-600 leading-relaxed line-clamp-2">
                                        {review.comment}
                                    </p>
                                    <p className="text-[9px] text-gray-400 mt-1.5 font-medium">
                                        {new Date(review.date).toLocaleDateString('en-US', {
                                            month: 'short',
                                            day: 'numeric',
                                            year: 'numeric'
                                        })}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Footer */}
                <div className="pt-3 mt-3 border-t border-gray-200/50">
                    <button className="w-full relative group overflow-hidden px-4 py-2 rounded-xl bg-gradient-to-r from-blue-500 to-indigo-600 text-white text-xs font-semibold shadow-lg shadow-blue-500/25 hover:shadow-xl hover:shadow-blue-500/30 hover:from-blue-600 hover:to-indigo-700 transition-all duration-300">
                        <span className="relative z-10 flex items-center justify-center gap-1.5">
                            View All Reviews
                            <svg className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                            </svg>
                        </span>
                    </button>
                </div>
            </div>
        </div>
    );
}
