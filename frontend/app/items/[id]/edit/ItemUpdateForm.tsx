"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAuthHeaders } from "@/app/lib/auth";
import { Item } from "@/app/items/types";

interface ItemUpdateFormProps {
  itemId: string;
}

export default function ItemUpdateForm({ itemId }: ItemUpdateFormProps) {
  const router = useRouter();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [newPhotoUrl, setNewPhotoUrl] = useState("");

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    category: "",
    material: "",
    price: "",
    available: true,
    photoUrls: [] as string[],
  });

  // Load existing item data
  useEffect(() => {
    async function loadItem() {
      try {
        console.log("Loading item:", itemId);
        const response = await fetch(`http://localhost:8080/items/${itemId}`, {
          headers: getAuthHeaders(),
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
          throw new Error(`Failed to load item: ${response.status}`);
        }

        const item: Item = await response.json();
        console.log("Loaded item:", item);

        // Extract category and material as strings
        // Category comes as object: {id: "ANCHORS", displayName: "Anchors", ...}
        // Material comes as string: "TITANIUM"
        const categoryValue =
          typeof item.category === "object" && item.category !== null
            ? (item.category as any).id || ""
            : item.category || "";

        const materialValue =
          typeof item.material === "object" && item.material !== null
            ? (item.material as any).id || ""
            : item.material || "";

        console.log("Setting category to:", categoryValue);
        console.log("Setting material to:", materialValue);

        setFormData({
          name: item.name || "",
          description: item.description || "",
          category: categoryValue,
          material: materialValue,
          price: item.price?.toString() || "",
          available: item.available ?? true,
          photoUrls: item.photoUrls || [],
        });
        setLoading(false);
      } catch (err) {
        console.error("Error loading item:", err);
        setError("Failed to load item details");
        setLoading(false);
      }
    }

    if (itemId) {
      loadItem();
    } else {
      setLoading(false);
    }
  }, [itemId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError("");

    try {
      // Prepare update data - only send non-empty values
      const updateData: Record<string, any> = {
        available: formData.available, // Always include availability
      };

      // Add other fields only if they have values
      if (formData.name?.trim()) updateData.name = formData.name.trim();
      if (formData.description?.trim())
        updateData.description = formData.description.trim();
      if (formData.category) updateData.category = formData.category;
      if (formData.material) updateData.material = formData.material;
      if (formData.price && parseFloat(formData.price) > 0) {
        updateData.price = parseFloat(formData.price);
      }
      if (formData.photoUrls && formData.photoUrls.length > 0) {
        updateData.photoUrls = formData.photoUrls;
      }

      console.log("Updating item with data:", updateData);

      const headers = getAuthHeaders();
      console.log("Request headers:", headers);
      console.log("Token from localStorage:", localStorage.getItem("token"));
      console.log("UserId from localStorage:", localStorage.getItem("userId"));

      const response = await fetch(`http://localhost:8080/items/${itemId}`, {
        method: "PUT",
        headers: headers,
        body: JSON.stringify(updateData),
      });

      console.log("Update response status:", response.status);

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error("You don't have permission to edit this item");
        }
        if (response.status === 404) {
          throw new Error("Item not found");
        }
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to update item");
      }

      // Success! Redirect to item page
      router.push(`/items/${itemId}`);
      router.refresh();
    } catch (err: any) {
      setError(err.message || "Failed to update item");
      console.error("Update error:", err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto space-y-6 bg-white p-8 rounded-xl shadow-lg border border-gray-200">
        <div className="border-b border-gray-200 pb-4 space-y-3">
          <div className="h-8 bg-gray-200 rounded w-1/3 animate-pulse flex-shrink-0"></div>
          <div className="h-4 bg-gray-200 rounded w-full animate-pulse flex-shrink-0"></div>
        </div>
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="space-y-2">
            <div className="h-4 bg-gray-200 rounded w-1/4 animate-pulse flex-shrink-0"></div>
            <div className="h-10 bg-gray-200 rounded animate-pulse flex-shrink-0"></div>
          </div>
        ))}
        <div className="h-12 bg-gray-200 rounded animate-pulse flex-shrink-0"></div>
      </div>
    );
  }

  if (error && !formData.name) {
    return (
      <div className="max-w-2xl mx-auto text-center py-8">
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
          {error}
        </div>
        <button
          onClick={() => router.back()}
          className="mt-4 px-6 py-2 border rounded-lg hover:bg-gray-50"
        >
          Go Back
        </button>
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="max-w-3xl mx-auto space-y-6 bg-white p-8 rounded-xl shadow-lg border border-gray-200"
    >
      {/* Header */}
      <div className="border-b border-gray-200 pb-4">
        <h1 className="text-2xl font-bold text-gray-900">Edit Item Details</h1>
        <p className="text-sm text-gray-700 mt-1">
          Update any field you want to change. Leave others as they are.
        </p>
      </div>

      {/* Item Name */}
      <div>
        <label className="block text-sm font-semibold text-gray-900 mb-2">
          Item Name
        </label>
        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
          placeholder="Enter item name"
        />
        <p className="text-xs text-gray-600 mt-1">
          The display name for your item
        </p>
      </div>

      {/* Description */}
      <div>
        <label className="block text-sm font-semibold text-gray-900 mb-2">
          Description
        </label>
        <textarea
          value={formData.description}
          onChange={(e) =>
            setFormData({ ...formData, description: e.target.value })
          }
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
          rows={4}
          placeholder="Describe your item in detail"
        />
        <p className="text-xs text-gray-600 mt-1">
          Detailed description to help renters understand your item
        </p>
      </div>

      {/* Category and Material */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-semibold text-gray-900 mb-2">
            Category
          </label>
          <select
            value={formData.category}
            onChange={(e) =>
              setFormData({ ...formData, category: e.target.value })
            }
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
          >
            <option value="">Select Category</option>
            <optgroup label="Top Level">
              <option value="RODS">Rods</option>
              <option value="REELS">Reels</option>
              <option value="COMBOS">Rod & Reel Combos</option>
              <option value="ELECTRONICS">Electronics</option>
              <option value="APPAREL">Apparel</option>
              <option value="ACCESSORIES">Accessories</option>
              <option value="BOATS">Boats & Kayaks</option>
            </optgroup>
            <optgroup label="Rods">
              <option value="RODS_SPINNING">Spinning Rods</option>
              <option value="RODS_CASTING">Casting Rods</option>
              <option value="RODS_FLY">Fly Rods</option>
              <option value="RODS_SPECIALTY">Specialty Rods</option>
            </optgroup>
            <optgroup label="Reels">
              <option value="REELS_SPINNING">Spinning Reels</option>
              <option value="REELS_CASTING">Casting Reels</option>
              <option value="REELS_FLY">Fly Reels</option>
              <option value="REELS_SPECIALTY">Specialty Reels</option>
            </optgroup>
            <optgroup label="Combos">
              <option value="SPINNING_COMBOS">Spinning Combos</option>
              <option value="CASTING_COMBOS">Casting Combos</option>
              <option value="FLY_COMBOS">Fly Combos</option>
            </optgroup>
            <optgroup label="Electronics">
              <option value="NAVIGATION">Navigation Devices</option>
              <option value="FISH_DETECTION">Fish Detection Equipment</option>
              <option value="CAMERA_SYSTEMS">Camera Systems</option>
            </optgroup>
            <optgroup label="Apparel">
              <option value="WADING">Wading Gear</option>
              <option value="SAFETY">Safety Gear</option>
              <option value="WEATHER">Weather Protection</option>
            </optgroup>
            <optgroup label="Accessories">
              <option value="LANDING_TOOLS">Landing Tools</option>
              <option value="MEASURING_TOOLS">Measuring Tools</option>
              <option value="STORAGE">Storage</option>
              <option value="MISC_GEAR">Misc Gear</option>
              <option value="NETS">Nets</option>
            </optgroup>
            <optgroup label="Boats & Kayaks">
              <option value="KAYAKS">Fishing Kayaks</option>
              <option value="SMALL_BOATS">Small Boats</option>
              <option value="ADDONS">Boat Add-ons</option>
              <option value="ANCHORS">Anchors</option>
            </optgroup>
          </select>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-900 mb-2">
            Material
          </label>
          <select
            value={formData.material}
            onChange={(e) =>
              setFormData({ ...formData, material: e.target.value })
            }
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
          >
            <option value="">Select Material</option>
            <optgroup label="Rod Materials">
              <option value="GRAPHITE">Graphite</option>
              <option value="FIBERGLASS">Fiberglass</option>
              <option value="COMPOSITE">Composite Blend</option>
              <option value="CARBON_FIBER">Carbon Fiber</option>
            </optgroup>
            <optgroup label="Reel Materials">
              <option value="ALUMINUM">Aluminum</option>
              <option value="MACHINED_ALUMINUM">Machined Aluminum</option>
              <option value="DIE_CAST_ALUMINUM">Die-Cast Aluminum</option>
              <option value="STAINLESS_STEEL">Stainless Steel</option>
              <option value="MAGNESIUM">Magnesium</option>
              <option value="BRASS">Brass</option>
              <option value="CARBON_COMPOSITE">Carbon Composite</option>
              <option value="POLYMER">Polymer</option>
            </optgroup>
            <optgroup label="Boat Materials">
              <option value="ROTOMOLDED_POLYETHYLENE">
                Rotomolded Polyethylene
              </option>
              <option value="THERMOFORMED_PLASTIC">Thermoformed Plastic</option>
              <option value="INFLATABLE_PVC">Inflatable PVC</option>
              <option value="HYPALON">Hypalon</option>
              <option value="FIBERGLASS_BOAT">Fiberglass</option>
              <option value="ALUMINUM_BOAT">Aluminum Boat</option>
            </optgroup>
            <optgroup label="Apparel Materials">
              <option value="NEOPRENE">Neoprene</option>
              <option value="BREATHABLE_FABRIC">Breathable Fabric</option>
              <option value="NYLON">Nylon</option>
              <option value="POLYESTER">Polyester</option>
              <option value="RUBBER">Rubber</option>
              <option value="PVC_COATED_FABRIC">PVC-Coated Fabric</option>
            </optgroup>
            <optgroup label="Accessory Materials">
              <option value="CARBON_STEEL">Carbon Steel</option>
              <option value="ANODIZED_ALUMINUM">Anodized Aluminum</option>
              <option value="TITANIUM">Titanium</option>
              <option value="HARD_PLASTIC">Hard Plastic</option>
              <option value="EVA_FOAM">EVA Foam</option>
              <option value="CORK">Cork</option>
              <option value="COMPOSITE_HANDLE">Composite Handle</option>
            </optgroup>
            <optgroup label="Net Materials">
              <option value="RUBBER_MESH">Rubber Mesh</option>
              <option value="KNOTLESS_NYLON_MESH">Knotless Nylon Mesh</option>
              <option value="ALUMINUM_FRAME">Aluminum Frame</option>
              <option value="CARBON_FRAME">Carbon Frame</option>
            </optgroup>
          </select>
        </div>
      </div>

      {/* Price */}
      <div>
        <label className="block text-sm font-semibold text-gray-900 mb-2">
          Daily Rental Price (€)
        </label>
        <div className="relative">
          <span className="absolute left-4 top-3.5 text-gray-600 font-medium">
            €
          </span>
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={formData.price}
            onChange={(e) =>
              setFormData({ ...formData, price: e.target.value })
            }
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
            placeholder="0.00"
          />
        </div>
        <p className="text-xs text-gray-600 mt-1">
          ⚠️ Price changes won't affect existing bookings
        </p>
      </div>

      {/* Photo URLs */}
      <div>
        <label className="block text-sm font-semibold text-gray-900 mb-2">
          Photo URLs
        </label>
        <div className="space-y-3">
          {/* Existing photos */}
          {formData.photoUrls.length > 0 ? (
            <div className="space-y-2">
              {formData.photoUrls.map((url, index) => (
                <div key={index} className="flex gap-2 items-start">
                  <div className="flex-1">
                    <input
                      type="url"
                      value={url}
                      onChange={(e) => {
                        const newUrls = [...formData.photoUrls];
                        newUrls[index] = e.target.value;
                        setFormData({ ...formData, photoUrls: newUrls });
                      }}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
                      placeholder="https://example.com/photo.jpg"
                    />
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      const newUrls = formData.photoUrls.filter(
                        (_, i) => i !== index
                      );
                      setFormData({ ...formData, photoUrls: newUrls });
                    }}
                    className="px-4 py-3 bg-red-50 text-red-600 border border-red-300 rounded-lg hover:bg-red-100 font-medium transition-colors"
                    title="Remove photo"
                  >
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500 italic py-2">
              No photos added yet
            </p>
          )}

          {/* Add new photo */}
          <div className="border-t border-gray-200 pt-3">
            <div className="flex gap-2">
              <input
                type="url"
                value={newPhotoUrl}
                onChange={(e) => setNewPhotoUrl(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    if (newPhotoUrl.trim()) {
                      setFormData({
                        ...formData,
                        photoUrls: [...formData.photoUrls, newPhotoUrl.trim()],
                      });
                      setNewPhotoUrl("");
                    }
                  }
                }}
                className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900"
                placeholder="Enter new photo URL and click Add"
              />
              <button
                type="button"
                onClick={() => {
                  if (newPhotoUrl.trim()) {
                    setFormData({
                      ...formData,
                      photoUrls: [...formData.photoUrls, newPhotoUrl.trim()],
                    });
                    setNewPhotoUrl("");
                  }
                }}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
              >
                Add
              </button>
            </div>
          </div>
        </div>
        <p className="text-xs text-gray-600 mt-2">
          Add URLs to photos of your item. You can add multiple photos and
          reorder them.
        </p>
      </div>

      {/* Availability */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-center gap-3">
          <input
            type="checkbox"
            id="available"
            checked={formData.available}
            onChange={(e) =>
              setFormData({ ...formData, available: e.target.checked })
            }
            className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <label
            htmlFor="available"
            className="text-sm font-semibold text-gray-900"
          >
            Item is available for rent
          </label>
        </div>
        <p className="text-xs text-gray-700 mt-2 ml-8">
          Uncheck to temporarily make this item unavailable for new bookings
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-lg">
          <div className="flex items-start">
            <svg
              className="w-5 h-5 mr-2 mt-0.5"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            <span>{error}</span>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-4 pt-4 border-t border-gray-200">
        <button
          type="submit"
          disabled={saving}
          className="flex-1 bg-blue-600 text-white py-3 px-6 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-semibold transition-colors shadow-sm"
        >
          {saving ? (
            <span className="flex items-center justify-center gap-2">
              <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                  fill="none"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              Saving...
            </span>
          ) : (
            "Update Item"
          )}
        </button>
        <button
          type="button"
          onClick={() => router.back()}
          className="px-8 py-3 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-semibold transition-colors"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
