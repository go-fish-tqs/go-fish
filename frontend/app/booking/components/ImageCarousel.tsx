"use client";

import { useState, useEffect, useCallback } from "react";

interface ImageCarouselProps {
    images: string[];
    initialIndex?: number;
    isOpen: boolean;
    onClose: () => void;
    itemName: string;
}

export default function ImageCarousel({
    images,
    initialIndex = 0,
    isOpen,
    onClose,
    itemName,
}: ImageCarouselProps) {
    const [currentIndex, setCurrentIndex] = useState(initialIndex);

    // Reset index when carousel opens
    useEffect(() => {
        if (isOpen) {
            setCurrentIndex(initialIndex);
        }
    }, [isOpen, initialIndex]);

    // Keyboard navigation
    const handleKeyDown = useCallback((e: KeyboardEvent) => {
        if (!isOpen) return;

        switch (e.key) {
            case "ArrowLeft":
                setCurrentIndex((prev) => (prev > 0 ? prev - 1 : images.length - 1));
                break;
            case "ArrowRight":
                setCurrentIndex((prev) => (prev < images.length - 1 ? prev + 1 : 0));
                break;
            case "Escape":
                onClose();
                break;
        }
    }, [isOpen, images.length, onClose]);

    useEffect(() => {
        document.addEventListener("keydown", handleKeyDown);
        return () => document.removeEventListener("keydown", handleKeyDown);
    }, [handleKeyDown]);

    // Prevent body scroll when modal is open
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = "hidden";
        } else {
            document.body.style.overflow = "";
        }
        return () => {
            document.body.style.overflow = "";
        };
    }, [isOpen]);

    if (!isOpen || images.length === 0) return null;

    const goToPrevious = () => {
        setCurrentIndex((prev) => (prev > 0 ? prev - 1 : images.length - 1));
    };

    const goToNext = () => {
        setCurrentIndex((prev) => (prev < images.length - 1 ? prev + 1 : 0));
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/80 backdrop-blur-md animate-fade-in"
                onClick={onClose}
            />

            {/* Modal Container */}
            <div className="relative z-10 w-full max-w-5xl mx-4 animate-scale-in">
                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute -top-12 right-0 p-2 rounded-full backdrop-blur-xl bg-white/20 border border-white/30 text-white hover:bg-white/30 transition-all duration-300 group"
                    aria-label="Close carousel"
                >
                    <svg
                        className="w-6 h-6 transition-transform group-hover:rotate-90"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M6 18L18 6M6 6l12 12"
                        />
                    </svg>
                </button>

                {/* Main Image Container */}
                <div className="relative backdrop-blur-2xl bg-white/10 border border-white/20 rounded-3xl overflow-hidden shadow-2xl">
                    {/* Image */}
                    <div className="relative aspect-[16/10] flex items-center justify-center bg-black/20">
                        <img
                            src={images[currentIndex]}
                            alt={`${itemName} - Image ${currentIndex + 1}`}
                            className="max-w-full max-h-full object-contain transition-opacity duration-300"
                        />
                    </div>

                    {/* Navigation Arrows */}
                    {images.length > 1 && (
                        <>
                            <button
                                onClick={goToPrevious}
                                className="absolute left-4 top-1/2 -translate-y-1/2 p-3 rounded-full backdrop-blur-xl bg-white/20 border border-white/30 text-white hover:bg-white/40 hover:scale-110 transition-all duration-300 group"
                                aria-label="Previous image"
                            >
                                <svg
                                    className="w-6 h-6 transition-transform group-hover:-translate-x-0.5"
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
                            <button
                                onClick={goToNext}
                                className="absolute right-4 top-1/2 -translate-y-1/2 p-3 rounded-full backdrop-blur-xl bg-white/20 border border-white/30 text-white hover:bg-white/40 hover:scale-110 transition-all duration-300 group"
                                aria-label="Next image"
                            >
                                <svg
                                    className="w-6 h-6 transition-transform group-hover:translate-x-0.5"
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
                        </>
                    )}

                    {/* Bottom Bar with Thumbnails and Counter */}
                    <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/60 to-transparent">
                        <div className="flex items-center justify-between">
                            {/* Counter */}
                            <div className="backdrop-blur-md bg-white/20 px-4 py-2 rounded-full border border-white/30">
                                <span className="text-white text-sm font-medium">
                                    {currentIndex + 1} / {images.length}
                                </span>
                            </div>

                            {/* Thumbnail Dots */}
                            {images.length > 1 && images.length <= 10 && (
                                <div className="flex items-center gap-2">
                                    {images.map((_, index) => (
                                        <button
                                            key={index}
                                            onClick={() => setCurrentIndex(index)}
                                            className={`w-2.5 h-2.5 rounded-full transition-all duration-300 ${index === currentIndex
                                                ? "bg-white scale-125 shadow-lg shadow-white/30"
                                                : "bg-white/40 hover:bg-white/70"
                                                }`}
                                            aria-label={`Go to image ${index + 1}`}
                                        />
                                    ))}
                                </div>
                            )}

                            {/* Keyboard hint */}
                            <div className="hidden md:flex items-center gap-2 text-white/60 text-xs">
                                <kbd className="px-2 py-1 rounded bg-white/10 border border-white/20">←</kbd>
                                <kbd className="px-2 py-1 rounded bg-white/10 border border-white/20">→</kbd>
                                <span>to navigate</span>
                            </div>
                        </div>

                        {/* Small Thumbnails Row (only if more than 1 image) */}
                        {images.length > 1 && (
                            <div className="flex items-center justify-center gap-2 mt-4 overflow-x-auto scrollbar-hide">
                                {images.map((url, index) => (
                                    <button
                                        key={index}
                                        onClick={() => setCurrentIndex(index)}
                                        className={`relative flex-shrink-0 w-16 h-12 rounded-lg overflow-hidden transition-all duration-300 ${index === currentIndex
                                            ? "ring-2 ring-white scale-105 shadow-lg"
                                            : "opacity-60 hover:opacity-100 hover:scale-105"
                                            }`}
                                    >
                                        <img
                                            src={url}
                                            alt={`Thumbnail ${index + 1}`}
                                            className="w-full h-full object-cover"
                                        />
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Animation Styles */}
            <style dangerouslySetInnerHTML={{
                __html: `
                @keyframes carousel-fade-in {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }
                @keyframes carousel-scale-in {
                    from {
                        opacity: 0;
                        transform: scale(0.95);
                    }
                    to {
                        opacity: 1;
                        transform: scale(1);
                    }
                }
                .animate-fade-in {
                    animation: carousel-fade-in 0.2s ease-out;
                }
                .animate-scale-in {
                    animation: carousel-scale-in 0.3s ease-out;
                }
            `}} />
        </div>
    );
}
