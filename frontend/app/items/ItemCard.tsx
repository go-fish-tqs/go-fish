import Link from "next/link";
import { Item } from "@/app/items/types";

interface ItemCardProps {
  item: Item;
}

export default function ItemCard({ item }: ItemCardProps) {
  return (
    <div className="border border-gray-200 rounded-xl p-5 bg-white shadow-sm hover:shadow-md transition duration-200 flex flex-col">
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
        <Link
          href={`/booking/add?itemId=${item.id}`}
          className="text-sm text-blue-600 font-medium hover:underline"
        >
          Book Now
        </Link>
      </div>
    </div>
  );
}
