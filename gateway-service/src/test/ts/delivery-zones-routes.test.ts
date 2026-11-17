import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, expect, it } from "vitest";
import { AuthRole } from "munchies-commons/kotlin/commons-modules";
import { GetDeliveryZonesRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-delivery-zones.route";
import { UpdateDeliveryZonesRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-delivery-zones.route";

// Delivery zones are an internal batching hint for scheduler-service, which reads them from
// restaurant-service directly: through the gateway only managers may read or replace them.
describe("Delivery zones routes", () => {
  it("serves the zones under the restaurant, manager-only", () => {
    const route = new GetDeliveryZonesRoute();
    expect(route.method.name).toBe("GET");
    expect(route.path).toBe("/restaurants/{restaurantId}/delivery-zones/");
    expect(route.authRole?.name).toBe(AuthRole.MANAGER.name);
  });

  it("replaces them with a manager-only PUT", () => {
    const route = new UpdateDeliveryZonesRoute();
    expect(route.method.name).toBe("PUT");
    expect(route.authRole?.name).toBe(AuthRole.MANAGER.name);
  });

  it("fills the manager id from the session, never from the client", () => {
    const route = new UpdateDeliveryZonesRoute();
    const request = route
      .parseRequest(JSON.stringify({ managerId: "forged", zones: [] }))
      .addId("manager-1");
    expect(request.managerId).toBe("manager-1");
  });
});
