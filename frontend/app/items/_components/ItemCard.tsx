"use client";

import Link from "next/link";
import { Item } from "@/app/items/types";
import { useState, useEffect } from "react";

interface ItemCardProps {
  item: Item;
  index?: number;
}

export default function ItemCard({ item, index = 0 }: ItemCardProps) {
  const staggerClass = `stagger-${(index % 6) + 1}`;
  const [imageError, setImageError] = useState(false);
  const [isOwner, setIsOwner] = useState(false);

  useEffect(() => {
    const userId = localStorage.getItem("userId");
    const itemOwnerId = item.owner?.id?.toString();
    setIsOwner(userId === itemOwnerId);
  }, [item.owner]);

  // If owner, go to details; if not owner, go directly to booking
  const href = isOwner ? `/items/${item.id}` : `/booking/add?itemId=${item.id}`;

  return (
    <Link
      href={href}
      className={`
        group relative bg-white rounded-2xl overflow-hidden
        shadow-lg hover:shadow-2xl
        transform hover:-translate-y-1 hover:scale-[1.02]
        transition-all duration-300 ease-out
        opacity-0 animate-fade-in ${staggerClass}
        cursor-pointer block
      `}
    >
      {/* Image Container */}
      <div className="relative w-full h-48 bg-gradient-to-br from-blue-100 to-indigo-100 overflow-hidden flex-shrink-0">
        {item.photoUrls && item.photoUrls.length > 0 && !imageError ? (
          <img
            src={item.photoUrls[0]}
            alt={item.name}
            width={400}
            height={192}
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="h-full w-full flex items-center justify-center">
            <svg className="w-16 h-16 text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}

        {/* Price Badge */}
        {item.price !== undefined && (
          <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-full shadow-md">
            <span className="text-lg font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
              ${item.price.toFixed(2)}
            </span>
          </div>
        )}

        {/* Material Badge */}
        {item.material && (
          <div className="absolute top-3 left-3 bg-blue-600/90 backdrop-blur-sm text-white text-xs font-semibold px-2.5 py-1 rounded-full">
            {item.material.replace(/_/g, " ")}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-5">
        <h2 className="text-lg font-bold text-gray-800 mb-2 line-clamp-1 group-hover:text-blue-600 transition-colors">
          {item.name}
        </h2>
        <p className="text-sm text-gray-500 line-clamp-2 mb-4">
          {item.description}
        </p>

        {/* CTA Button */}
        <span
          className="
            block w-full text-center py-2.5 px-4 
            bg-gradient-to-r from-blue-500 to-indigo-500 
            group-hover:from-blue-600 group-hover:to-indigo-600
            text-white font-medium rounded-xl
            transform transition-all duration-200
            group-hover:shadow-lg group-hover:shadow-blue-500/25
          "
        >
          Book Now
        </span>
      </div>
    </Link>
  );
}
