"use client";

import { useFormContext } from "./FormContext";

interface FormData {
    name: string;
    description: string;
    photoUrls: string[];
    category: string;
    material: string;
    price: string;
}

export function useFormValidation() {
    const { formData, setErrors } = useFormContext();

    const validateForm = (): boolean => {
        const newErrors: Partial<FormData> = {};

        if (!formData.name.trim()) {
            newErrors.name = "Name is required";
        }
        if (!formData.description.trim()) {
            newErrors.description = "Description is required";
        }
        if (!formData.category) {
            newErrors.category = "Category is required";
        }
        if (!formData.material) {
            newErrors.material = "Material is required";
        }
        if (!formData.price || parseFloat(formData.price) <= 0) {
            newErrors.price = "Valid price is required";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    return { validateForm };
}
