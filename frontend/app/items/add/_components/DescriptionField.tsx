"use client";

import { useFormContext } from "./FormContext";

export function DescriptionField() {
  const { formData, errors, updateField } = useFormContext();

  return (
    <div>
      <label
        htmlFor="description"
        className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2"
      >
        Description <span className="text-red-500">*</span>
      </label>
      <textarea
        id="description"
        name="description"
        value={formData.description}
        onChange={(e) => updateField("description", e.target.value)}
        maxLength={512}
        rows={4}
        className={`w-full px-4 py-3 rounded-lg border ${
          errors.description
            ? "border-red-500 focus:ring-red-500"
            : "border-gray-300 dark:border-gray-600 focus:ring-blue-500"
        } focus:ring-2 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100 transition-colors resize-none`}
        placeholder="Provide a detailed description of the item..."
      />
      <div className="flex justify-between mt-2 h-6">
        {errors.description ? (
          <p className="text-sm text-red-500">{errors.description}</p>
        ) : (
          <div />
        )}
        <p className="text-sm text-gray-500 dark:text-gray-400 ml-auto">
          {formData.description.length}/512
        </p>
      </div>
    </div>
  );
}
