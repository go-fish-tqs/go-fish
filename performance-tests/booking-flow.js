import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 10, // 10 concurrent users trying to book
  duration: "30s",
};

export default function () {
  const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

  // 1. LOGIN
  // Update these credentials to match a real user in your DB
  const loginPayload = JSON.stringify({
    email: "user@example.com",
    password: "password123",
  });

  const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
    headers: { "Content-Type": "application/json" },
  });

  const isLoggedIn = check(loginRes, {
    "Login successful": (r) => r.status === 200,
  });

  if (!isLoggedIn) {
    console.error("Login failed");
    return;
  }

  // Extract Token (Assuming LoginResponseDTO has a 'token' field)
  const authToken = loginRes.json("token");
  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${authToken}`,
  };

  // 2. CREATE BOOKING
  // We book Item #1 for a random date in the future to avoid conflicts
  const randomDay = Math.floor(Math.random() * 30) + 1;
  const bookingPayload = JSON.stringify({
    itemId: 1, // Ensure Item ID 1 exists
    startDate: `2025-06-${randomDay < 10 ? "0" + randomDay : randomDay}`,
    endDate: `2025-06-${randomDay < 10 ? "0" + randomDay : randomDay}`, // 1 day booking
  });

  const bookingRes = http.post(`${BASE_URL}/api/bookings`, bookingPayload, {
    headers: authHeaders,
  });

  check(bookingRes, {
    "Booking Created (201)": (r) => r.status === 201,
    // If 400, it might be an overlap/unavailable date, which is technically a "success" for the server logic
    "Handled Overlap Gracefully": (r) => r.status === 201 || r.status === 400,
  });

  sleep(2);
}
