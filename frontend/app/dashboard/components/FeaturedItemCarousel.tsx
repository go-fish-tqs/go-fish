"use client";

import { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { Item } from "@/app/items/types";

export default function FeaturedItemCarousel() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const [progress, setProgress] = useState(0);

  // Fetch all items
  const {
    data: items = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["featuredItems"],
    queryFn: async () => {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL || ""}/items/filter`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({}),
        }
      );
      if (!res.ok) throw new Error("Failed to fetch items");
      return res.json() as Promise<Item[]>;
    },
  });

  // Auto-rotate every 5 seconds with progress bar
  useEffect(() => {
    if (items.length <= 1) return;

    const progressInterval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) return 0;
        return prev + 2;
      });
    }, 100);

    const interval = setInterval(() => {
      setIsTransitioning(true);
      setProgress(0);
      setTimeout(() => {
        let newIndex;
        do {
          newIndex = Math.floor(Math.random() * items.length);
        } while (newIndex === currentIndex && items.length > 1);
        setCurrentIndex(newIndex);
        setIsTransitioning(false);
      }, 500);
    }, 5000);

    return () => {
      clearInterval(interval);
      clearInterval(progressInterval);
    };
  }, [items.length, currentIndex]);

  if (isLoading) {
    return (
      <div className="relative h-[480px] rounded-[2rem] overflow-hidden">
        {/* Animated gradient background */}
        <div className="absolute inset-0 bg-gradient-to-br from-slate-900 via-blue-900 to-indigo-900">
          <div className="absolute inset-0 opacity-30">
            <div className="absolute top-0 -left-4 w-72 h-72 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl animate-blob"></div>
            <div className="absolute top-0 -right-4 w-72 h-72 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000"></div>
            <div className="absolute -bottom-8 left-20 w-72 h-72 bg-indigo-500 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000"></div>
          </div>
        </div>
        {/* Glass loader */}
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="relative">
            <div className="w-20 h-20 rounded-full border-4 border-white/10"></div>
            <div className="absolute inset-0 w-20 h-20 rounded-full border-4 border-transparent border-t-white/80 animate-spin"></div>
            <div className="absolute inset-2 w-16 h-16 rounded-full border-4 border-transparent border-t-blue-400/60 animate-spin animation-delay-150"></div>
          </div>
        </div>
      </div>
    );
  }

  if (isError || items.length === 0) {
    return (
      <div className="relative h-[480px] rounded-[2rem] overflow-hidden bg-gradient-to-br from-slate-800 to-slate-900">
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-center">
            <div className="w-24 h-24 mx-auto mb-6 rounded-2xl bg-white/5 backdrop-blur-xl border border-white/10 flex items-center justify-center">
              <svg
                className="w-12 h-12 text-white/40"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
            <p className="text-xl font-semibold text-white/60">
              No items available
            </p>
            <p className="text-sm text-white/40 mt-2">
              Check back later for new gear
            </p>
          </div>
        </div>
      </div>
    );
  }

  const currentItem = items[currentIndex];

  const getCategoryDisplay = (category: unknown): string => {
    if (typeof category === "string") return category.replace(/_/g, " ");
    if (category && typeof category === "object" && "displayName" in category) {
      return String((category as { displayName: string }).displayName);
    }
    return String(category);
  };

  return (
    <div className="relative group">
      {/* Main Container with glassmorphism */}
      <div
        className={`relative h-[480px] rounded-[2rem] overflow-hidden transition-all duration-700 ease-out flex-shrink-0 ${isTransitioning
            ? "opacity-0 scale-[0.98] blur-sm"
            : "opacity-100 scale-100 blur-0"
          }`}
      >
        {/* Background Image with parallax effect */}
        <div className="absolute inset-0 scale-105 group-hover:scale-110 transition-transform duration-[2000ms] ease-out">
          {currentItem.photoUrls && currentItem.photoUrls.length > 0 ? (
            <img
              src={currentItem.photoUrls[0]}
              alt={currentItem.name}
              width={1200}
              height={480}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-violet-600 via-blue-600 to-cyan-500">
              <div className="absolute inset-0 opacity-30">
                <div className="absolute top-0 -left-4 w-96 h-96 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse"></div>
                <div className="absolute bottom-0 right-0 w-96 h-96 bg-cyan-500 rounded-full mix-blend-multiply filter blur-3xl animate-pulse animation-delay-2000"></div>
              </div>
            </div>
          )}
        </div>

        {/* Multi-layer gradient overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent opacity-90"></div>
        <div className="absolute inset-0 bg-gradient-to-r from-black/40 via-transparent to-transparent"></div>

        {/* Noise texture overlay */}
        <div className="absolute inset-0 opacity-[0.03] bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMDAiIGhlaWdodD0iMzAwIj48ZmlsdGVyIGlkPSJhIiB4PSIwIiB5PSIwIj48ZmVUdXJidWxlbmNlIGJhc2VGcmVxdWVuY3k9Ii43NSIgc3RpdGNoVGlsZXM9InN0aXRjaCIgdHlwZT0iZnJhY3RhbE5vaXNlIi8+PC9maWx0ZXI+PHJlY3Qgd2lkdGg9IjMwMCIgaGVpZ2h0PSIzMDAiIGZpbHRlcj0idXJsKCNhKSIgb3BhY2l0eT0iMC40Ii8+PC9zdmc+')]"></div>

        {/* Content */}
        <div className="absolute inset-0 flex flex-col justify-end p-10">
          <div className="max-w-2xl">
            {/* Category & Material Badges */}
            <div className="flex flex-wrap gap-3 mb-5">
              {currentItem.category && (
                <span className="px-4 py-1.5 bg-white/10 backdrop-blur-xl text-white/90 text-xs font-semibold rounded-full border border-white/20 shadow-lg">
                  {getCategoryDisplay(currentItem.category)}
                </span>
              )}
              {currentItem.material && (
                <span className="px-4 py-1.5 bg-gradient-to-r from-blue-500/80 to-indigo-500/80 backdrop-blur-xl text-white text-xs font-semibold rounded-full shadow-lg shadow-blue-500/25">
                  {getCategoryDisplay(currentItem.material)}
                </span>
              )}
            </div>

            {/* Title with gradient */}
            <h2 className="text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white via-white to-white/80 mb-4 leading-tight">
              {currentItem.name}
            </h2>

            {/* Description */}
            <p className="text-white/70 text-lg mb-8 line-clamp-2 leading-relaxed max-w-xl">
              {currentItem.description}
            </p>

            {/* Price & CTA */}
            <div className="flex items-center gap-8">
              {currentItem.price !== undefined && (
                <div className="relative">
                  <div className="text-4xl font-black text-white">
                    ${currentItem.price.toFixed(2)}
                  </div>
                  <span className="absolute -top-1 -right-12 text-xs font-medium text-white/50 uppercase tracking-wider">
                    /day
                  </span>
                </div>
              )}
              <Link
                href={`/booking/add?itemId=${currentItem.id}`}
                className="group/btn relative px-10 py-4 overflow-hidden rounded-2xl font-bold text-white shadow-2xl shadow-blue-500/30 hover:shadow-blue-500/50 transition-all duration-500 hover:scale-105"
              >
                {/* Button gradient background */}
                <div className="absolute inset-0 bg-gradient-to-r from-blue-600 via-indigo-600 to-violet-600 transition-all duration-500 group-hover/btn:scale-110"></div>
                {/* Shimmer effect */}
                <div className="absolute inset-0 opacity-0 group-hover/btn:opacity-100 transition-opacity duration-500">
                  <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent -translate-x-full group-hover/btn:translate-x-full transition-transform duration-1000"></div>
                </div>
                <span className="relative flex items-center gap-2">
                  Book Now
                  <svg
                    className="w-5 h-5 group-hover/btn:translate-x-1 transition-transform duration-300"
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
                </span>
              </Link>
            </div>
          </div>
        </div>

        {/* Featured Badge with glow */}
        <div className="absolute top-8 left-8">
          <div className="relative">
            <div className="absolute inset-0 bg-amber-400 rounded-full blur-xl opacity-40 animate-pulse"></div>
            <div className="relative flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-amber-400 via-orange-400 to-red-400 text-white font-bold text-sm rounded-full shadow-xl">
              <svg
                className="w-4 h-4 animate-pulse"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              Featured
            </div>
          </div>
        </div>

        {/* Navigation arrows */}
        <div className="absolute top-1/2 -translate-y-1/2 left-6 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
          <button
            onClick={() => {
              setIsTransitioning(true);
              setProgress(0);
              setTimeout(() => {
                setCurrentIndex((prev) =>
                  prev === 0 ? items.length - 1 : prev - 1
                );
                setIsTransitioning(false);
              }, 300);
            }}
            className="w-12 h-12 rounded-full bg-white/10 backdrop-blur-xl border border-white/20 flex items-center justify-center text-white hover:bg-white/20 transition-all duration-300 hover:scale-110"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
        </div>
        <div className="absolute top-1/2 -translate-y-1/2 right-6 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
          <button
            onClick={() => {
              setIsTransitioning(true);
              setProgress(0);
              setTimeout(() => {
                setCurrentIndex((prev) =>
                  prev === items.length - 1 ? 0 : prev + 1
                );
                setIsTransitioning(false);
              }, 300);
            }}
            className="w-12 h-12 rounded-full bg-white/10 backdrop-blur-xl border border-white/20 flex items-center justify-center text-white hover:bg-white/20 transition-all duration-300 hover:scale-110"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </div>

        {/* Progress bar */}
        <div className="absolute bottom-0 left-0 right-0 h-1 bg-white/10">
          <div
            className="h-full bg-gradient-to-r from-blue-500 via-indigo-500 to-violet-500 transition-all duration-100 ease-linear"
            style={{ width: `${progress}%` }}
          ></div>
        </div>
      </div>

      {/* Modern dots indicator */}
      <div className="flex justify-center items-center gap-3 mt-6">
        {items.slice(0, Math.min(items.length, 8)).map((_, idx) => (
          <button
            key={idx}
            onClick={() => {
              setIsTransitioning(true);
              setProgress(0);
              setTimeout(() => {
                setCurrentIndex(idx);
                setIsTransitioning(false);
              }, 300);
            }}
            className={`relative h-3 rounded-full transition-all duration-500 overflow-hidden ${idx === currentIndex ? "w-12" : "w-3 hover:w-4"
              }`}
          >
            <div
              className={`absolute inset-0 transition-all duration-300 ${idx === currentIndex
                  ? "bg-gradient-to-r from-blue-500 via-indigo-500 to-violet-500"
                  : "bg-gray-300/50 hover:bg-gray-300"
                }`}
            ></div>
            {idx === currentIndex && (
              <div
                className="absolute inset-0 bg-white/30"
                style={{ width: `${progress}%` }}
              ></div>
            )}
          </button>
        ))}
        {items.length > 8 && (
          <span className="text-xs text-gray-400 ml-2 font-medium">
            +{items.length - 8}
          </span>
        )}
      </div>
    </div>
  );
}
