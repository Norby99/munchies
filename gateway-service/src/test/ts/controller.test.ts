import { describe, it, expect } from "vitest";
import {
  GatewayController,
  UserController,
} from "../../main/ts/infrastructure/adapter/inbound/web/controller/controller";

describe("controller.ts", () => {
  it("GatewayController can be instantiated and exampleEndpoint called", () => {
    const gc = new GatewayController();
    expect(gc).toBeDefined();
    expect(() => gc.exampleEndpoint()).not.toThrow();
  });

  it("UserController can be instantiated and logout called", () => {
    const uc = new UserController();
    expect(uc).toBeDefined();
    expect(() => uc.logout()).not.toThrow();
  });
});
