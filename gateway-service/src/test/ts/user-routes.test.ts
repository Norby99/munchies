import "./setup-env";
import { describe, it, expect, vi, afterEach } from "vitest";

process.env.JWT_SECRET = "secret-for-user-routes-testing";

await import("../../main/ts/infrastructure/adapter/middleware/routes/routes");
const { userRoutes } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/user.routes");
const { GetUserRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/get-user.route");
const { DeleteUserRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/delete-user.route");
const { VerifyEmailRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/verify-email.route");
const { UpdateUserInfoRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/update-user-info.route");
const { UpdateUserPasswordRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/update-user-password.route");
const { LoginUserRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/login-user.route");
const { RegisterUserRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/register-user.route");
const { LogoutUserRoute } = await import("../../main/ts/infrastructure/adapter/middleware/routes/user/logout-user.route");
import { AuthRole, ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import * as internalClient from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import * as authModule from "../../main/ts/infrastructure/adapter/middleware/auth";
import {
  loginUserResponseFromJson,
  registerUserResponseFromJson,
  getUserResponseFromJson,
  deleteUserResponseFromJson,
  updateUserInfoResponseFromJson,
  updateUserPasswordResponseFromJson,
  verifyEmailResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";

function mockResponse() {
  const res: any = {
    statusCode: 200,
    headers: {},
    body: null,
    status: vi.fn(function (code: number) {
      res.statusCode = code;
      return res;
    }),
    type: vi.fn(function () {
      return res;
    }),
    send: vi.fn(function (body: any) {
      res.body = body;
      return res;
    }),
    cookie: vi.fn(),
    clearCookie: vi.fn(),
  };
  return res;
}

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

  describe("GetUserRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is not configured", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new GetUserRoute();
      const result = await route.getUser("user-1");
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("calls request and returns response when USER_SERVICE_URL is set", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = getUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new GetUserRoute();
      const result = await route.getUser("user-1");
      expect(result).toBe(expectedResponse);
    });

    it("handles forward: success and error catch", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = getUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new GetUserRoute();

      // Success forward
      const authedReq = { user: { id: "user-1", role: AuthRole.CUSTOMER } } as any;
      const res1 = await route.forward(authedReq);
      expect(res1).toBe(expectedResponse);

      // Catch forward: user is missing
      const unauthedReq = {} as any;
      const res2 = await route.forward(unauthedReq);
      expect(res2).toBeInstanceOf(ErrorResponse);
      expect((res2 as ErrorResponse).code).toBe(500);
    });

    it("handles respond", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = getUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new GetUserRoute();
      const req = { user: { id: "user-1", role: AuthRole.CUSTOMER } } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(200);
      expect(res.send).toHaveBeenCalledWith(expectedResponse.toJson());
    });
  });

  describe("DeleteUserRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new DeleteUserRoute();
      const result = await route.deleteUser("user-1");
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("calls request and handles forward and respond", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = deleteUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new DeleteUserRoute();
      const result = await route.deleteUser("user-1");
      expect(result).toBe(expectedResponse);

      // Forward catch
      const failForward = await route.forward({} as any);
      expect(failForward).toBeInstanceOf(ErrorResponse);

      // Respond
      const req = { user: { id: "user-1", role: AuthRole.CUSTOMER } } as any;
      const res = mockResponse();
      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("VerifyEmailRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new VerifyEmailRoute();
      const mockReq = { toJson: () => "{}" } as any;
      const result = await route.verifyEmail(mockReq);
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("handles forward success, catch, and respond", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = verifyEmailResponseFromJson(
        JSON.stringify({ result: "verified", code: 200 })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new VerifyEmailRoute();

      // Catch forward: invalid body / missing user
      const failForward = await route.forward({ body: "invalid-json" } as any);
      expect(failForward).toBeInstanceOf(ErrorResponse);

      // Success forward
      const validReq = {
        body: JSON.stringify({ id: "user-1", otk: "123456" }),
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any;
      const resForward = await route.forward(validReq);
      expect(resForward).toBe(expectedResponse);

      // Respond
      const res = mockResponse();
      await route.respond(validReq, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("UpdateUserInfoRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new UpdateUserInfoRoute();
      const mockReq = { toJson: () => "{}" } as any;
      const result = await route.updateUserInfo(mockReq);
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("handles forward success, catch, and respond", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = updateUserInfoResponseFromJson(
        JSON.stringify({
          result: "User updated successfully",
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new UpdateUserInfoRoute();

      // Catch forward
      const fail = await route.forward({ body: "invalid" } as any);
      expect(fail).toBeInstanceOf(ErrorResponse);

      // Success forward
      const validReq = {
        body: JSON.stringify({
          user: { id: "user-1", username: "new-username", email: "new@m.com", role: "CUSTOMER", isEmailVerified: true },
        }),
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any;
      const success = await route.forward(validReq);
      expect(success).toBe(expectedResponse);

      // Respond
      const res = mockResponse();
      await route.respond(validReq, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("UpdateUserPasswordRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new UpdateUserPasswordRoute();
      const mockReq = { toJson: () => "{}" } as any;
      const result = await route.updateUserPassword(mockReq);
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("handles forward success, catch, and respond", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expectedResponse = updateUserPasswordResponseFromJson(
        JSON.stringify({ result: "password updated", code: 200 })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expectedResponse);

      const route = new UpdateUserPasswordRoute();

      // Catch forward
      const fail = await route.forward({ body: "invalid" } as any);
      expect(fail).toBeInstanceOf(ErrorResponse);

      // Success forward
      const validReq = {
        body: JSON.stringify({ id: "user-1", oldHashedPassword: "old", newPassword: "new" }),
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any;
      const success = await route.forward(validReq);
      expect(success).toBe(expectedResponse);

      // Respond
      const res = mockResponse();
      await route.respond(validReq, res);
      expect(res.status).toHaveBeenCalledWith(200);
    });
  });

  describe("LoginUserRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new LoginUserRoute();
      const mockReq = { toJson: () => "{}" } as any;
      const result = await route.loginUser(mockReq);
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("handles forward failure and success", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const route = new LoginUserRoute();

      // Catch forward
      const fail = await route.forward({ body: "invalid" } as any);
      expect(fail).toBeInstanceOf(ErrorResponse);

      // Success forward
      const expected = loginUserResponseFromJson(
        JSON.stringify({ result: { id: "user-1", role: "CUSTOMER" }, code: 200 })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);
      const success = await route.forward({
        body: JSON.stringify({ email: "u@m.com", username: "u1", password: "p" }),
      } as any);
      expect(success).toBe(expected);
    });

    it("handles respond with LoginUserResult and injects cookie", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const uniqueId = "login-user-" + Math.random();
      const expected = loginUserResponseFromJson(
        JSON.stringify({ result: { id: uniqueId, role: "CUSTOMER" }, code: 200 })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);

      const route = new LoginUserRoute();
      const req = { body: JSON.stringify({ email: "u@m.com", username: "u1", password: "p" }) } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.cookie).toHaveBeenCalled();
      expect(res.status).toHaveBeenCalledWith(200);
    });

    it("handles respond when injectCookie throws an error", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expected = loginUserResponseFromJson(
        JSON.stringify({ result: { id: "user-1", role: "CUSTOMER" }, code: 200 })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);
      vi.spyOn(authModule, "injectCookie").mockImplementation(() => {
        throw new Error("Cookie injection failed");
      });

      const route = new LoginUserRoute();
      const req = { body: JSON.stringify({ email: "u@m.com", username: "u1", password: "p" }) } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(500);
    });

    it("handles respond when forward returns ErrorResponse", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const errorResp = new ErrorResponse("Bad credentials", 401);
      vi.spyOn(internalClient, "request").mockResolvedValue(errorResp);

      const route = new LoginUserRoute();
      const req = { body: JSON.stringify({ email: "u@m.com", username: "u1", password: "p" }) } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(401);
      expect(res.cookie).not.toHaveBeenCalled();
    });
  });

  describe("RegisterUserRoute", () => {
    it("returns 500 ErrorResponse if USER_SERVICE_URL is missing", async () => {
      delete process.env.USER_SERVICE_URL;
      const route = new RegisterUserRoute();
      const mockReq = { toJson: () => "{}" } as any;
      const result = await route.registerUser(mockReq);
      expect(result).toBeInstanceOf(ErrorResponse);
      expect((result as ErrorResponse).code).toBe(500);
    });

    it("handles forward failure and success", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const route = new RegisterUserRoute();

      // Catch forward
      const fail = await route.forward({ body: "invalid" } as any);
      expect(fail).toBeInstanceOf(ErrorResponse);

      // Success forward
      const expected = registerUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);
      const success = await route.forward({
        body: JSON.stringify({ username: "u1", email: "u@m.com", role: "CUSTOMER", hashedPassword: "hp", saltValue: "salt" }),
      } as any);
      expect(success).toBe(expected);
    });

    it("handles respond with UserDTO and injects cookie", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const uniqueId = "register-user-" + Math.random();
      const expected = registerUserResponseFromJson(
        JSON.stringify({
          result: { id: uniqueId, username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);

      const route = new RegisterUserRoute();
      const req = {
        body: JSON.stringify({ username: "u1", email: "u@m.com", role: "CUSTOMER", hashedPassword: "hp", saltValue: "salt" }),
      } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.cookie).toHaveBeenCalled();
      expect(res.status).toHaveBeenCalledWith(200);
    });

    it("handles respond when injectCookie throws", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const expected = registerUserResponseFromJson(
        JSON.stringify({
          result: { id: "user-1", username: "u1", email: "u@m.com", role: "CUSTOMER", isEmailVerified: true },
          code: 200,
        })
      );
      vi.spyOn(internalClient, "request").mockResolvedValue(expected);
      vi.spyOn(authModule, "injectCookie").mockImplementation(() => {
        throw new Error("Cookie error");
      });

      const route = new RegisterUserRoute();
      const req = {
        body: JSON.stringify({ username: "u1", email: "u@m.com", role: "CUSTOMER", hashedPassword: "hp", saltValue: "salt" }),
      } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(500);
    });

    it("handles respond when forward returns ErrorResponse", async () => {
      process.env.USER_SERVICE_URL = "http://userservice";
      const errResp = new ErrorResponse("Conflict", 409);
      vi.spyOn(internalClient, "request").mockResolvedValue(errResp);

      const route = new RegisterUserRoute();
      const req = {
        body: JSON.stringify({ username: "u1", email: "u@m.com", role: "CUSTOMER", hashedPassword: "hp", saltValue: "salt" }),
      } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(409);
      expect(res.cookie).not.toHaveBeenCalled();
    });
  });

  describe("LogoutUserRoute", () => {
    it("logs out user, forwards request, and responds with cookie cleared", async () => {
      const route = new LogoutUserRoute();
      const req = {
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any;

      const resForward = await route.forward(req);
      expect(resForward.code).toBe(200);

      // Respond success
      const res = mockResponse();
      await route.respond(req, res);
      expect(res.clearCookie).toHaveBeenCalled();
      expect(res.status).toHaveBeenCalledWith(200);
    });

    it("handles forward catch when req.user is missing", async () => {
      const route = new LogoutUserRoute();
      const fail = await route.forward({ body: "{}" } as any);
      expect(fail).toBeInstanceOf(ErrorResponse);
      expect((fail as ErrorResponse).code).toBe(500);
    });

    it("handles respond catch when clearCookie throws", async () => {
      const route = new LogoutUserRoute();
      vi.spyOn(authModule, "clearCookie").mockImplementation(() => {
        throw new Error("Clear cookie error");
      });

      const req = {
        body: "{}",
        user: { id: "user-1", role: AuthRole.CUSTOMER },
      } as any;
      const res = mockResponse();

      await route.respond(req, res);
      expect(res.status).toHaveBeenCalledWith(500);
    });
  });
});
