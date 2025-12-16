import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  scenarios: {
    spike_test: {
      executor: "ramping-arrival-rate",
      startRate: 10,
      timeUnit: "1s",
      preAllocatedVUs: 50,
      stages: [
        { target: 100, duration: "10s" }, // Quickly spike to 100 reqs/sec
        { target: 0, duration: "10s" }, // Drop off
      ],
    },
  },
};

export default function () {
  const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
  const itemId = 1;

  // Requesting availability for a HUGE date range (1 year)
  //
  const from = "2025-01-01";
  const to = "2025-12-31";

  const res = http.get(
    `${BASE_URL}/api/items/${itemId}/unavailability?from=${from}&to=${to}`
  );

  check(res, {
    "Availability check fast": (r) => r.timings.duration < 200, // Should still be fast
    "Status 200": (r) => r.status === 200,
  });
}
