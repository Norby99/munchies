import "./setup-env";
import { describe, it, expect, vi } from "vitest";

const { mockApp } = vi.hoisted(() => {
  const app: any = {
    use: vi.fn(),
    get: vi.fn((path: string, handler: any) => {
      if (path === "/health") {
        const res = { status: vi.fn().mockReturnThis(), json: vi.fn() };
        handler({}, res);
      }
    }),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    listen: vi.fn((port: number, cb: () => void) => {
      cb();
    }),
  };
  return { mockApp: app };
});

vi.mock("express", () => {
  const exp: any = vi.fn(() => mockApp);
  exp.raw = vi.fn().mockReturnValue("raw-middleware");
  return { default: exp };
});

vi.mock("cookie-parser", () => {
  return { default: vi.fn().mockReturnValue("cookie-parser-middleware") };
});

vi.mock("express-list-endpoints", () => {
  return {
    default: vi.fn().mockReturnValue([
      { methods: ["GET"], path: "/health" },
      { methods: ["POST"], path: "/users/login" },
    ]),
  };
});

describe("index.ts", () => {
  it("initializes and starts the gateway server", async () => {
    const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});

    await import("../../main/ts/index");

    expect(mockApp.use).toHaveBeenCalled();
    expect(mockApp.get).toHaveBeenCalledWith("/health", expect.any(Function));
    expect(mockApp.listen).toHaveBeenCalledWith(8080, expect.any(Function));

    consoleSpy.mockRestore();
  });
});
