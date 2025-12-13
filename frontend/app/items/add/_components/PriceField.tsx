"use client";

import { useFormContext } from "./FormContext";

export function PriceField() {
  const { formData, errors, updateField } = useFormContext();

  return (
    <div>
      <label
        htmlFor="price"
        className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2"
      >
        Price (€) <span className="text-red-500">*</span>
      </label>
      <input
        type="number"
        id="price"
        name="price"
        value={formData.price}
        onChange={(e) => updateField("price", e.target.value)}
        step="0.01"
        min="0"
        className={`w-full px-4 py-3 rounded-lg border ${
          errors.price
            ? "border-red-500 focus:ring-red-500"
            : "border-gray-300 dark:border-gray-600 focus:ring-blue-500"
        } focus:ring-2 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100 transition-colors`}
        placeholder="0.00"
      />
      {errors.price && (
        <p className="mt-1 text-sm text-red-500">{errors.price}</p>
      )}
    </div>
  );
}
