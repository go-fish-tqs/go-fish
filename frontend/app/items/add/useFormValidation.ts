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

        const name = formData.name.trim();
        if (!name) {
            newErrors.name = "Name is required";
        } else if (name.length < 3 || name.length > 100) {
            newErrors.name = "Name must be between 3 and 100 characters";
        }

        const description = formData.description.trim();
        if (!description) {
            newErrors.description = "Description is required";
        } else if (description.length < 10 || description.length > 1000) {
            newErrors.description = "Description must be between 10 and 1000 characters";
        }

        if (!formData.category) {
            newErrors.category = "Category is required";
        }
        if (!formData.material) {
            newErrors.material = "Material is required";
        }

        const price = parseFloat(formData.price);
        if (!formData.price || isNaN(price) || price <= 0) {
            newErrors.price = "Price must be a positive decimal number";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    return { validateForm };
}
