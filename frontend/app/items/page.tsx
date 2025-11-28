"use client";

import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";

type Item = {
  id: string;
  name: string;
  description: string;
  material?: string;
  category?: string;
  price?: number;
  images?: string[];
};

type ItemFilter = {
  name?: string | null;
  category?: string | null;
  material?: string | null;
  minPrice?: number | null;
  maxPrice?: number | null;
  sortBy?: string | null;
  direction?: string | null;
};

export default function ItemsPage() {
  // ---------- LOCAL FILTER STATES ----------
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState(""); // actual query value
  const [material, setMaterial] = useState("");
  const [category, setCategory] = useState("");
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 1000]);

  // ---------- FETCH FUNCTION ----------
  const fetchItems = async (filter: ItemFilter): Promise<Item[]> => {
    const res = await fetch("http://localhost:8080/api/items/filter", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(filter),
    });

    if (!res.ok) {
      throw new Error("Failed to fetch items");
    }

    return res.json();
  };

  const { data, isLoading, isError } = useQuery({
    queryKey: ["items", search, material, category, priceRange],
    queryFn: () =>
      fetchItems({
        name: search,
        category: category === "" ? null : category,
        material: material === "" ? null : material,
        minPrice: priceRange[0],
        maxPrice: priceRange[1],
        sortBy: null,
        direction: null,
      }),
  });

  // ---------- CLIENT-SIDE FILTER LOGIC ----------
  const filteredItems = useMemo(() => {
    if (!data) return [];

    return data.filter((item) => {
      const matchesSearch =
        item.name.toLowerCase().includes(search.toLowerCase()) ||
        item.description.toLowerCase().includes(search.toLowerCase());

      const matchesMaterial = material ? item.material === material : true;
      const matchesCategory = category ? item.category === category : true;

      const matchesPrice =
        item.price !== undefined &&
        item.price >= priceRange[0] &&
        item.price <= priceRange[1];

      return (
        matchesSearch && matchesMaterial && matchesCategory && matchesPrice
      );
    });
  }, [data, search, material, category, priceRange]);

  // ---------- LOADING / ERROR UI ----------
  if (isLoading) return <p className="text-gray-600">Loading items...</p>;
  if (isError) return <p className="text-red-500">Failed to load items.</p>;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Available Items</h1>

      {/* ---------- FILTER BAR UI ---------- */}
      <div className="p-6 mb-8 bg-white shadow-md rounded-xl">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {/* Search */}
          <div className="col-span-1 md:col-span-2">
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Search
            </label>

            <div className="flex gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search items..."
                className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition"
              />

              <button
                onClick={() => setSearch(searchInput)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                type="button"
              >
                Search
              </button>
            </div>
          </div>

          {/* Material */}
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Material
            </label>
            <select
              value={material}
              onChange={(e) => setMaterial(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition"
            >
              <option value="">All</option>
              <option value="wood">Wood</option>
              <option value="metal">Metal</option>
              <option value="plastic">Plastic</option>
            </select>
          </div>

          {/* Category */}
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Category
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition"
            >
              <option value="">All</option>
              <option value="furniture">Furniture</option>
              <option value="decor">Decor</option>
              <option value="tools">Tools</option>
            </select>
          </div>

          {/* Price Range */}
          <div className="md:col-span-4">
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Price Range (${priceRange[0]} - ${priceRange[1]})
            </label>

            <div className="flex items-center gap-6">
              <div className="flex-1">
                <input
                  type="range"
                  min={0}
                  max={1000}
                  value={priceRange[0]}
                  onChange={(e) =>
                    setPriceRange([Number(e.target.value), priceRange[1]])
                  }
                  className="w-full accent-blue-500"
                />
              </div>

              <div className="flex-1">
                <input
                  type="range"
                  min={0}
                  max={1000}
                  value={priceRange[1]}
                  onChange={(e) =>
                    setPriceRange([priceRange[0], Number(e.target.value)])
                  }
                  className="w-full accent-blue-500"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ---------- ITEM LIST ---------- */}
      {filteredItems.length === 0 ? (
        <p className="text-gray-600">No items match the selected filters.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredItems.map((item) => (
            <div
              key={item.id}
              className="border rounded-lg p-4 bg-white shadow hover:shadow-md transition duration-200"
            >
              <h2 className="text-lg font-semibold">{item.name}</h2>
              <p className="text-sm text-gray-600 mt-2">{item.description}</p>

              {item.price !== undefined && (
                <p className="text-sm mt-2 font-medium">Price: ${item.price}</p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
