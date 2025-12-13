import React from "react";
import Link from "next/link";
import { Item } from "@/app/items/types"; // Adjust import path
import ItemCard from "@/app/items/_components/ItemCard"; // Adjust import path

interface ItemsGridProps {
  items: Item[];
  isLoading: boolean;
  isError: boolean;
}

export default function ItemsGrid({
  items,
  isLoading,
  isError,
}: ItemsGridProps) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin">
          <svg
            className="w-12 h-12 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="text-center py-12 bg-red-50 rounded-xl border border-red-100">
        <p className="text-red-600 font-medium">Failed to load items.</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="text-center py-12 bg-gray-50 rounded-xl border border-gray-100">
        <p className="text-gray-500 text-lg">No items match your filters.</p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto gap-6 grid grid-cols-3 sm:grid-cols-1 lg:grid-cols-3 scrollbar-blue p-2">
      {items.map((item, index) => (
        <ItemCard key={item.id} item={item} index={index} />
      ))}
    </div>
  );
}
