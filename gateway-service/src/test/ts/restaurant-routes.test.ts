import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, it, expect, vi, afterEach } from "vitest";
import { restaurantRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/restaurant.routes";
import { CreateRestaurantRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/create-restaurant.route";
import { GetManagerRestaurantsRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-manager-restaurants.route";
import { GetRestaurantRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-restaurant.route";
import { UpdateRestaurantRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-restaurant.route";
import { DeleteRestaurantRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/delete-restaurant.route";
import { CreateMenuRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/create-menu.route";
import { GetMenuRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-menu.route";
import { GetRestaurantMenusRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-restaurant-menus.route";
import { UpdateMenuRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-menu.route";
import { DeleteMenuRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/delete-menu.route";
import { CreateCategoryRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/create-category.route";
import { UpdateCategoryRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-category.route";
import { DeleteCategoryRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/delete-category.route";
import { CreateMenuItemRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/create-menu-item.route";
import { UpdateMenuItemRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-menu-item.route";
import { RemoveMenuItemRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/remove-menu-item.route";
import { ErrorResponse, AuthRole } from "munchies-commons/kotlin/commons-modules";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";

function mockResponse() {
  const res: any = {
    statusCode: 200,
    status: vi.fn(function (code: number) {
      res.statusCode = code;
      return res;
    }),
    type: vi.fn(function () {
      return res;
    }),
    send: vi.fn(function (body: any) {
      return res;
    }),
  };
  return res;
}

const dummySuccess = {
  code: 200,
  toJson: () => JSON.stringify({ result: "ok", code: 200 }),
} as any;

const mockParsedReqWithAddId = {
  toJson: () => "{}",
  addId: vi.fn(function () {
    return this;
  }),
};

const mockParsedReqSimple = {
  toJson: () => "{}",
};

describe("Restaurant Routes", () => {
  const originalEnv = process.env.RESTAURANT_SERVICE_URL;

  afterEach(() => {
    if (originalEnv !== undefined) {
      process.env.RESTAURANT_SERVICE_URL = originalEnv;
    } else {
      delete process.env.RESTAURANT_SERVICE_URL;
    }
    vi.restoreAllMocks();
  });

  describe("restaurantRoutes array", () => {
    it("exports all 16 restaurant routes", () => {
      expect(restaurantRoutes.length).toBe(16);
    });
  });

  // ---- CreateRestaurantRoute ----
  describe("CreateRestaurantRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new CreateRestaurantRoute();
      const res = await route.createRestaurant({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles createRestaurant with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateRestaurantRoute();
      const res = await route.createRestaurant({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateRestaurantRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqWithAddId);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.MANAGER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "u1", role: AuthRole.MANAGER } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- GetManagerRestaurantsRoute ----
  describe("GetManagerRestaurantsRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new GetManagerRestaurantsRoute();
      const res = await route.getManagerRestaurants("mgr-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getManagerRestaurants with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetManagerRestaurantsRoute();
      const res = await route.getManagerRestaurants("mgr-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetManagerRestaurantsRoute();

      const resSuccess = await route.forward({
        user: { id: "mgr-1", role: AuthRole.MANAGER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ user: { id: "mgr-1", role: AuthRole.MANAGER } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- GetRestaurantRoute ----
  describe("GetRestaurantRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new GetRestaurantRoute();
      const res = await route.getRestaurant("rest-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getRestaurant with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetRestaurantRoute();
      const res = await route.getRestaurant("rest-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetRestaurantRoute();

      const resSuccess = await route.forward({ params: { restaurantId: "rest-1" } } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond({ params: { restaurantId: "rest-1" } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateRestaurantRoute ----
  describe("UpdateRestaurantRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new UpdateRestaurantRoute();
      const res = await route.updateRestaurant("rest-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateRestaurant with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateRestaurantRoute();
      const res = await route.updateRestaurant("rest-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateRestaurantRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqWithAddId);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "mgr-1", role: AuthRole.MANAGER },
        params: { restaurantId: "rest-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined, params: {} } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond(
        { body: "{}", user: { id: "mgr-1", role: AuthRole.MANAGER }, params: { restaurantId: "rest-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- DeleteRestaurantRoute ----
  describe("DeleteRestaurantRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new DeleteRestaurantRoute();
      const res = await route.deleteRestaurant("mgr-1", "rest-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles deleteRestaurant with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteRestaurantRoute();
      const res = await route.deleteRestaurant("mgr-1", "rest-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteRestaurantRoute();

      const resSuccess = await route.forward({
        user: { id: "mgr-1", role: AuthRole.MANAGER },
        params: { restaurantId: "rest-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ user: undefined, params: {} } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond(
        { user: { id: "mgr-1", role: AuthRole.MANAGER }, params: { restaurantId: "rest-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- CreateMenuRoute ----
  describe("CreateMenuRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new CreateMenuRoute();
      const res = await route.createMenu("rest-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles createMenu with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateMenuRoute();
      const res = await route.createMenu("rest-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateMenuRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqWithAddId);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "mgr-1", role: AuthRole.MANAGER },
        params: { restaurantId: "rest-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined, params: {} } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond(
        { body: "{}", user: { id: "mgr-1", role: AuthRole.MANAGER }, params: { restaurantId: "rest-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- GetMenuRoute ----
  describe("GetMenuRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new GetMenuRoute();
      const res = await route.getMenu("rest-1", "menu-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getMenu with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetMenuRoute();
      const res = await route.getMenu("rest-1", "menu-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetMenuRoute();

      const resSuccess = await route.forward({
        params: { restaurantId: "rest-1", menuId: "menu-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond({ params: { restaurantId: "rest-1", menuId: "menu-1" } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- GetRestaurantMenusRoute ----
  describe("GetRestaurantMenusRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new GetRestaurantMenusRoute();
      const res = await route.getRestaurantMenus("rest-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getRestaurantMenus with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetRestaurantMenusRoute();
      const res = await route.getRestaurantMenus("rest-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetRestaurantMenusRoute();

      const resSuccess = await route.forward({ params: { restaurantId: "rest-1" } } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond({ params: { restaurantId: "rest-1" } } as any, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateMenuRoute ----
  describe("UpdateMenuRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new UpdateMenuRoute();
      const res = await route.updateMenu("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateMenu with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateMenuRoute();
      const res = await route.updateMenu("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateMenuRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqWithAddId);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "mgr-1", role: AuthRole.MANAGER },
        params: { restaurantId: "rest-1", menuId: "menu-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined, params: {} } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond(
        { body: "{}", user: { id: "mgr-1", role: AuthRole.MANAGER }, params: { restaurantId: "rest-1", menuId: "menu-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- DeleteMenuRoute ----
  describe("DeleteMenuRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new DeleteMenuRoute();
      const res = await route.deleteMenu("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles deleteMenu with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteMenuRoute();
      const res = await route.deleteMenu("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteMenuRoute();

      const resSuccess = await route.forward({
        user: { id: "mgr-1", role: AuthRole.MANAGER },
        params: { restaurantId: "rest-1", menuId: "menu-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ user: undefined, params: {} } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond(
        { user: { id: "mgr-1", role: AuthRole.MANAGER }, params: { restaurantId: "rest-1", menuId: "menu-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- CreateCategoryRoute ----
  describe("CreateCategoryRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new CreateCategoryRoute();
      const res = await route.createCategory("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles createCategory with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateCategoryRoute();
      const res = await route.createCategory("rest-1", "menu-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateCategoryRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqSimple);

      const resSuccess = await route.forward({
        body: "{}",
        params: { restaurantId: "rest-1", menuId: "menu-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { body: "{}", params: { restaurantId: "rest-1", menuId: "menu-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateCategoryRoute ----
  describe("UpdateCategoryRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new UpdateCategoryRoute();
      const res = await route.updateCategory("rest-1", "menu-1", "cat-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateCategory with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateCategoryRoute();
      const res = await route.updateCategory("rest-1", "menu-1", "cat-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateCategoryRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqSimple);

      const resSuccess = await route.forward({
        body: "{}",
        params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { body: "{}", params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- DeleteCategoryRoute ----
  describe("DeleteCategoryRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new DeleteCategoryRoute();
      const res = await route.deleteCategory("rest-1", "menu-1", "cat-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles deleteCategory with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteCategoryRoute();
      const res = await route.deleteCategory("rest-1", "menu-1", "cat-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteCategoryRoute();

      const resSuccess = await route.forward({
        params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- CreateMenuItemRoute ----
  describe("CreateMenuItemRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new CreateMenuItemRoute();
      const res = await route.createMenuItem("rest-1", "menu-1", "cat-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles createMenuItem with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateMenuItemRoute();
      const res = await route.createMenuItem("rest-1", "menu-1", "cat-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new CreateMenuItemRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqSimple);

      const resSuccess = await route.forward({
        body: "{}",
        params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { body: "{}", params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateMenuItemRoute ----
  describe("UpdateMenuItemRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new UpdateMenuItemRoute();
      const res = await route.updateMenuItem("rest-1", "menu-1", "cat-1", "item-1", { toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateMenuItem with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateMenuItemRoute();
      const res = await route.updateMenuItem("rest-1", "menu-1", "cat-1", "item-1", { toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateMenuItemRoute();
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReqSimple);

      const resSuccess = await route.forward({
        body: "{}",
        params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1", itemId: "item-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { body: "{}", params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1", itemId: "item-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- RemoveMenuItemRoute ----
  describe("RemoveMenuItemRoute", () => {
    it("returns 500 when RESTAURANT_SERVICE_URL is missing", async () => {
      delete process.env.RESTAURANT_SERVICE_URL;
      const route = new RemoveMenuItemRoute();
      const res = await route.removeMenuItem("rest-1", "menu-1", "cat-1", "item-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles removeMenuItem with RESTAURANT_SERVICE_URL", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new RemoveMenuItemRoute();
      const res = await route.removeMenuItem("rest-1", "menu-1", "cat-1", "item-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.RESTAURANT_SERVICE_URL = "http://restaurant-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new RemoveMenuItemRoute();

      const resSuccess = await route.forward({
        params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1", itemId: "item-1" },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const res = mockResponse();
      await route.respond(
        { params: { restaurantId: "rest-1", menuId: "menu-1", categoryId: "cat-1", itemId: "item-1" } } as any,
        res,
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });
});
