"use client";

import { useState } from "react";
import { Item } from "@/app/items/types";
import ImageCarousel from "./ImageCarousel";

interface ItemSummaryProps {
    item: Item;
}

export default function ItemSummary({ item }: ItemSummaryProps) {
    const [isCarouselOpen, setIsCarouselOpen] = useState(false);
    const [carouselStartIndex, setCarouselStartIndex] = useState(0);

    const hasImages = item.photoUrls && item.photoUrls.length > 0;

    const openCarousel = (index: number = 0) => {
        if (hasImages) {
            setCarouselStartIndex(index);
            setIsCarouselOpen(true);
        }
    };

    const getImageUrl = (url: string) => {
        if (!url) return "";
        if (url.startsWith("http")) return url;
        const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "";
        const path = url.startsWith("/") ? url : `/${url}`;
        return `${baseUrl}${path}`;
    };

    const carouselImages = item.photoUrls?.map(getImageUrl) || [];

    return (
        <div className="flex-1 flex flex-col">
            {/* Large Hero Image */}
            <div
                className={`relative h-80 rounded-2xl overflow-hidden mb-6 group ${hasImages ? 'cursor-pointer' : ''}`}
                onClick={() => openCarousel(0)}
            >
                {hasImages ? (
                    <>
                        <img
                            src={getImageUrl(item.photoUrls![0])}
                            alt={item.name}
                            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                        />
                        {/* Click hint overlay */}
                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors duration-300 flex items-center justify-center">
                            <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-300 backdrop-blur-sm bg-white/20 px-4 py-2 rounded-full border border-white/30">
                                <div className="flex items-center gap-2 text-white drop-shadow-lg">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
                                    </svg>
                                    <span className="text-sm font-medium">
                                        View {item.photoUrls!.length > 1 ? `all ${item.photoUrls!.length} photos` : 'photo'}
                                    </span>
                                </div>
                            </div>
                        </div>
                        {/* Image count badge */}
                        {item.photoUrls!.length > 1 && (
                            <div className="absolute top-4 right-4 backdrop-blur-xl bg-black/30 border border-white/20 px-3 py-1.5 rounded-full">
                                <span className="text-sm font-medium text-white flex items-center gap-1.5">
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                    {item.photoUrls!.length}
                                </span>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="w-full h-full bg-gradient-to-br from-blue-100 via-indigo-100 to-purple-100 flex items-center justify-center">
                        <svg className="w-20 h-20 text-blue-300/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                )}

                {/* Glass overlay with price */}
                {item.price !== undefined && (
                    <div className="absolute bottom-4 right-4 backdrop-blur-xl bg-white/30 border border-white/40 px-4 py-2 rounded-2xl shadow-lg">
                        <span className="text-2xl font-bold text-white drop-shadow-lg">
                            ${item.price.toFixed(2)}
                            <span className="text-sm font-normal opacity-80"> / day</span>
                        </span>
                    </div>
                )}

                {/* Material badge */}
                {item.material && (
                    <div className="absolute top-4 left-4 backdrop-blur-xl bg-white/30 border border-white/40 px-3 py-1.5 rounded-full">
                        <span className="text-sm font-medium text-white drop-shadow">
                            {item.material.replace(/_/g, " ")}
                        </span>
                    </div>
                )}
            </div>

            {/* Item Info - Glass Card */}
            <div className="backdrop-blur-xl bg-white/60 border border-white/40 rounded-2xl p-6 shadow-xl">
                <h2 className="text-2xl font-bold text-gray-800 mb-3">{item.name}</h2>
                <p className="text-gray-600 leading-relaxed">{item.description}</p>

                {item.category && (
                    <div className="mt-4 pt-4 border-t border-gray-200/50">
                        <span className="text-xs uppercase tracking-wide text-gray-500">Category</span>
                        <p className="text-gray-700 font-medium mt-1">{String(item.category).replace(/_/g, " ")}</p>
                    </div>
                )}
            </div>

            {/* Image Carousel Modal */}
            {hasImages && (
                <ImageCarousel
                    images={carouselImages}
                    initialIndex={carouselStartIndex}
                    isOpen={isCarouselOpen}
                    onClose={() => setIsCarouselOpen(false)}
                    itemName={item.name}
                />
            )}
        </div >
    );
}
