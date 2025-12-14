import React from "react";
import { FlatCategory } from "@/app/items/types"; // Adjust import path

interface ItemsFilterBarProps {
  // State Values
  searchInput: string;
  setSearchInput: (val: string) => void;
  onSearch: () => void; // Trigger the actual search
  material: string;
  setMaterial: (val: string) => void;
  category: string;
  setCategory: (val: string) => void;
  priceRange: [number, number];
  setPriceRange: (val: [number, number]) => void;

  // Data for Dropdowns
  flatCategories: FlatCategory[];
  availableMaterials: string[];
}

export default function ItemsFilterBar({
  searchInput,
  setSearchInput,
  onSearch,
  material,
  setMaterial,
  category,
  setCategory,
  priceRange,
  setPriceRange,
  flatCategories,
  availableMaterials,
}: ItemsFilterBarProps) {
  return (
    <div className="p-6 mb-8 bg-white shadow-lg rounded-xl border border-gray-100">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* 1. Search */}
        <div className="col-span-1 md:col-span-2">
          <label className="block mb-2 text-sm font-semibold text-gray-700">
            Search
          </label>
          <div className="flex gap-2">
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 outline-none"
              placeholder="Search..."
              onKeyDown={(e) => e.key === "Enter" && onSearch()}
            />
            <button
              onClick={onSearch}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
            >
              Go
            </button>
          </div>
        </div>

        {/* 2. Category Select */}
        <div>
          <label className="block mb-2 text-sm font-semibold text-gray-700">
            Category
          </label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 outline-none bg-white"
          >
            <option value="">All Categories</option>
            {flatCategories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {"\u00A0\u00A0".repeat(cat.level)}
                {cat.level > 0 ? "↳ " : ""}
                {cat.displayName}
              </option>
            ))}
          </select>
        </div>

        {/* 3. Material Select */}
        <div>
          <label className="block mb-2 text-sm font-semibold text-gray-700">
            Material
          </label>
          <select
            value={material}
            onChange={(e) => setMaterial(e.target.value)}
            disabled={availableMaterials.length === 0}
            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 outline-none bg-white disabled:bg-gray-100 disabled:text-gray-400"
          >
            <option value="">All Materials</option>
            {availableMaterials.map((mat) => (
              <option key={mat} value={mat}>
                {mat.replace(/_/g, " ")}
              </option>
            ))}
          </select>
        </div>

        {/* 4. Price Range */}
        <div className="md:col-span-4 mt-2">
          <div className="flex justify-between mb-2">
            <label className="text-sm font-semibold text-gray-700">
              Price Range
            </label>
            <span className="text-sm font-medium text-blue-600">
              ${priceRange[0]} - ${priceRange[1]}
            </span>
          </div>
          <div className="flex items-center gap-6">
            <input
              type="range"
              min={0}
              max={1000}
              value={priceRange[0]}
              onChange={(e) => {
                const val = Number(e.target.value);
                if (val < priceRange[1]) setPriceRange([val, priceRange[1]]);
              }}
              className="w-full accent-blue-600 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
            <input
              type="range"
              min={0}
              max={1000}
              value={priceRange[1]}
              onChange={(e) => {
                const val = Number(e.target.value);
                if (val > priceRange[0]) setPriceRange([priceRange[0], val]);
              }}
              className="w-full accent-blue-600 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
