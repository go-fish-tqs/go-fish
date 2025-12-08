import React from "react";
import { Item } from "@/app/items/types";
import ItemCard from "./ItemCard";

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
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
        {[...Array(6)].map((_, i) => (
          <div key={i} className="h-64 bg-gray-200 rounded-xl"></div>
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
      <div className="items-center flex justify-center text-center py-8 bg-gray-50 rounded-xl border border-gray-100">
        <p className="text-gray-500 text-lg">No items match your filters.</p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto gap-6">
      {items.map((item) => (
        <ItemCard key={item.id} item={item} />
      ))}
    </div>
  );
}
