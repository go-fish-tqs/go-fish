"use client";

import { useState } from "react";
import { useFormContext } from "./FormContext";

export function PhotoUrlsField() {
    const { formData, updateField } = useFormContext();
    const [photoInput, setPhotoInput] = useState("");

    const handleAddPhoto = () => {
        if (photoInput.trim()) {
            updateField("photoUrls", [...formData.photoUrls, photoInput.trim()]);
            setPhotoInput("");
        }
    };

    const handleRemovePhoto = (index: number) => {
        updateField("photoUrls", formData.photoUrls.filter((_, i) => i !== index));
    };

    return (
        <div>
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">
                Photo URLs
            </label>
            <div className="flex gap-2 mb-3">
                <input
                    type="url"
                    value={photoInput}
                    onChange={(e) => setPhotoInput(e.target.value)}
                    className="flex-1 px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100"
                    placeholder="https://example.com/image.jpg"
                    onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddPhoto())}
                />
                <button
                    type="button"
                    onClick={handleAddPhoto}
                    className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
                >
                    Add
                </button>
            </div>
            {formData.photoUrls.length > 0 && (
                <div className="space-y-2">
                    {formData.photoUrls.map((url, index) => (
                        <div key={index} className="flex items-center gap-2 p-3 bg-gray-50 dark:bg-slate-700 rounded-lg">
                            <span className="flex-1 text-sm text-gray-700 dark:text-gray-300 truncate">{url}</span>
                            <button
                                type="button"
                                onClick={() => handleRemovePhoto(index)}
                                className="text-red-500 hover:text-red-700 font-medium text-sm"
                            >
                                Remove
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
