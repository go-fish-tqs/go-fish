import http from "k6/http";
import { check, sleep } from "k6";

// Test configuration
export const options = {
  vus: 1, // 1 Virtual User
  duration: "10s", // Run for 10 seconds
};

export default function () {
  // 1. Define the target (Use environment variable or default to localhost)
  const BASE_URL = __ENV.NEXT_PUBLIC_BASE_URL || "http://localhost:8080";

  // 2. Make the request
  const res = http.get(`${BASE_URL}/actuator/health`);
  // ^ CHANGE THIS to a real endpoint if you don't have actuator (e.g., /api/items)

  // 3. Verify the response
  check(res, {
    "status is 200": (r) => r.status === 200,
  });

  sleep(1);
}
