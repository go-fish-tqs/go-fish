import { getAuthHeaders } from "./auth";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

// Booking API
export const bookingApi = {
  async create(data: { itemId: number; startDate: string; endDate: string }) {
    const response = await fetch(`${API_BASE_URL}/bookings`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "Failed to create booking");
    }

    return response.json();
  },

  async updateStatus(bookingId: number, status: string) {
    const response = await fetch(
      `${API_BASE_URL}/bookings/${bookingId}/status`,
      {
        method: "PATCH",
        headers: getAuthHeaders(),
        body: JSON.stringify({ status }),
      }
    );

    if (!response.ok) {
      throw new Error("Failed to update booking status");
    }

    return response.json();
  },

  async getById(bookingId: number) {
    const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}`, {
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error("Failed to get booking");
    }

    return response.json();
  },
};

// Review API
export const reviewApi = {
  async create(data: { itemId: number; rating: number; comment?: string }) {
    const response = await fetch(`${API_BASE_URL}/reviews`, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || "Failed to create review");
    }

    return response.json();
  },

  async update(
    reviewId: number,
    data: {
      rating: number;
      comment?: string;
    }
  ) {
    const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error("Failed to update review");
    }

    return response.json();
  },

  async delete(reviewId: number) {
    const response = await fetch(`${API_BASE_URL}/reviews/${reviewId}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error("Failed to delete review");
    }
  },

  async getByItem(itemId: number, page: number = 0, size: number = 10) {
    const response = await fetch(
      `${API_BASE_URL}/reviews/item/${itemId}?page=${page}&size=${size}`,
      {
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      throw new Error("Failed to get reviews");
    }

    return response.json();
  },
};

// Item API (public endpoints don't need auth, but included for consistency)
export const itemApi = {
  async getAll(filters?: Record<string, any>) {
    const params = new URLSearchParams(filters).toString();
    const url = `${API_BASE_URL}/items${params ? `?${params}` : ""}`;

    const response = await fetch(url);

    if (!response.ok) {
      throw new Error("Failed to get items");
    }

    return response.json();
  },

  async getById(itemId: number) {
    const response = await fetch(`${API_BASE_URL}/items/${itemId}`);

    if (!response.ok) {
      throw new Error("Failed to get item");
    }

    return response.json();
  },
};
