import React from "react";
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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 w-full">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="bg-white rounded-2xl overflow-hidden shadow-lg animate-pulse flex flex-col"
          >
            <div className="h-48 bg-gray-200 flex-shrink-0"></div>
            <div className="p-5 space-y-3 flex-1">
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="h-3 bg-gray-200 rounded w-full"></div>
              <div className="h-3 bg-gray-200 rounded w-5/6"></div>
              <div className="h-10 bg-gray-200 rounded w-full mt-auto"></div>
            </div>
          </div>
        ))}
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
