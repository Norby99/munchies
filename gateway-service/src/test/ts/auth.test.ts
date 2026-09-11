import "./setup-env";
import { describe, it, expect, vi } from "vitest";

process.env.JWT_SECRET = "my-auth-test-secret-key-12345";

const {
  parseAuthRoleString,
  requireAuth,
  requireRole,
  injectCookie,
  clearCookie,
} = await import("../../main/ts/infrastructure/adapter/middleware/auth");
type AuthedRequest = import("../../main/ts/infrastructure/adapter/middleware/auth").AuthedRequest;
import { AuthRole } from "munchies-commons/kotlin/commons-modules";

describe("auth.ts", () => {
  describe("parseAuthRoleString", () => {
    it("parses CUSTOMER correctly", () => {
      expect(parseAuthRoleString("CUSTOMER")).toBe(AuthRole.CUSTOMER);
      expect(parseAuthRoleString("customer")).toBe(AuthRole.CUSTOMER);
    });

    it("parses MANAGER correctly", () => {
      expect(parseAuthRoleString("MANAGER")).toBe(AuthRole.MANAGER);
      expect(parseAuthRoleString("manager")).toBe(AuthRole.MANAGER);
    });

    it("throws for unknown role", () => {
      expect(() => parseAuthRoleString("ADMIN")).toThrow("Unexpected Role");
    });
  });

  describe("requireAuth middleware", () => {
    it("returns 401 when cookies are missing or authToken is undefined", async () => {
      const middleware = requireAuth();
      const req = { cookies: {} } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.type).toHaveBeenCalledWith("json");
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
    });

    it("returns 401 when token is invalid", async () => {
      const middleware = requireAuth();
      const req = { cookies: { authToken: "invalid-token" } } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.type).toHaveBeenCalledWith("json");
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
    });

    it("sets req.user and calls next() when token is valid", async () => {
      const dummyRes = {
        cookie: vi.fn(),
      } as any;
      const injectResult = injectCookie(dummyRes, { id: "test-user-id", role: AuthRole.CUSTOMER });
      expect(injectResult).toBe(dummyRes);
      const validToken = dummyRes.cookie.mock.calls[0][1];

      const middleware = requireAuth();
      const req: AuthedRequest = { cookies: { authToken: validToken } } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);

      expect(next).toHaveBeenCalled();
      expect(req.user).toBeDefined();
      expect(req.user?.id).toBe("test-user-id");
      expect(req.user?.role).toBe(AuthRole.CUSTOMER);
      expect(res.status).not.toHaveBeenCalled();
    });
  });

  describe("requireRole middleware", () => {
    it("returns 401 when req.user is missing", async () => {
      const middleware = requireRole(AuthRole.CUSTOMER);
      const req = {} as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);

      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.type).toHaveBeenCalledWith("json");
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
    });

    it("calls next() when req.user has sufficient visibility", async () => {
      const middleware = requireRole(AuthRole.CUSTOMER);
      const req = {
        user: { id: "123", role: AuthRole.CUSTOMER },
      } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);
      expect(next).toHaveBeenCalled();
      expect(res.status).not.toHaveBeenCalled();
    });

    it("calls next() when MANAGER accesses CUSTOMER route (higher visibility)", async () => {
      const middleware = requireRole(AuthRole.CUSTOMER);
      const req = {
        user: { id: "123", role: AuthRole.MANAGER },
      } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);
      expect(next).toHaveBeenCalled();
      expect(res.status).not.toHaveBeenCalled();
    });

    it("returns 403 when req.user has insufficient visibility", async () => {
      const middleware = requireRole(AuthRole.MANAGER);
      const req = {
        user: { id: "123", role: AuthRole.CUSTOMER },
      } as any;
      const res = {
        status: vi.fn().mockReturnThis(),
        type: vi.fn().mockReturnThis(),
        send: vi.fn(),
      } as any;
      const next = vi.fn();

      await middleware(req, res, next);
      expect(res.status).toHaveBeenCalledWith(403);
      expect(res.type).toHaveBeenCalledWith("json");
      expect(res.send).toHaveBeenCalled();
      expect(next).not.toHaveBeenCalled();
    });
  });

  describe("injectCookie", () => {
    it("returns null when token generation fails (e.g. duplicate/revoked token)", async () => {
      const { getTokenRepository } = await import("../../main/ts/infrastructure/adapter/outbound/token-repository");
      const repo = getTokenRepository();
      const spy = vi.spyOn(repo, "isRevoked").mockReturnValueOnce(true);

      const res = { cookie: vi.fn() } as any;
      const result = injectCookie(res, { id: "any-id", role: AuthRole.CUSTOMER });
      expect(result).toBeNull();
      expect(res.cookie).not.toHaveBeenCalled();
      spy.mockRestore();
    });

    it("sets cookie on res and returns res when successful", () => {
      const res = { cookie: vi.fn() } as any;
      const result = injectCookie(res, { id: "123", role: AuthRole.CUSTOMER });
      expect(result).toBe(res);
      expect(res.cookie).toHaveBeenCalledWith(
        "authToken",
        expect.any(String),
        expect.objectContaining({
          httpOnly: true,
          secure: true,
          sameSite: "lax",
          path: "/",
        })
      );
    });
  });

  describe("clearCookie", () => {
    it("clears authToken cookie with expected options", () => {
      const res = { clearCookie: vi.fn() } as any;
      const result = clearCookie(res);
      expect(result).toBe(res);
      expect(res.clearCookie).toHaveBeenCalledWith(
        "authToken",
        expect.objectContaining({
          httpOnly: true,
          secure: true,
          sameSite: "lax",
          path: "/",
        })
      );
    });
  });
});
