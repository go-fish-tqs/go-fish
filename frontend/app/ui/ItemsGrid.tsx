import React from "react";
import { Item } from "@/app/items/types"; // Adjust import path

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
      <div className="text-center py-12 bg-gray-50 rounded-xl border border-gray-100">
        <p className="text-gray-500 text-lg">No items match your filters.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {items.map((item) => (
        <div
          key={item.id}
          className="border border-gray-200 rounded-xl p-5 bg-white shadow-sm hover:shadow-md transition duration-200 flex flex-col"
        >
          {/* Image */}
          <div className="h-40 bg-gray-100 rounded-lg mb-4 flex items-center justify-center overflow-hidden">
            {item.images && item.images.length > 0 ? (
              <img
                src={item.images[0]}
                alt={item.name}
                className="h-full w-full object-cover"
              />
            ) : (
              <span className="text-gray-400 text-sm">No Image</span>
            )}
          </div>

          {/* Content */}
          <div className="flex-grow">
            <div className="flex justify-between items-start">
              <h2 className="text-lg font-bold text-gray-800 line-clamp-1">
                {item.name}
              </h2>
              {item.material && (
                <span className="text-xs font-semibold bg-blue-100 text-blue-800 px-2 py-1 rounded whitespace-nowrap ml-2">
                  {item.material.replace(/_/g, " ")}
                </span>
              )}
            </div>
            <p className="text-sm text-gray-600 mt-2 line-clamp-2">
              {item.description}
            </p>
          </div>

          {/* Footer */}
          <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between">
            {item.price !== undefined && (
              <span className="text-xl font-bold text-gray-900">
                ${item.price.toFixed(2)}
              </span>
            )}
            <button className="text-sm text-blue-600 font-medium hover:underline">
              View Details
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
