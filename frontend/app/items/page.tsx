"use client";

import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import ItemsFilterBar from "@/app/ui/ItemsFilterBar";
import ItemsGrid from "@/app/ui/ItemsGrid";
import {
  Item,
  ItemFilter,
  CategoryNode,
  FlatCategory,
  MaterialMap,
} from "./types";

// Helper: Flatten Tree & Track Root
const flattenCategories = (
  nodes: CategoryNode[],
  level = 0,
  inheritedRootId?: string
): FlatCategory[] => {
  return nodes.flatMap((node) => {
    const currentRootId = level === 0 ? node.id : inheritedRootId!;
    return [
      { ...node, level, rootId: currentRootId },
      ...flattenCategories(node.subCategories || [], level + 1, currentRootId),
    ];
  });
};

export default function ItemsPage() {
  // ---------- STATE ----------
  const [searchInput, setSearchInput] = useState(""); // Input field state
  const [search, setSearch] = useState(""); // Actual search trigger state
  const [material, setMaterial] = useState("");
  const [category, setCategory] = useState("");
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 1000]);

  // ---------- API FETCHERS ----------

  // 1. Fetch Materials
  const { data: materialsMap = {} } = useQuery({
    queryKey: ["materials"],
    queryFn: async () => {
      // UPDATED PATH: /api/items/materials
      const res = await fetch("http://localhost:8080/api/items/materials");
      if (!res.ok) throw new Error("Failed");
      return res.json() as Promise<MaterialMap>;
    },
  });

  // 2. Fetch Categories
  const { data: categoryTree = [] } = useQuery({
    queryKey: ["categories"],
    queryFn: async () => {
      // UPDATED PATH: /api/items/categories
      const res = await fetch("http://localhost:8080/api/items/categories");
      if (!res.ok) throw new Error("Failed");
      return res.json() as Promise<CategoryNode[]>;
    },
  });

  // 3. Fetch Items
  const {
    data: items = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["items", search, material, category, priceRange],
    queryFn: async () => {
      const filter: ItemFilter = {
        name: search,
        category: category === "" ? null : category,
        material: material === "" ? null : material,
        minPrice: priceRange[0],
        maxPrice: priceRange[1],
      };

      const res = await fetch("http://localhost:8080/api/items/filter", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(filter),
      });

      if (!res.ok) throw new Error("Failed");
      return res.json() as Promise<Item[]>;
    },
  });

  // ---------- DERIVED LOGIC ----------

  // Prepare flattened categories for the child component
  const flatCategories = useMemo(
    () => flattenCategories(categoryTree),
    [categoryTree]
  );

  // Prepare available materials based on selection
  const availableMaterials = useMemo(() => {
    if (!category) {
      const allMats = Object.values(materialsMap).flat();
      return Array.from(new Set(allMats));
    }
    const selectedCatNode = flatCategories.find((c) => c.id === category);
    if (selectedCatNode && materialsMap[selectedCatNode.rootId]) {
      return materialsMap[selectedCatNode.rootId];
    }
    return [];
  }, [category, materialsMap, flatCategories]);

  // Handle Category Change (Reset material logic)
  const handleCategoryChange = (newCat: string) => {
    setCategory(newCat);
    setMaterial(""); // Reset material so we don't have invalid combos
  };

  return (
    <div className="max-w-7xl mx-auto p-6 h-full space-y-6">
      <div className="h-1/14">
        <h1 className="text-3xl font-bold mb-8 text-gray-800">Available Gear</h1>
      </div>
      <div className="h-3/14">
        <ItemsFilterBar
          searchInput={searchInput}
          setSearchInput={setSearchInput}
          onSearch={() => setSearch(searchInput)}
          material={material}
          setMaterial={setMaterial}
          category={category}
          setCategory={handleCategoryChange}
          priceRange={priceRange}
          setPriceRange={setPriceRange}
          flatCategories={flatCategories}
          availableMaterials={availableMaterials}
        />
      </div>
      <div className="h-9/14 overflow-y-auto">
        <ItemsGrid items={items} isLoading={isLoading} isError={isError} />
      </div>
    </div>
  );
}
