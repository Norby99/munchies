import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, expect, it } from "vitest";
import { AuthRole } from "munchies-commons/kotlin/commons-modules";
import { GetCapacityPolicyRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/get-capacity-policy.route";
import { UpdateCapacityPolicyRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/restaurant/update-capacity-policy.route";

// The capacity policy carries the restaurant's opening hours, shown to customers on the
// restaurant page: customers may read it, only managers may change it.
describe("Capacity policy routes", () => {
  it("lets customers read the policy", () => {
    const route = new GetCapacityPolicyRoute();
    expect(route.method.name).toBe("GET");
    expect(route.authRole?.name).toBe(AuthRole.CUSTOMER.name);
  });

  it("keeps updating it manager-only", () => {
    const route = new UpdateCapacityPolicyRoute();
    expect(route.method.name).toBe("PUT");
    expect(route.authRole?.name).toBe(AuthRole.MANAGER.name);
  });
});
