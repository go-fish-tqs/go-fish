package gofish.pt.entity;

import java.util.Arrays;
import java.util.List;

public enum Category {

    // Top-level
    RODS(null, "Rods"),
    REELS(null, "Reels"),
    COMBOS(null, "Rod & Reel Combos"),
    ELECTRONICS(null, "Electronics"),
    APPAREL(null, "Apparel"),
    ACCESSORIES(null, "Accessories"),
    BOATS(null, "Boats & Kayaks"),

    // RODS → categories
    RODS_SPINNING(RODS, "Spinning Rods"),
    RODS_CASTING(RODS, "Casting Rods"),
    RODS_FLY(RODS, "Fly Rods"),
    RODS_SPECIALTY(RODS, "Specialty Rods"),

    // RODS → SPINNING → subcategories
    LIGHT_SPINNING_RODS(RODS_SPINNING, "Light Spinning Rods"),
    MEDIUM_SPINNING_RODS(RODS_SPINNING, "Medium Spinning Rods"),
    HEAVY_SPINNING_RODS(RODS_SPINNING, "Heavy Spinning Rods"),

    // RODS → CASTING → subcategories
    BAITCASTING_RODS(RODS_CASTING, "Baitcasting Rods"),
    SURF_CASTING_RODS(RODS_CASTING, "Surf Casting Rods"),
    TROLLING_CASTING_RODS(RODS_CASTING, "Trolling Casting Rods"),

    // RODS → FLY → subcategories
    FRESHWATER_FLY_RODS(RODS_FLY, "Freshwater Fly Rods"),
    SALTWATER_FLY_RODS(RODS_FLY, "Saltwater Fly Rods"),

    // RODS → SPECIALTY
    ICE_RODS(RODS_SPECIALTY, "Ice Rods"),
    TRAVEL_RODS(RODS_SPECIALTY, "Travel Rods"),

    // REELS → categories
    REELS_SPINNING(REELS, "Spinning Reels"),
    REELS_CASTING(REELS, "Casting Reels"),
    REELS_FLY(REELS, "Fly Reels"),
    REELS_SPECIALTY(REELS, "Specialty Reels"),

    // REELS → SPINNING
    LIGHT_SPINNING_REELS(REELS_SPINNING, "Light Spinning Reels"),
    MEDIUM_SPINNING_REELS(REELS_SPINNING, "Medium Spinning Reels"),
    HEAVY_SPINNING_REELS(REELS_SPINNING, "Heavy Spinning Reels"),

    // REELS → CASTING
    BAITCASTING_REELS(REELS_CASTING, "Baitcasting Reels"),
    SURF_CASTING_REELS(REELS_CASTING, "Surf Casting Reels"),

    // REELS → FLY
    TROUT_FLY_REELS(REELS_FLY, "Trout Fly Reels"),
    SALTWATER_FLY_REELS(REELS_FLY, "Saltwater Fly Reels"),

    // REELS → SPECIALTY
    TROLLING_REELS(REELS_SPECIALTY, "Trolling Reels"),
    ICE_REELS(REELS_SPECIALTY, "Ice Reels"),

    // COMBOS
    SPINNING_COMBOS(COMBOS, "Spinning Combos"),
    CASTING_COMBOS(COMBOS, "Casting Combos"),
    FLY_COMBOS(COMBOS, "Fly Combos"),

    FRESHWATER_SPINNING_COMBOS(SPINNING_COMBOS, "Freshwater Spinning Combos"),
    SALTWATER_SPINNING_COMBOS(SPINNING_COMBOS, "Saltwater Spinning Combos"),

    BAITCAST_COMBOS(CASTING_COMBOS, "Baitcast Combos"),
    SURF_COMBOS(CASTING_COMBOS, "Surf Casting Combos"),

    BEGINNER_FLY_KITS(FLY_COMBOS, "Beginner Fly Kits"),
    ADVANCED_FLY_KITS(FLY_COMBOS, "Advanced Fly Kits"),

    // ELECTRONICS
    NAVIGATION(ELECTRONICS, "Navigation Devices"),
    FISH_DETECTION(ELECTRONICS, "Fish Detection Equipment"),
    CAMERA_SYSTEMS(ELECTRONICS, "Camera Systems"),

    GPS_UNITS(NAVIGATION, "GPS Units"),
    CHARTPLOTTERS(NAVIGATION, "Chartplotters"),

    FISH_FINDERS(FISH_DETECTION, "Fish Finders"),
    SONAR_SYSTEMS(FISH_DETECTION, "Sonar Systems"),

    UNDERWATER_CAMERAS(CAMERA_SYSTEMS, "Underwater Cameras"),

    // APPAREL
    WADING(APPAREL, "Wading Gear"),
    SAFETY(APPAREL, "Safety Gear"),
    WEATHER(APPAREL, "Weather Protection"),

    CHEST_WADERS(WADING, "Chest Waders"),
    HIP_WADERS(WADING, "Hip Waders"),
    WADING_BOOTS(WADING, "Wading Boots"),

    LIFE_JACKETS(SAFETY, "Life Jackets / PFDs"),

    RAIN_JACKETS(WEATHER, "Rain Jackets"),
    WATERPROOF_PANTS(WEATHER, "Waterproof Pants"),

    // ACCESSORIES
    LANDING_TOOLS(ACCESSORIES, "Landing Tools"),
    MEASURING_TOOLS(ACCESSORIES, "Measuring Tools"),
    STORAGE(ACCESSORIES, "Storage"),
    MISC_GEAR(ACCESSORIES, "Misc Gear"),

    NETS(LANDING_TOOLS, "Nets"),
    GAFFS(LANDING_TOOLS, "Gaffs"),

    SCALES(MEASURING_TOOLS, "Scales"),
    MEASURING_BOARDS(MEASURING_TOOLS, "Measuring Boards"),

    HARD_TACKLE_BOXES(STORAGE, "Hard Tackle Boxes"),
    SOFT_TACKLE_BAGS(STORAGE, "Soft Tackle Bags"),

    ROD_HOLDERS(MISC_GEAR, "Rod Holders"),
    MULTITOOLS(MISC_GEAR, "Multitools"),

    // BOATS
    KAYAKS(BOATS, "Fishing Kayaks"),
    SMALL_BOATS(BOATS, "Small Boats"),
    ADDONS(BOATS, "Boat Add-ons"),

    SIT_ON_TOP_KAYAKS(KAYAKS, "Sit-On-Top Kayaks"),
    PEDAL_KAYAKS(KAYAKS, "Pedal Kayaks"),

    INFLATABLE_BOATS(SMALL_BOATS, "Inflatable Boats"),
    CANOES(SMALL_BOATS, "Canoes"),

    TROLLING_MOTORS(ADDONS, "Trolling Motors"),
    ANCHORS(ADDONS, "Anchors"),
    MOUNTING_SYSTEMS(ADDONS, "Mounting Systems");

    private final Category parent;
    private final String displayName;

    Category(Category parent, String displayName) {
        this.parent = parent;
        this.displayName = displayName;
    }

    public Category getParent() {
        return parent;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public boolean isBottomLevel() {
        return getChildren().isEmpty();
    }

    public List<Category> getChildren() {
        return Arrays.stream(values())
                .filter(c -> c.parent == this)
                .toList();
    }

    public String getDisplayName() {
        return displayName;
    }
}
