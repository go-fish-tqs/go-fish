package gofish.pt.entity;

public enum Material {

    // --- Rod Materials ---
    GRAPHITE(MaterialGroup.ROD_MATERIALS, "Graphite"),
    FIBERGLASS(MaterialGroup.ROD_MATERIALS, "Fiberglass"),
    COMPOSITE(MaterialGroup.ROD_MATERIALS, "Composite Blend"),
    CARBON_FIBER(MaterialGroup.ROD_MATERIALS, "Carbon Fiber"),

    // --- Reel Materials ---
    ALUMINUM(MaterialGroup.REEL_MATERIALS, "Aluminum"),
    MACHINED_ALUMINUM(MaterialGroup.REEL_MATERIALS, "Machined Aluminum"),
    DIE_CAST_ALUMINUM(MaterialGroup.REEL_MATERIALS, "Die-Cast Aluminum"),
    STAINLESS_STEEL(MaterialGroup.REEL_MATERIALS, "Stainless Steel"),
    MAGNESIUM(MaterialGroup.REEL_MATERIALS, "Magnesium"),
    BRASS(MaterialGroup.REEL_MATERIALS, "Brass"),
    CARBON_COMPOSITE(MaterialGroup.REEL_MATERIALS, "Carbon Composite"),
    POLYMER(MaterialGroup.REEL_MATERIALS, "Polymer"),

    // --- Boats & Kayaks ---
    ROTOMOLDED_POLYETHYLENE(MaterialGroup.BOAT_MATERIALS, "Rotomolded Polyethylene"),
    THERMOFORMED_PLASTIC(MaterialGroup.BOAT_MATERIALS, "Thermoformed Plastic"),
    INFLATABLE_PVC(MaterialGroup.BOAT_MATERIALS, "Inflatable PVC"),
    HYPALON(MaterialGroup.BOAT_MATERIALS, "Hypalon"),
    FIBERGLASS_BOAT(MaterialGroup.BOAT_MATERIALS, "Fiberglass"),
    ALUMINUM_BOAT(MaterialGroup.BOAT_MATERIALS, "Aluminum Boat"),

    // --- Apparel & Wearables ---
    NEOPRENE(MaterialGroup.APPAREL_MATERIALS, "Neoprene"),
    BREATHABLE_FABRIC(MaterialGroup.APPAREL_MATERIALS, "Breathable Fabric"),
    NYLON(MaterialGroup.APPAREL_MATERIALS, "Nylon"),
    POLYESTER(MaterialGroup.APPAREL_MATERIALS, "Polyester"),
    RUBBER(MaterialGroup.APPAREL_MATERIALS, "Rubber"),
    PVC_COATED_FABRIC(MaterialGroup.APPAREL_MATERIALS, "PVC-Coated Fabric"),

    // --- Accessories / Tools ---
    CARBON_STEEL(MaterialGroup.ACCESSORY_MATERIALS, "Carbon Steel"),
    ANODIZED_ALUMINUM(MaterialGroup.ACCESSORY_MATERIALS, "Anodized Aluminum"),
    TITANIUM(MaterialGroup.ACCESSORY_MATERIALS, "Titanium"),
    HARD_PLASTIC(MaterialGroup.ACCESSORY_MATERIALS, "Hard Plastic"),
    EVA_FOAM(MaterialGroup.ACCESSORY_MATERIALS, "EVA Foam"),
    CORK(MaterialGroup.ACCESSORY_MATERIALS, "Cork"),
    COMPOSITE_HANDLE(MaterialGroup.ACCESSORY_MATERIALS, "Composite Handle"),

    // --- Nets ---
    RUBBER_MESH(MaterialGroup.NET_MATERIALS, "Rubber Mesh"),
    KNOTLESS_NYLON_MESH(MaterialGroup.NET_MATERIALS, "Knotless Nylon Mesh"),
    ALUMINUM_FRAME(MaterialGroup.NET_MATERIALS, "Aluminum Frame"),
    CARBON_FRAME(MaterialGroup.NET_MATERIALS, "Carbon Frame");

    public enum MaterialGroup {
        ROD_MATERIALS,
        REEL_MATERIALS,
        BOAT_MATERIALS,
        APPAREL_MATERIALS,
        ACCESSORY_MATERIALS,
        NET_MATERIALS
    }

    private final MaterialGroup group;
    private final String displayName;

    Material(MaterialGroup group, String displayName) {
        this.group = group;
        this.displayName = displayName;
    }

    public MaterialGroup getGroup() {
        return group;
    }

    public String getDisplayName() {
        return displayName;
    }

}
