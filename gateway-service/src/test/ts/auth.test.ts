import "./setup-env";
import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  parseAuthRoleString,
  requireAuth,
  requireRole,
  injectCookie,
  clearCookie,
} from "../../main/ts/infrastructure/adapter/middleware/auth";
import { AuthRole, ErrorResponse } from "munchies-commons/kotlin/commons-modules";

function mockRes() {
  const res: any = {
    statusCode: 200,
    status: vi.fn(function (code: number) {
      res.statusCode = code;
      return res;
    }),
    type: vi.fn(function () {
      return res;
    }),
    send: vi.fn(function () {
      return res;
    }),
    cookie: vi.fn(function () {
      return res;
    }),
    clearCookie: vi.fn(function () {
      return res;
    }),
  };
  return res;
}

function mockReq(overrides: any = {}) {
  return {
    cookies: {},
    ...overrides,
  };
}

describe("auth.ts", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  describe("parseAuthRoleString", () => {
    it("parses CUSTOMER correctly (uppercase)", () => {
      expect(parseAuthRoleString("CUSTOMER")).toBe(AuthRole.CUSTOMER);
    });

    it("parses CUSTOMER correctly (lowercase)", () => {
      expect(parseAuthRoleString("customer")).toBe(AuthRole.CUSTOMER);
    });

    it("parses MANAGER correctly (uppercase)", () => {
      expect(parseAuthRoleString("MANAGER")).toBe(AuthRole.MANAGER);
    });

    it("parses MANAGER correctly (lowercase)", () => {
      expect(parseAuthRoleString("manager")).toBe(AuthRole.MANAGER);
    });

    it("throws for unknown role", () => {
      expect(() => parseAuthRoleString("ADMIN")).toThrow("Unexpected Role");
    });
  });

  describe("requireAuth", () => {
    it("returns 401 when authToken cookie is missing", async () => {
      const req = mockReq({ cookies: {} });
      const res = mockRes();
      const next = vi.fn();
      const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});

      const middleware = requireAuth();
      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
      consoleSpy.mockRestore();
    });

    it("calls next when valid token is provided", async () => {
      // Generate a valid token first
      const { AuthTokenProvider } = await import(
        "../../main/ts/infrastructure/adapter/middleware/token"
      );
      const { UUIDEntityId } = await import(
        "munchies-commons/kotlin/commons-modules"
      );
      const provider = new AuthTokenProvider();
      const genResult = provider.generateToken(
        new UUIDEntityId("123e4567-e89b-12d3-a456-426614174000"),
        AuthRole.CUSTOMER,
      );
      const { GenerateTokenSuccess } = await import(
        "munchies-commons/kotlin/commons-modules"
      );
      expect(genResult).toBeInstanceOf(GenerateTokenSuccess);
      const token = (genResult as any).token;

      const req = mockReq({ cookies: { authToken: token } });
      const res = mockRes();
      const next = vi.fn();
      const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});

      const middleware = requireAuth();
      await middleware(req, res, next);

      expect(next).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });

    it("returns 401 when token is invalid/malformed", async () => {
      const req = mockReq({ cookies: { authToken: "invalid.jwt.token" } });
      const res = mockRes();
      const next = vi.fn();
      const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});

      const middleware = requireAuth();
      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
      consoleSpy.mockRestore();
    });
  });

  describe("requireRole", () => {
    it("returns 401 when req.user is missing", async () => {
      const req = mockReq({ user: undefined });
      const res = mockRes();
      const next = vi.fn();

      const middleware = requireRole(AuthRole.CUSTOMER);
      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(next).not.toHaveBeenCalled();
    });

    it("calls next when user has sufficient role (MANAGER >= CUSTOMER)", async () => {
      const req = mockReq({ user: { id: "user-1", role: AuthRole.MANAGER } });
      const res = mockRes();
      const next = vi.fn();

      const middleware = requireRole(AuthRole.CUSTOMER);
      await middleware(req, res, next);

      expect(next).toHaveBeenCalled();
    });

    it("calls next when user has exact required role (CUSTOMER = CUSTOMER)", async () => {
      const req = mockReq({ user: { id: "user-1", role: AuthRole.CUSTOMER } });
      const res = mockRes();
      const next = vi.fn();

      const middleware = requireRole(AuthRole.CUSTOMER);
      await middleware(req, res, next);

      expect(next).toHaveBeenCalled();
    });

    it("returns 403 when user role is insufficient (CUSTOMER < MANAGER)", async () => {
      const req = mockReq({ user: { id: "user-1", role: AuthRole.CUSTOMER } });
      const res = mockRes();
      const next = vi.fn();

      const middleware = requireRole(AuthRole.MANAGER);
      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(403);
      expect(next).not.toHaveBeenCalled();
    });
  });



  describe("clearCookie", () => {
    it("clears authToken cookie on response", () => {
      const res = mockRes();
      const result = clearCookie(res);
      expect(result).toBe(res);
      expect(res.clearCookie).toHaveBeenCalledWith(
        "authToken",
        expect.objectContaining({ httpOnly: true }),
      );
    });
  });
});
