import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, expect, it } from "vitest";
import { AuthRole } from "munchies-commons/kotlin/commons-modules";
import { GetKitchenSetupRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-kitchen-setup.route";
import { UpdateKitchenSetupRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-kitchen-setup.route";

// The kitchen setup is input for scheduler-service's capacity agent, which reads it from
// restaurant-service directly: through the gateway only managers may read or replace it.
describe("Kitchen setup routes", () => {
  it("serves the kitchen under the restaurant, manager-only", () => {
    const route = new GetKitchenSetupRoute();
    expect(route.method.name).toBe("GET");
    expect(route.path).toBe("/restaurants/{restaurantId}/kitchen-setup/");
    expect(route.authRole?.name).toBe(AuthRole.MANAGER.name);
  });

  it("replaces it with a manager-only PUT", () => {
    const route = new UpdateKitchenSetupRoute();
    expect(route.method.name).toBe("PUT");
    expect(route.authRole?.name).toBe(AuthRole.MANAGER.name);
  });

  it("fills the manager id from the session and keeps the kitchen", () => {
    const route = new UpdateKitchenSetupRoute();
    const request = route
      .parseRequest(
        JSON.stringify({
          managerId: "forged",
          route: "PREP_THEN_COOK",
          prepWorkers: 2,
          cookStations: [{ name: "Oven", places: 7 }],
          prepMinutes: 3,
          loadingMinutes: 0.5,
          cookMinutes: 5,
        }),
      )
      .addId("manager-1");
    expect(request.managerId).toBe("manager-1");
    expect(request.prepWorkers).toBe(2);
    expect(request.cookStations[0].name).toBe("Oven");
    expect(request.cookMinutes).toBe(5);
  });
});
