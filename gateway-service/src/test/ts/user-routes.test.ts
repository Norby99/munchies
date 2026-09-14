import "./setup-env";
import "../../main/ts/infrastructure/adapter/middleware/routes/routes";
import { describe, it, expect, vi, afterEach } from "vitest";
import { userRoutes } from "../../main/ts/infrastructure/adapter/middleware/routes/user/user.routes";
import { GetUserRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/get-user.route";
import { DeleteUserRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/delete-user.route";
import { LoginUserRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/login-user.route";
import { LogoutUserRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/logout-user.route";
import { RegisterUserRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/register-user.route";
import { UpdateUserInfoRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/update-user-info.route";
import { UpdateUserPasswordRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/update-user-password.route";
import { VerifyEmailRoute } from "../../main/ts/infrastructure/adapter/middleware/routes/user/verify-email.route";
import { ErrorResponse, AuthRole } from "munchies-commons/kotlin/commons-modules";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import * as auth from "../../main/ts/infrastructure/adapter/middleware/auth";

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
    cookie: vi.fn(function () {
      return res;
    }),
    clearCookie: vi.fn(function () {
      return res;
    }),
  };
  return res;
}

const dummySuccess = {
  code: 200,
  toJson: () => JSON.stringify({ result: "ok", code: 200 }),
} as any;

describe("User Routes", () => {
  const originalEnv = process.env.USER_SERVICE_URL;

  afterEach(() => {
    if (originalEnv !== undefined) {
      process.env.USER_SERVICE_URL = originalEnv;
    } else {
      delete process.env.USER_SERVICE_URL;
    }
    vi.restoreAllMocks();
  });

  describe("userRoutes array", () => {
    it("exports all 8 user routes", () => {
      expect(userRoutes.length).toBe(8);
    });
  });

  // ---- GetUserRoute ----
  describe("GetUserRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new GetUserRoute();
      const res = await route.getUser("user-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles getUser with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetUserRoute();
      const res = await route.getUser("user-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new GetUserRoute();

      const resSuccess = await route.forward({
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ user: { id: "user-1", role: AuthRole.CUSTOMER } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- DeleteUserRoute ----
  describe("DeleteUserRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new DeleteUserRoute();
      const res = await route.deleteUser("user-1");
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles deleteUser with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteUserRoute();
      const res = await route.deleteUser("user-1");
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new DeleteUserRoute();

      const resSuccess = await route.forward({
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ user: { id: "user-1", role: AuthRole.CUSTOMER } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- LoginUserRoute ----
  describe("LoginUserRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new LoginUserRoute();
      const res = await route.loginUser({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles loginUser with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new LoginUserRoute();
      const res = await route.loginUser({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward success and catch", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new LoginUserRoute();
      const mockParsedReq = { toJson: () => "{}" };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({ body: "{}" } as any);
      expect(resSuccess).toBe(dummySuccess);
    });

    it("handles respond when result is not LoginUserResult (ErrorResponse path)", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      const errorResponse = new ErrorResponse("Unauthorized", 401);
      vi.spyOn(internalClient, "request").mockResolvedValue(errorResponse);
      const route = new LoginUserRoute();
      const mockParsedReq = { toJson: () => "{}" };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const res = mockResponse();
      await route.respond({ body: "{}" } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(401);
    });
  });

  // ---- LogoutUserRoute ----
  describe("LogoutUserRoute", () => {
    it("handles logoutUser", async () => {
      const route = new LogoutUserRoute();
      const res = await route.logoutUser({} as any);
      expect(res).toBeDefined();
      expect((res as any).code).toBe(200);
    });

    it("handles forward success and catch", async () => {
      const route = new LogoutUserRoute();

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect((resSuccess as any).code).toBe(200);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);
    });

    it("handles respond and clears cookie", async () => {
      const route = new LogoutUserRoute();
      vi.spyOn(auth, "clearCookie").mockImplementation((r) => r);

      const res = mockResponse();
      await route.respond(
        { body: "{}", user: { id: "user-1", role: AuthRole.CUSTOMER } } as any,
        res, () => {},
      );
      expect(res.status).toHaveBeenCalledWith(200);
    });

    it("handles respond when clearCookie throws", async () => {
      const route = new LogoutUserRoute();
      vi.spyOn(auth, "clearCookie").mockImplementation(() => {
        throw new Error("Cookie error");
      });

      const res = mockResponse();
      await route.respond(
        { body: "{}", user: { id: "user-1", role: AuthRole.CUSTOMER } } as any,
        res, () => {},
      );
      expect(res.status).toHaveBeenCalledWith(500);
    });
  });

  // ---- RegisterUserRoute ----
  describe("RegisterUserRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new RegisterUserRoute();
      const res = await route.registerUser({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles registerUser with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new RegisterUserRoute();
      const res = await route.registerUser({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });



    it("handles respond when result is not UserDTO (ErrorResponse path)", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      const errorResponse = new ErrorResponse("Conflict", 409);
      vi.spyOn(internalClient, "request").mockResolvedValue(errorResponse);
      const route = new RegisterUserRoute();
      const mockParsedReq = { toJson: () => "{}" };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const res = mockResponse();
      await route.respond({ body: "{}" } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(409);
    });
  });

  // ---- UpdateUserInfoRoute ----
  describe("UpdateUserInfoRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new UpdateUserInfoRoute();
      const res = await route.updateUserInfo({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateUserInfo with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateUserInfoRoute();
      const res = await route.updateUserInfo({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateUserInfoRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "user-1", role: AuthRole.CUSTOMER } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- UpdateUserPasswordRoute ----
  describe("UpdateUserPasswordRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new UpdateUserPasswordRoute();
      const res = await route.updateUserPassword({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles updateUserPassword with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateUserPasswordRoute();
      const res = await route.updateUserPassword({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new UpdateUserPasswordRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "user-1", role: AuthRole.CUSTOMER } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  // ---- VerifyEmailRoute ----
  describe("VerifyEmailRoute", () => {
    it("returns 500 when USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new VerifyEmailRoute();
      const res = await route.verifyEmail({ toJson: () => "{}" } as any);
      expect(res).toBeInstanceOf(ErrorResponse);
      expect((res as ErrorResponse).code).toBe(500);
    });

    it("handles verifyEmail with USER_SERVICE_URL", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new VerifyEmailRoute();
      const res = await route.verifyEmail({ toJson: () => "{}" } as any);
      expect(res).toBe(dummySuccess);
    });

    it("handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://user-service";
      vi.spyOn(internalClient, "request").mockResolvedValue(dummySuccess);
      const route = new VerifyEmailRoute();
      const mockParsedReq = { toJson: () => "{}", addId: vi.fn().mockReturnThis() };
      vi.spyOn(route as any, "parseRequest").mockReturnValue(mockParsedReq);

      const resSuccess = await route.forward({
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any);
      expect(resSuccess).toBe(dummySuccess);

      const resCatch = await route.forward({ body: "{}", user: undefined } as any);
      expect(resCatch).toBeInstanceOf(ErrorResponse);

      const res = mockResponse();
      await route.respond({ body: "{}", user: { id: "user-1", role: AuthRole.CUSTOMER } } as any, res, () => {});
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });
});
