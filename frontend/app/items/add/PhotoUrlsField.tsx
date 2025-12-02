"use client";

import { useState } from "react";
import { useFormContext } from "./FormContext";

export function PhotoUrlsField() {
    const { formData, updateField } = useFormContext();
    const [urlInput, setUrlInput] = useState("");

    // Handle manual URL input
    const handleAddUrl = () => {
        const trimmedUrl = urlInput.trim();
        if (!trimmedUrl) return;

        updateField("photoUrls", [...formData.photoUrls, trimmedUrl]);
        setUrlInput("");
    };

    const handleRemovePhoto = (index: number) => {
        updateField("photoUrls", formData.photoUrls.filter((_, i) => i !== index));
    };

    return (
        <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">
                Product Photos
            </label>

            <div className="flex flex-col gap-3 mb-4">
                {/* URL Input Row */}
                <div className="flex gap-2">
                    <input
                        type="url"
                        value={urlInput}
                        onChange={(e) => setUrlInput(e.target.value)}
                        className="flex-1 px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100"
                        placeholder="Paste image URL here..."
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                handleAddUrl();
                            }
                        }}
                    />
                    <button
                        type="button"
                        onClick={handleAddUrl}
                        className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
                    >
                        Add URL
                    </button>
                </div>

            </div>

            {/* Image previews */}
            {formData.photoUrls.length > 0 && (
                <div className="mt-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                    {formData.photoUrls.map((url, index) => (
                        <div key={index} className="relative group aspect-square">
                            <div className="w-full h-full rounded-lg overflow-hidden bg-gray-100 dark:bg-slate-700 border-2 border-gray-200 dark:border-gray-600">
                                <img
                                    src={url}
                                    alt={`Product photo ${index + 1} `}
                                    className="w-full h-full object-cover"
                                />
                            </div>
                            <button
                                type="button"
                                onClick={() => handleRemovePhoto(index)}
                                className="absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white rounded-full p-1.5 opacity-0 group-hover:opacity-100 transition-opacity shadow-lg"
                                title="Remove image"
                            >
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {formData.photoUrls.length > 0 && (
                <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                    {formData.photoUrls.length} image{formData.photoUrls.length !== 1 ? 's' : ''} selected
                </p>
            )}
        </div>
    );
}
