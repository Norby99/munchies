import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { afterEach, describe, expect, it, vi } from "vitest";
import { AuthRole, ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import { applyRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import { SearchRestaurantsRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/search-restaurants.route";

const customer = { id: "cust-1", role: AuthRole.CUSTOMER };
const ok = { code: 200, toJson: () => JSON.stringify({ result: [], code: 200 }) } as any;

describe("SearchRestaurantsRoute", () => {
  const originalUrl = process.env.RESTAURANT_SERVICE_URL;

  afterEach(() => {
    if (originalUrl === undefined) delete process.env.RESTAURANT_SERVICE_URL;
    else process.env.RESTAURANT_SERVICE_URL = originalUrl;
    vi.restoreAllMocks();
  });

  it("is exposed as GET /restaurants/ for customers", () => {
    const route = new SearchRestaurantsRoute();
    expect(route.path).toBe("/restaurants/");
    expect(route.method.name).toBe("GET");
    expect(route.authRole?.name).toBe(AuthRole.CUSTOMER.name);
  });

  it("is registered on the gateway", () => {
    const registered: string[] = [];
    const record = (method: string) =>
      vi.fn((path: string) => {
        registered.push(`${method} ${path}`);
        return app;
      });
    const app: any = {
      get: record("GET"),
      post: record("POST"),
      put: record("PUT"),
      patch: record("PATCH"),
      delete: record("DELETE"),
    };
    applyRoutes(app);
    expect(registered).toContain("GET /restaurants/");
  });

  it("forwards q, URL-encoded, to restaurant-service", async () => {
    process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(ok);

    await new SearchRestaurantsRoute().forward({ query: { q: "pizza & co" }, user: customer } as any);

    expect(spy.mock.calls[0][0]).toBe("http://restaurant-service/restaurants/?q=pizza+%26+co");
    expect((spy.mock.calls[0][1] as any).name).toBe("GET");
  });

  it("sends an empty q when the client gives none, and keeps only the first of repeated ones", async () => {
    process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
    const spy = vi.spyOn(internalClient, "request").mockResolvedValue(ok);
    const route = new SearchRestaurantsRoute();

    await route.forward({ query: {}, user: customer } as any);
    await route.forward({ query: { q: ["sushi", "pizza"] }, user: customer } as any);

    expect(spy.mock.calls[0][0]).toBe("http://restaurant-service/restaurants/?q=");
    expect(spy.mock.calls[1][0]).toBe("http://restaurant-service/restaurants/?q=sushi");
  });

  it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
    delete process.env.RESTAURANT_SERVICE_URL;
    const res = await new SearchRestaurantsRoute().forward({ query: { q: "x" }, user: customer } as any);
    expect(res).toBeInstanceOf(ErrorResponse);
    expect(res.code).toBe(500);
  });
});
