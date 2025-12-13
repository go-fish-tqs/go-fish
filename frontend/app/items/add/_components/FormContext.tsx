"use client";

import { createContext, useContext, useState, ReactNode } from "react";

interface FormData {
    name: string;
    description: string;
    photoUrls: string[];
    category: string;
    material: string;
    price: string;
}

interface FormContextType {
    formData: FormData;
    errors: Partial<FormData>;
    setFormData: (data: FormData | ((prev: FormData) => FormData)) => void;
    setErrors: (errors: Partial<FormData> | ((prev: Partial<FormData>) => Partial<FormData>)) => void;
    updateField: (field: keyof FormData, value: string | string[]) => void;
    clearError: (field: keyof FormData) => void;
}

const FormContext = createContext<FormContextType | undefined>(undefined);

export function useFormContext() {
    const context = useContext(FormContext);
    if (!context) {
        throw new Error("useFormContext must be used within FormProvider");
    }
    return context;
}

interface FormProviderProps {
    children: ReactNode;
}

export function FormProvider({ children }: FormProviderProps) {
    const [formData, setFormData] = useState<FormData>({
        name: "",
        description: "",
        photoUrls: [],
        category: "",
        material: "",
        price: ""
    });

    const [errors, setErrors] = useState<Partial<FormData>>({});

    const updateField = (field: keyof FormData, value: string | string[]) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (errors[field]) {
            setErrors(prev => ({ ...prev, [field]: undefined }));
        }
    };

    const clearError = (field: keyof FormData) => {
        setErrors(prev => ({ ...prev, [field]: undefined }));
    };

    return (
        <FormContext.Provider value={{ formData, errors, setFormData, setErrors, updateField, clearError }}>
            {children}
        </FormContext.Provider>
    );
}
