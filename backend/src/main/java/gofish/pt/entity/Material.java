package gofish.pt.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Material {

    // --- Rod Materials ---
    GRAPHITE(MaterialGroup.RODS, "Graphite"),
    FIBERGLASS(MaterialGroup.RODS, "Fiberglass"),
    COMPOSITE(MaterialGroup.RODS, "Composite Blend"),
    CARBON_FIBER(MaterialGroup.RODS, "Carbon Fiber"),

    // --- Reel Materials ---
    ALUMINUM(MaterialGroup.REELS, "Aluminum"),
    MACHINED_ALUMINUM(MaterialGroup.REELS, "Machined Aluminum"),
    DIE_CAST_ALUMINUM(MaterialGroup.REELS, "Die-Cast Aluminum"),
    STAINLESS_STEEL(MaterialGroup.REELS, "Stainless Steel"),
    MAGNESIUM(MaterialGroup.REELS, "Magnesium"),
    BRASS(MaterialGroup.REELS, "Brass"),
    CARBON_COMPOSITE(MaterialGroup.REELS, "Carbon Composite"),
    POLYMER(MaterialGroup.REELS, "Polymer"),

    // --- Boats & Kayaks ---
    ROTOMOLDED_POLYETHYLENE(MaterialGroup.BOATS, "Rotomolded Polyethylene"),
    THERMOFORMED_PLASTIC(MaterialGroup.BOATS, "Thermoformed Plastic"),
    INFLATABLE_PVC(MaterialGroup.BOATS, "Inflatable PVC"),
    HYPALON(MaterialGroup.BOATS, "Hypalon"),
    FIBERGLASS_BOAT(MaterialGroup.BOATS, "Fiberglass"),
    ALUMINUM_BOAT(MaterialGroup.BOATS, "Aluminum Boat"),

    // --- Apparel & Wearables ---
    NEOPRENE(MaterialGroup.APPARELS, "Neoprene"),
    BREATHABLE_FABRIC(MaterialGroup.APPARELS, "Breathable Fabric"),
    NYLON(MaterialGroup.APPARELS, "Nylon"),
    POLYESTER(MaterialGroup.APPARELS, "Polyester"),
    RUBBER(MaterialGroup.APPARELS, "Rubber"),
    PVC_COATED_FABRIC(MaterialGroup.APPARELS, "PVC-Coated Fabric"),

    // --- Accessories / Tools ---
    CARBON_STEEL(MaterialGroup.ACCESSORIES, "Carbon Steel"),
    ANODIZED_ALUMINUM(MaterialGroup.ACCESSORIES, "Anodized Aluminum"),
    TITANIUM(MaterialGroup.ACCESSORIES, "Titanium"),
    HARD_PLASTIC(MaterialGroup.ACCESSORIES, "Hard Plastic"),
    EVA_FOAM(MaterialGroup.ACCESSORIES, "EVA Foam"),
    CORK(MaterialGroup.ACCESSORIES, "Cork"),
    COMPOSITE_HANDLE(MaterialGroup.ACCESSORIES, "Composite Handle"),

    // --- Nets ---
    RUBBER_MESH(MaterialGroup.NETS, "Rubber Mesh"),
    KNOTLESS_NYLON_MESH(MaterialGroup.NETS, "Knotless Nylon Mesh"),
    ALUMINUM_FRAME(MaterialGroup.NETS, "Aluminum Frame"),
    CARBON_FRAME(MaterialGroup.NETS, "Carbon Frame");

    public enum MaterialGroup {
        RODS,
        REELS,
        BOATS,
        APPARELS,
        ACCESSORIES,
        NETS;

        public List<Material> getMaterials() {
            return Arrays.stream(Material.values()).filter(v -> v.getGroup() == this).toList();
        }
    }

    private final MaterialGroup group;
    private final String displayName;

    Material(MaterialGroup group, String displayName) {
        this.group = group;
        this.displayName = displayName;
    }
}
