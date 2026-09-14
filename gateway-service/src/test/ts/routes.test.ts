import "./setup-env";
import { describe, it, expect, vi } from "vitest";
import {
  fillPath,
  catchError,
  applyRoutes,
} from "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { HttpMethod } from "munchies-commons/kotlin/commons-modules";

describe("routes.ts", () => {
  describe("fillPath", () => {
    it("fills path parameters correctly", () => {
      const path = "/restaurants/{restaurantId}/menus/{menuId}";
      const result = fillPath(path, "rest-1", 42);
      expect(result).toBe("/restaurants/rest-1/menus/42");
    });

    it("throws error when not enough values are provided", () => {
      const path = "/users/{id}/details/{subId}";
      expect(() => fillPath(path, "user-1")).toThrow("Not enough values provided for path params");
    });
  });

  describe("catchError", () => {
    it("executes handler successfully when no error is thrown", async () => {
      const handler = vi.fn().mockResolvedValue("done");
      const wrapped = catchError(handler);
      const req = {} as any;
      const res = {} as any;
      const next = vi.fn();

      wrapped(req, res, next);

      // wait microtask queue
      await Promise.resolve();

      expect(handler).toHaveBeenCalledWith(req, res, next);
      expect(next).not.toHaveBeenCalled();
    });

    it("catches rejection and passes error to next", async () => {
      const error = new Error("Something broke");
      const handler = vi.fn().mockRejectedValue(error);
      const wrapped = catchError(handler);
      const req = {} as any;
      const res = {} as any;
      const next = vi.fn();

      wrapped(req, res, next);

      await Promise.resolve();
      await Promise.resolve();

      expect(handler).toHaveBeenCalledWith(req, res, next);
      expect(next).toHaveBeenCalledWith(error);
    });
  });

  describe("applyRoutes", () => {
    it("registers all routes with middleware onto express app", async () => {
      const registeredRoutes: { method: string; path: string; handlers: any[] }[] = [];

      const mockApp: any = {
        post: vi.fn((path: string, ...handlers: any[]) => {
          registeredRoutes.push({ method: "POST", path, handlers });
          return mockApp;
        }),
        put: vi.fn((path: string, ...handlers: any[]) => {
          registeredRoutes.push({ method: "PUT", path, handlers });
          return mockApp;
        }),
        delete: vi.fn((path: string, ...handlers: any[]) => {
          registeredRoutes.push({ method: "DELETE", path, handlers });
          return mockApp;
        }),
        patch: vi.fn((path: string, ...handlers: any[]) => {
          registeredRoutes.push({ method: "PATCH", path, handlers });
          return mockApp;
        }),
        get: vi.fn((path: string, ...handlers: any[]) => {
          registeredRoutes.push({ method: "GET", path, handlers });
          return mockApp;
        }),
      };

      applyRoutes(mockApp);

      // userRoutes (8) + restaurantRoutes (16) + orderRoutes (9) = 33 routes
      expect(registeredRoutes.length).toBe(33);

      // Verify each method was used at least once
      const methods = new Set(registeredRoutes.map((r) => r.method));
      expect(methods.has("POST")).toBe(true);
      expect(methods.has("PUT")).toBe(true);
      expect(methods.has("DELETE")).toBe(true);
      expect(methods.has("PATCH")).toBe(true);
      expect(methods.has("GET")).toBe(true);

      // Verify path conversion: curly braces converted to express colons
      const routeWithParam = registeredRoutes.find((r) => r.path.includes(":"));
      expect(routeWithParam).toBeDefined();

      // Test logRequests handler (first handler in chain)
      const firstRoute = registeredRoutes[0];
      const logHandler = firstRoute.handlers[0];
      const nextFn = vi.fn();
      const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});
      await logHandler({ body: { test: 1 } }, {}, nextFn);
      expect(consoleSpy).toHaveBeenCalled();
      expect(nextFn).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });
  });
});
