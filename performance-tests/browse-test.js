import http from "k6/http";
import { check, sleep } from "k6";

// Scenario: 50 users browsing the catalog simultaneously
export const options = {
  stages: [
    { duration: "30s", target: 20 }, // Ramp up to 20 users
    { duration: "1m", target: 50 }, // Stress test at 50 users
    { duration: "30s", target: 0 }, // Cool down
  ],
  thresholds: {
    http_req_duration: ["p(95)<500"], // 95% of searches must be faster than 500ms
  },
};

export default function () {
  const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

  // 1. Search for items (Empty filter = Find All)
  //
  const searchPayload = JSON.stringify({});
  const params = { headers: { "Content-Type": "application/json" } };

  const resSearch = http.post(
    `${BASE_URL}/api/items/filter`,
    searchPayload,
    params
  );

  const searchSuccess = check(resSearch, {
    "Search status is 200": (r) => r.status === 200,
    "Search returns JSON": (r) =>
      r.headers["Content-Type"] &&
      r.headers["Content-Type"].includes("application/json"),
  });

  // 2. If search worked, simulate clicking on the first item found
  if (searchSuccess && resSearch.json().length > 0) {
    const firstItemId = resSearch.json()[0].id;

    //
    const resDetail = http.get(`${BASE_URL}/api/items/${firstItemId}`);

    check(resDetail, {
      "Item detail status is 200": (r) => r.status === 200,
    });
  }

  sleep(1);
}
