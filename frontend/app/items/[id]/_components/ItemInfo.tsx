// app/items/[id]/_components/ItemInfo.tsx
import { Item } from "@/app/items/types";

interface ItemInfoProps {
  item: Item;
}

export default function ItemInfo({ item }: ItemInfoProps) {
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

      {/* Action Button */}
      <div className="mt-4">
        <button
          disabled={!item.available}
          className={`w-full flex items-center justify-center rounded-md px-8 py-3 text-base font-medium text-white 
            ${item.available
              ? "bg-blue-600 hover:bg-blue-700 focus:ring-blue-500"
              : "bg-gray-400 cursor-not-allowed"
            }`}
        >
          {item.available ? "Request booking" : "Unavailable"}
        </button>
      </div>
    </div>
  );
}
