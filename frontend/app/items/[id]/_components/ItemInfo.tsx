// app/items/[id]/_components/ItemInfo.tsx
"use client";

import { Item } from "@/app/items/types";
import Link from "next/link";
import { useEffect, useState } from "react";

interface ItemInfoProps {
  item: Item;
}

export default function ItemInfo({ item }: ItemInfoProps) {
  const [isOwner, setIsOwner] = useState(false);

  useEffect(() => {
    // Check if current user is the owner
    const userId = localStorage.getItem("userId");
    
    console.log("Debug ownership check:", {
      userId,
      itemOwnerId: item.owner?.id,
      itemOwner: item.owner,
      fullItem: item
    });
    
    if (userId && item.owner?.id) {
      const ownershipMatch = parseInt(userId) === item.owner.id;
      console.log("Is owner?", ownershipMatch);
      setIsOwner(ownershipMatch);
    } else {
      setIsOwner(false);
    }
  }, [item.owner?.id, item]);

  return (
    <div className="flex flex-col gap-4 overflow-hidden">
      <div className="overflow-hidden">
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 break-words">
          {item.name}
        </h1>
        <div className="mt-2 flex items-center gap-4">
          <span className="text-2xl font-semibold text-blue-600">
            {new Intl.NumberFormat("pt-PT", {
              style: "currency",
              currency: "EUR",
            }).format(item.price ?? 0)}
          </span>
          {!item.available && (
            <span className="inline-flex items-center rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700 ring-1 ring-inset ring-red-600/10">
              Unavailable
            </span>
          )}
        </div>
      </div>

      {/* Characteristics */}
      <div className="border-t border-b border-gray-200 py-4 overflow-hidden">
        <dl className="grid grid-cols-2 gap-x-4 gap-y-4">
          <div className="min-w-0">
            <dt className="text-sm font-medium text-gray-500">Category</dt>
            <dd className="text-sm text-gray-900 truncate">
              {typeof item.category === "object" && item.category !== null
                ? (item.category as { displayName?: string }).displayName ?? ""
                : item.category}
            </dd>
          </div>
          <div className="min-w-0">
            <dt className="text-sm font-medium text-gray-500">Material</dt>
            <dd className="text-sm text-gray-900 truncate">
              {typeof item.material === "object" && item.material !== null
                ? (item.material as { displayName?: string }).displayName ?? ""
                : item.material}
            </dd>
          </div>
        </dl>
      </div>

      {/* Description */}
      <div className="overflow-hidden">
        <h3 className="text-sm font-medium text-gray-900">Description</h3>
        <div className="mt-2 text-base text-gray-600 whitespace-pre-line break-words overflow-wrap-anywhere">
          {item.description}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="mt-4 space-y-3">
        {/* Manage Availability Button - Only for owner */}
        {isOwner && (
          <Link
            href={`/items/${item.id}/manage-availability`}
            className="w-full flex items-center justify-center gap-2 rounded-lg px-8 py-3 text-base font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 border-2 border-blue-200 hover:border-blue-300 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Manage Availability
          </Link>
        )}

        {/* Booking Button - Only for non-owners */}
        {!isOwner && (
          <Link
            href={`/booking/add?itemId=${item.id}`}
            className={`w-full flex items-center justify-center rounded-lg px-8 py-3 text-base font-medium text-white 
              ${item.available
                ? "bg-blue-600 hover:bg-blue-700 focus:ring-blue-500"
                : "bg-gray-400 cursor-not-allowed pointer-events-none"
              }`}
          >
            {item.available ? "Request booking" : "Unavailable"}
          </Link>
        )}
      </div>
    </div>
  );
}
