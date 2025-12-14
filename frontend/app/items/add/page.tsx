"use client";

import { useState } from "react";
import { FormProvider, useFormContext } from "./FormContext";
import { useFormValidation } from "./useFormValidation";
import { Header } from "./Header";
import { NameField } from "./NameField";
import { DescriptionField } from "./DescriptionField";
import { PhotoUrlsField } from "./PhotoUrlsField";
import { CategoryField } from "./CategoryField";
import { MaterialField } from "./MaterialField";
import { PriceField } from "./PriceField";
import { SuccessModal } from "./SuccessModal";

function ItemForm() {
    const { formData } = useFormContext();
    const { validateForm } = useFormValidation();
    const [showSuccessModal, setShowSuccessModal] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        const itemData = {
            userId: 1,
            name: formData.name,
            description: formData.description,
            photoUrls: formData.photoUrls,
            material: formData.material,
            category: formData.category,
            price: parseFloat(formData.price)
        };

        console.log("Submitting item:", itemData);

        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/items`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(itemData)
            });

            if (response.ok) {
                setShowSuccessModal(true);
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error("Server error:", errorData);
                alert("Failed to create item. Please check your inputs and try again.");
            }
        } catch (error) {
            console.error("Network error:", error);
            alert("An error occurred while connecting to the server.");
        }
    };

    const handleSuccessClose = () => {
        setShowSuccessModal(false);
        window.location.href = "/items";
    };

    return (
        <div className="h-full  py-12 px-4 sm:px-6 lg:px-8">
            <div className="h-full mx-auto">
                <div className="h-full bg-white dark:bg-slate-800 shadow-xl rounded-2xl p-8 overflow-y-auto">
                    <Header />

                    <form onSubmit={handleSubmit} className="space-y-3">
                        <NameField />
                        <DescriptionField />
                        <PhotoUrlsField />

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <CategoryField />
                            <MaterialField />
                        </div>

                        <PriceField />

                        <div className="flex gap-4 pt-6 border-t border-gray-200 dark:border-gray-700">
                            <button
                                type="submit"
                                className="flex-1 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-semibold py-3 px-6 rounded-lg shadow-lg shadow-blue-500/30 transition-all duration-200 hover:shadow-blue-500/50 hover:scale-[1.02]"
                            >
                                Create Item
                            </button>
                            <button
                                type="button"
                                onClick={() => window.history.back()}
                                className="px-6 py-3 border-2 border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 font-semibold rounded-lg hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors"
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <SuccessModal
                isOpen={showSuccessModal}
                onClose={handleSuccessClose}
                message="Item created successfully!"
            />
        </div>
    );
}

export default function AddItemPage() {
    return (
        <FormProvider>
            <ItemForm />
        </FormProvider>
    );
}