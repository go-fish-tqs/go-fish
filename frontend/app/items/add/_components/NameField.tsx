"use client";

import { useFormContext } from "./FormContext";

export function NameField() {
  const { formData, errors, updateField } = useFormContext();

  return (
    <div>
      <label
        htmlFor="name"
        className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2"
      >
        Item Name <span className="text-red-500">*</span>
      </label>
      <input
        type="text"
        id="name"
        name="name"
        value={formData.name}
        onChange={(e) => updateField("name", e.target.value)}
        maxLength={64}
        className={`w-full px-4 py-3 rounded-lg border ${
          errors.name
            ? "border-red-500 focus:ring-red-500"
            : "border-gray-300 dark:border-gray-600 focus:ring-blue-500"
        } focus:ring-2 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100 transition-colors`}
        placeholder="e.g., Shimano Stradic FL Spinning Reel"
      />
      <div className="h-6 mt-1">
        {errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
      </div>
    </div>
  );
}
