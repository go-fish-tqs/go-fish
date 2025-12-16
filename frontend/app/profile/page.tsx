"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import toast from "react-hot-toast";

interface UserProfile {
  id: number;
  username: string;
  email: string;
  phone?: string;
  address?: string;
  profilePhoto?: string;
  location: string;
  balance: number;
}

export default function ProfilePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    phone: "",
    address: "",
    profilePhoto: "",
    location: "",
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem("authToken");
        const userId = localStorage.getItem("userId");

        console.log("Token:", token ? "exists" : "missing");
        console.log("User ID:", userId);

        if (!token || !userId) {
          toast.error("Please login first");
          router.push("/login");
          return;
        }

        const url = `http://localhost:8080/api/users/${userId}`;
        console.log("Fetching profile from:", url);

        const response = await fetch(url, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
          const errorText = await response.text();
          console.error("Error response:", errorText);
          throw new Error(`Failed to fetch profile: ${response.status}`);
        }

        const data = await response.json();
        console.log("Profile data received:", data);
        setProfile(data);
        
        // Only set values that are not null
        const newFormData: any = {
          username: data.username || "",
          email: data.email || "",
          location: data.location || "",
        };
        
        if (data.phone) newFormData.phone = data.phone;
        if (data.address) newFormData.address = data.address;
        if (data.profilePhoto) newFormData.profilePhoto = data.profilePhoto;
        
        setFormData({
          username: newFormData.username,
          email: newFormData.email,
          phone: newFormData.phone || "",
          address: newFormData.address || "",
          profilePhoto: newFormData.profilePhoto || "",
          location: newFormData.location,
        });
      } catch (error) {
        console.error("Profile fetch error:", error);
        toast.error("Failed to load profile");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);

    try {
      const token = localStorage.getItem("authToken");
      const userId = localStorage.getItem("userId");

      if (!token || !userId) {
        toast.error("Please login first");
        router.push("/login");
        return;
      }

      // Send only non-empty fields for partial update
      const updateData: Partial<typeof formData> = {};
      if (formData.username && formData.username !== profile?.username) {
        updateData.username = formData.username;
      }
      if (formData.email && formData.email !== profile?.email) {
        updateData.email = formData.email;
      }
      if (formData.phone) {
        updateData.phone = formData.phone;
      }
      if (formData.address) {
        updateData.address = formData.address;
      }
      if (formData.profilePhoto) {
        updateData.profilePhoto = formData.profilePhoto;
      }
      if (formData.location && formData.location !== profile?.location) {
        updateData.location = formData.location;
      }

      const response = await fetch(`http://localhost:8080/api/users/${userId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(updateData),
      });

      if (response.status === 409) {
        toast.error("Email already in use");
        return;
      }

      if (!response.ok) {
        throw new Error("Failed to update profile");
      }

      const updatedProfile = await response.json();
      setProfile(updatedProfile);
      
      // Update localStorage with new data
      localStorage.setItem("userName", updatedProfile.username);
      localStorage.setItem("userEmail", updatedProfile.email);
      if (updatedProfile.profilePhoto) {
        localStorage.setItem("profilePhoto", updatedProfile.profilePhoto);
      }
      
      // Dispatch custom event to update Sidebar
      window.dispatchEvent(new Event('userDataUpdated'));

      toast.success("Profile updated successfully!");
    } catch (error) {
      toast.error("Failed to update profile");
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-8 px-4">
      <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-500 to-indigo-600 px-8 py-6">
          <h1 className="text-3xl font-bold text-white">My Profile</h1>
          <p className="text-blue-100 mt-1">Manage your account information</p>
        </div>

        {/* Profile Info */}
        <div className="p-8">
          {profile && (
            <div className="mb-8 flex items-center gap-6">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-xl">
                {formData.profilePhoto ? (
                  <img
                    src={formData.profilePhoto}
                    alt="Profile"
                    className="w-full h-full rounded-full object-cover"
                  />
                ) : (
                  <span className="text-3xl font-bold text-white">
                    {formData.username.charAt(0).toUpperCase()}
                  </span>
                )}
              </div>
              <div>
                <h2 className="text-2xl font-bold text-gray-800">{profile.username}</h2>
                <p className="text-gray-600">{profile.email}</p>
                <p className="text-sm text-gray-500 mt-1">Balance: €{profile.balance.toFixed(2)}</p>
              </div>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Username */}
              <div>
                <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                  Username
                </label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  required
                  minLength={2}
                  maxLength={32}
                />
              </div>
 <span className="text-gray-400 text-xs">(optional)</span>
              {/* Email */}
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  required
                  maxLength={64}
                />
              </div>

              {/* Phone */}
              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                  Phone Number
                </label>
                <input
                  type="tel"
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="+351912345678"
                  pattern="^[+]?[0-9]{9,15}$"
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                />
                <p className="text-xs text-gray-500 mt-1">Format: +351912345678 (9-15 digits)</p>
              </div>

              {/* Location */}
              <div>
                <label htmlFor="location" className="block text-sm font-medium text-gray-700 mb-2">
                  Location
                </label>
                <input
                  type="text"
                  id="location"
                  name="location"
                  value={formData.location}
                  onChange={handleChange}
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  maxLength={255}
                />
              </div>

              {/* Address */}
              <div className="md:col-span-2">
                <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-2">
                  Address <span className="text-gray-400 text-xs">(optional)</span>
                </label>
                <textarea
                  id="address"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  rows={2}
                  placeholder="Your address"
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all resize-none"
                  maxLength={255}
                />
              </div>

              {/* Profile Photo URL */}
              <div className="md:col-span-2">
                <label htmlFor="profilePhoto" className="block text-sm font-medium text-gray-700 mb-2">
                  Profile Photo URL <span className="text-gray-400 text-xs">(optional)</span>
                </label>
                <input
                  type="url"
                  id="profilePhoto"
                  name="profilePhoto"
                  value={formData.profilePhoto}
                  onChange={handleChange}
                  placeholder="https://example.com/photo.jpg"
                  className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                />
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-4 pt-6 border-t">
              <button
                type="submit"
                disabled={saving}
                className="flex-1 bg-gradient-to-r from-blue-500 to-indigo-600 text-white font-medium py-3 px-6 rounded-lg hover:from-blue-600 hover:to-indigo-700 transition-all duration-300 shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Saving...
                  </span>
                ) : (
                  "Save Changes"
                )}
              </button>
              <button
                type="button"
                onClick={() => router.back()}
                className="px-6 py-3 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-all"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
