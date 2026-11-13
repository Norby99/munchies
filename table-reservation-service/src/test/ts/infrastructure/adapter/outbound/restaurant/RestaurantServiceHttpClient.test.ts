import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import axios from "axios";
import { RestaurantServiceHttpClient } from "@main/infrastructure/adapter/outbound/restaurant/RestaurantServiceHttpClient";

vi.mock("axios");

const answer = (status: number, body: unknown) => ({
  status,
  data: typeof body === "string" ? body : JSON.stringify(body),
});

describe("RestaurantServiceHttpClient", () => {
  const originalUrl = process.env.RESTAURANT_SERVICE_URL;
  const client = new RestaurantServiceHttpClient();

  beforeEach(() => {
    // The automocked axios keeps its call history across tests unless cleared.
    vi.clearAllMocks();
    process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service:8080";
  });

  afterEach(() => {
    if (originalUrl === undefined) delete process.env.RESTAURANT_SERVICE_URL;
    else process.env.RESTAURANT_SERVICE_URL = originalUrl;
    vi.restoreAllMocks();
  });

  describe("getTables", () => {
    it("reads the tables of the restaurant", async () => {
      const get = vi.spyOn(axios, "get").mockResolvedValue(
        answer(200, {
          result: [
            { number: 1, seats: 2 },
            { number: 2, seats: 4 },
          ],
          code: 200,
        }),
      );

      const result = await client.getTables("restaurant-1");

      expect(get).toHaveBeenCalledWith(
        "http://restaurant-service:8080/restaurants/restaurant-1/tables/",
        expect.objectContaining({ timeout: 3000 }),
      );
      expect(result).toEqual({
        type: "FOUND",
        tables: [
          { number: 1, seats: 2 },
          { number: 2, seats: 4 },
        ],
      });
    });

    it("reports an unknown restaurant", async () => {
      vi.spyOn(axios, "get").mockResolvedValue(answer(404, { result: "not found", code: 404 }));
      expect(await client.getTables("missing")).toEqual({ type: "NOT_FOUND" });
    });

    it("is unavailable on an error status", async () => {
      vi.spyOn(axios, "get").mockResolvedValue(answer(500, "boom"));
      expect(await client.getTables("restaurant-1")).toMatchObject({ type: "UNAVAILABLE" });
    });

    it("is unavailable when the request fails", async () => {
      vi.spyOn(axios, "get").mockRejectedValue(new Error("ECONNREFUSED"));
      expect(await client.getTables("restaurant-1")).toEqual({
        type: "UNAVAILABLE",
        reason: "Restaurant Service request failed: ECONNREFUSED",
      });
    });

    it("is unavailable when the answer cannot be decoded", async () => {
      vi.spyOn(axios, "get").mockResolvedValue(answer(200, "<html>"));
      expect(await client.getTables("restaurant-1")).toMatchObject({ type: "UNAVAILABLE" });
    });

    it("is unavailable without the service URL", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const get = vi.spyOn(axios, "get");

      expect(await client.getTables("restaurant-1")).toEqual({
        type: "UNAVAILABLE",
        reason: "Missing Restaurant Service URL",
      });
      expect(get).not.toHaveBeenCalled();
    });
  });

  describe("isManagedBy", () => {
    const restaurants = (...ids: string[]) =>
      answer(200, {
        result: ids.map((id) => ({ id, name: "n", address: "a", phone: "p", email: "e" })),
        code: 200,
      });

    it("confirms a restaurant among the manager's own", async () => {
      const get = vi.spyOn(axios, "get").mockResolvedValue(restaurants("other", "restaurant-1"));

      expect(await client.isManagedBy("restaurant-1", "manager-1")).toEqual({ type: "MANAGED" });
      expect(get).toHaveBeenCalledWith(
        "http://restaurant-service:8080/restaurants/manager/manager-1/",
        expect.anything(),
      );
    });

    it("denies a restaurant that is not among them", async () => {
      vi.spyOn(axios, "get").mockResolvedValue(restaurants("other"));
      expect(await client.isManagedBy("restaurant-1", "manager-1")).toEqual({
        type: "NOT_MANAGED",
      });
    });

    it("is unavailable on an error status", async () => {
      vi.spyOn(axios, "get").mockResolvedValue(answer(503, "down"));
      expect(await client.isManagedBy("restaurant-1", "manager-1")).toMatchObject({
        type: "UNAVAILABLE",
      });
    });
  });
});
