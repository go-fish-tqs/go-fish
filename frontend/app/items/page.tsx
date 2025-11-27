"use client";

import { useQuery } from "@tanstack/react-query";

type Item = {
  id: string;
  name: string;
  description: string;
  images?: string[];
};

export default function ItemsPage() {
  // Fetch function
  const fetchItems = async (): Promise<Item[]> => {
    const res = await fetch("http://localhost:8080/api/items", {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (!res.ok) {
      throw new Error("Failed to fetch items");
    }

    return res.json();
  };

  // React Query hook
  const { data, isLoading, isError } = useQuery({
    queryKey: ["items"],
    queryFn: fetchItems,
  });

  // Loading state
  if (isLoading) {
    return <p className="text-gray-600">Loading items...</p>;
  }

  // Error state
  if (isError) {
    return <p className="text-red-500">Failed to load items.</p>;
  }

  // Empty list response
  if (data && data.length === 0) {
    return (
      <p className="text-gray-600">
        No items available at the moment. Please check back later.
      </p>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Available Items</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {data?.map((item) => (
          <div
            key={item.id}
            className="border rounded-lg p-4 bg-white shadow hover:shadow-md transition duration-200"
          >
            {/* Image if exists */}
            {item.images ? (
              <img
                src={item.images[0]}
                alt={item.name}
                className="w-full h-40 object-cover rounded-md mb-3"
              />
            ) : (
              <div className="w-full h-40 bg-gray-200 rounded-md mb-3" />
            )}

            <h2 className="text-lg font-semibold">{item.name}</h2>
            <p className="text-sm text-gray-600 mt-2">{item.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
