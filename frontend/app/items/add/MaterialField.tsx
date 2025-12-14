"use client";

import { useFormContext } from "./FormContext";
import { MaterialGroup } from "./types";
import { useQuery } from "@tanstack/react-query";
import { MaterialMap } from "../types";
import { useMemo } from "react";

// Helper function to format group names
const formatGroupName = (group: string): string => {
    const groupNames: Record<string, string> = {
        'RODS': 'Rod Materials',
        'REELS': 'Reel Materials',
        'BOATS': 'Boat Materials',
        'APPARELS': 'Apparel Materials',
        'ACCESSORIES': 'Accessory Materials',
        'NETS': 'Net Materials'
    };
    return groupNames[group] || group;
};

// Helper function to format material names (convert SNAKE_CASE to Title Case)
const formatMaterialName = (materialId: string): string => {
    return materialId
        .split('_')
        .map(word => word.charAt(0) + word.slice(1).toLowerCase())
        .join(' ');
};

export function MaterialField() {
    const { formData, errors, updateField } = useFormContext();

    const { data: materialsMap, isLoading } = useQuery({
        queryKey: ["materials"],
        queryFn: async () => {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/items/materials`);
            if (!res.ok) throw new Error("Failed to fetch materials");
            return res.json() as Promise<MaterialMap>;
        },
    });

    // Convert MaterialMap to MaterialGroup[] format for rendering
    const materialGroups: MaterialGroup[] = useMemo(() => {
        if (!materialsMap || Object.keys(materialsMap).length === 0) {
            return [];
        }

        return Object.entries(materialsMap).map(([groupKey, materialIds]) => ({
            group: formatGroupName(groupKey),
            items: materialIds.map(id => ({
                id,
                name: formatMaterialName(id)
            }))
        }));
    }, [materialsMap]);

    return (
        <div>
            <label htmlFor="material" className="block text-sm font-semibold text-gray-700 dark:text-gray-200 mb-2">
                Material <span className="text-red-500">*</span>
            </label>
            <select
                id="material"
                name="material"
                value={formData.material}
                onChange={(e) => updateField("material", e.target.value)}
                disabled={isLoading}
                className={`w-full px-4 py-3 rounded-lg border ${errors.material
                    ? 'border-red-500 focus:ring-red-500'
                    : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                    } focus:ring-2 focus:outline-none bg-white dark:bg-slate-700 text-gray-900 dark:text-gray-100 transition-colors disabled:opacity-50`}
            >
                <option value="">{isLoading ? 'Loading materials...' : 'Select a material'}</option>
                {materialGroups.map(group => (
                    <optgroup key={group.group} label={group.group}>
                        {group.items.map(mat => (
                            <option key={mat.id} value={mat.id}>{mat.name}</option>
                        ))}
                    </optgroup>
                ))}
            </select>
            {errors.material && <p className="mt-1 text-sm text-red-500">{errors.material}</p>}
        </div>
    );
}
