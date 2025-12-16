"use client";

import { useFormContext } from "./FormContext";
import { useQuery } from "@tanstack/react-query";
import { CategoryNode } from "../../types";
import { useMemo } from "react";

// Helper function to flatten the category tree
const flattenCategories = (
  categories: CategoryNode[],
  level: number = 0
): Array<{ id: string; displayName: string; level: number }> => {
  const result: Array<{ id: string; displayName: string; level: number }> = [];

  for (const category of categories) {
    result.push({
      id: category.id,
      displayName: category.displayName,
      level,
    });

    if (category.subCategories && category.subCategories.length > 0) {
      result.push(...flattenCategories(category.subCategories, level + 1));
    }
  }

  return result;
};

export function CategoryField() {
  const { formData, errors, updateField } = useFormContext();

  const { data: categoryTree = [], isLoading } = useQuery({
    queryKey: ["categories"],
    queryFn: async () => {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/items/categories`
      );
      if (!res.ok) throw new Error("Failed to fetch categories");
      return res.json() as Promise<CategoryNode[]>;
    },
  });

  // Flatten the category tree for easier rendering in a select dropdown
  const flatCategories = useMemo(() => {
    return flattenCategories(categoryTree);
  }, [categoryTree]);

  return (
    <div>
      <label
        htmlFor="category"
        className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2"
      >
        Category <span className="text-red-500">*</span>
      </label>
      <select
        id="category"
        name="category"
        value={formData.category}
        onChange={(e) => updateField("category", e.target.value)}
        disabled={isLoading}
        className={`w-full px-4 py-3 rounded-lg border ${
          errors.category
            ? "border-red-500 focus:ring-red-500"
            : "border-gray-300 dark:border-gray-600 focus:ring-blue-500"
        } focus:ring-2 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100 transition-colors disabled:opacity-50`}
      >
        <option value="">
          {isLoading ? "Loading categories..." : "Select a category"}
        </option>
        {flatCategories.map((cat) => (
          <option key={cat.id} value={cat.id}>
            {"\u00A0".repeat(cat.level * 4)}
            {cat.displayName}
          </option>
        ))}
      </select>
      {errors.category && (
        <p className="mt-1 text-sm text-red-500">{errors.category}</p>
      )}
    </div>
  );
}
