"use client";

import { use } from "react"; // <--- 1. IMPORT THIS
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import ItemGallery from "./_components/ItemGallery";
import ItemInfo from "./_components/ItemInfo";
import { Item } from "@/app/items/types";

interface PageProps {
  // 2. NOW IT'S A PROMISE!
  params: Promise<{
    id: string;
  }>;
}

export default function ItemPage({ params }: PageProps) {
  // 3. UNWRAP THE PROMISE HERE
  const { id } = use(params);

  // From here down, everything is the same, but use the 'id' you just extracted
  const {
    data: item,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["item", id], // <--- Use the unwrapped id
    queryFn: async () => {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/items/${id}`);
      if (!res.ok) throw new Error("Deu barraca no fetch!");
      return res.json() as Promise<Item>;
    },
  });

  if (isLoading) {
    return (
      <div className="px-4 py-6 sm:px-6 lg:px-8">
        <div className="h-10 w-24 bg-gray-200 rounded mb-6 animate-pulse flex-shrink-0"></div>
        <div className="grid gap-6 lg:gap-8 lg:grid-cols-2 items-start">
          {/* Gallery skeleton */}
          <div className="space-y-4">
            <div className="aspect-square w-full bg-gray-200 rounded-lg animate-pulse flex-shrink-0"></div>
            <div className="flex gap-3">
              {Array.from({ length: 3 }).map((_, i) => (
                <div
                  key={i}
                  className="h-16 w-16 bg-gray-200 rounded animate-pulse flex-shrink-0"
                ></div>
              ))}
            </div>
          </div>
          {/* Info skeleton */}
          <div className="space-y-4">
            <div className="h-8 bg-gray-200 rounded w-3/4 animate-pulse flex-shrink-0"></div>
            <div className="h-6 bg-gray-200 rounded w-1/4 animate-pulse flex-shrink-0"></div>
            <div className="space-y-2">
              {Array.from({ length: 4 }).map((_, i) => (
                <div
                  key={i}
                  className="h-4 bg-gray-200 rounded animate-pulse flex-shrink-0"
                ></div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }
  if (isError || !item)
    return (
      <div className="p-10 text-center text-red-600">
        Erro: {error?.message}
      </div>
    );

  return (
    <div className="px-4 py-6 sm:px-6 lg:px-8  overflow-y-hidden">
      <Link
        href="/items"
        className="inline-flex items-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-800 mb-4"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15 19l-7-7 7-7"
          />
        </svg>
        Back to Items
      </Link>
      <div className="grid gap-6 lg:gap-8 lg:grid-cols-2 items-start">
        <ItemGallery images={item.photoUrls || []} name={item.name} />
        <ItemInfo item={item} />
      </div>
    </div>
  );
}
