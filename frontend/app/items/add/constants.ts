// Type definitions
export interface Category {
    id: string;
    name: string;
}

export interface Material {
    id: string;
    name: string;
}

export interface MaterialGroup {
    group: string;
    items: Material[];
}

export const TOP_CATEGORIES: Category[] = [
    { id: "RODS", name: "Rods" },
    { id: "REELS", name: "Reels" },
    { id: "COMBOS", name: "Rod & Reel Combos" },
    { id: "ELECTRONICS", name: "Electronics" },
    { id: "APPAREL", name: "Apparel" },
    { id: "ACCESSORIES", name: "Accessories" },
    { id: "BOATS", name: "Boats & Kayaks" }
];

export const MATERIALS: MaterialGroup[] = [
    {
        group: "Rod Materials", items: [
            { id: "GRAPHITE", name: "Graphite" },
            { id: "FIBERGLASS", name: "Fiberglass" },
            { id: "COMPOSITE", name: "Composite Blend" },
            { id: "CARBON_FIBER", name: "Carbon Fiber" }
        ]
    },
    {
        group: "Reel Materials", items: [
            { id: "ALUMINUM", name: "Aluminum" },
            { id: "MACHINED_ALUMINUM", name: "Machined Aluminum" },
            { id: "DIE_CAST_ALUMINUM", name: "Die-Cast Aluminum" },
            { id: "STAINLESS_STEEL", name: "Stainless Steel" },
            { id: "MAGNESIUM", name: "Magnesium" },
            { id: "BRASS", name: "Brass" },
            { id: "CARBON_COMPOSITE", name: "Carbon Composite" },
            { id: "POLYMER", name: "Polymer" }
        ]
    },
    {
        group: "Boat Materials", items: [
            { id: "ROTOMOLDED_POLYETHYLENE", name: "Rotomolded Polyethylene" },
            { id: "THERMOFORMED_PLASTIC", name: "Thermoformed Plastic" },
            { id: "INFLATABLE_PVC", name: "Inflatable PVC" },
            { id: "HYPALON", name: "Hypalon" },
            { id: "FIBERGLASS_BOAT", name: "Fiberglass" },
            { id: "ALUMINUM_BOAT", name: "Aluminum Boat" }
        ]
    },
    {
        group: "Apparel Materials", items: [
            { id: "NEOPRENE", name: "Neoprene" },
            { id: "BREATHABLE_FABRIC", name: "Breathable Fabric" },
            { id: "NYLON", name: "Nylon" },
            { id: "POLYESTER", name: "Polyester" },
            { id: "RUBBER", name: "Rubber" },
            { id: "PVC_COATED_FABRIC", name: "PVC-Coated Fabric" }
        ]
    },
    {
        group: "Accessory Materials", items: [
            { id: "CARBON_STEEL", name: "Carbon Steel" },
            { id: "ANODIZED_ALUMINUM", name: "Anodized Aluminum" },
            { id: "TITANIUM", name: "Titanium" },
            { id: "HARD_PLASTIC", name: "Hard Plastic" },
            { id: "EVA_FOAM", name: "EVA Foam" },
            { id: "CORK", name: "Cork" },
            { id: "COMPOSITE_HANDLE", name: "Composite Handle" }
        ]
    },
    {
        group: "Net Materials", items: [
            { id: "RUBBER_MESH", name: "Rubber Mesh" },
            { id: "KNOTLESS_NYLON_MESH", name: "Knotless Nylon Mesh" },
            { id: "ALUMINUM_FRAME", name: "Aluminum Frame" },
            { id: "CARBON_FRAME", name: "Carbon Frame" }
        ]
    }
];
