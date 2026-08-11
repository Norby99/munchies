import { AuthTokenDecoder, AuthTokenProvider } from "./token";
import {
  AuthRole,
  DecodedTokenFailure,
  DecodedTokenSuccess,
  GenerateTokenSuccess,
  GenerateTokenFailure,
  UUIDEntityId,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
const provider = new AuthTokenProvider();
const decoder = new AuthTokenDecoder();

import {
  Request as ExpressRequest,
  NextFunction,
  RequestHandler as ExpressRequestHandler,
  Response as ExpressResponse,
} from "express";

export interface AuthInfo {
  id: string;
  role: AuthRole;
}

export function parseAuthRoleString(role: string): AuthRole {
  switch (role.toUpperCase()) {
    case AuthRole.CUSTOMER.name.toUpperCase():
      return AuthRole.CUSTOMER;
    case AuthRole.MANAGER.name.toUpperCase():
      return AuthRole.MANAGER;
    default:
      throw new Error("Unexpected Role");
  }
}

export interface AuthedRequest extends ExpressRequest {
  user?: AuthInfo;
}
export function requireAuth(): ExpressRequestHandler {
  return async (
    req: AuthedRequest,
    res: ExpressResponse,
    next: NextFunction,
  ) => {
    const missingToken = 401;
    console.log("cookies", req.cookies["authToken"]);
    if (req.cookies["authToken"] === undefined) {
      res
        .status(missingToken)
        .type("json")
        .send(new ErrorResponse("missing token", missingToken).toJson());
      return;
    } else {
      const tokenRes = decoder.validateAndDecodeToken(req.cookies.authToken);

      if (tokenRes instanceof DecodedTokenSuccess) {
        req.user = { id: tokenRes.id, role: tokenRes.role };
        next();
      } else {
        console.log("Token decode was not successful");
        res
          .status(missingToken)
          .type("json")
          .send(
            new ErrorResponse(
              "invalid token: " + (tokenRes as DecodedTokenFailure).toString(),
              missingToken,
            ).toJson(),
          );
        return;
      }
    }
  };
}
export function requireRole(requiredRole: AuthRole): ExpressRequestHandler {
  return async (
    req: AuthedRequest,
    res: ExpressResponse,
    next: NextFunction,
  ) => {
    const unauthorizedCode = 403;
    const missingRole = 401;
    if (!req.user) {
      res
        .status(missingRole)
        .type("json")
        .send(new ErrorResponse("Missing role", missingRole).toJson());
      return;
    }
    if (req.user?.role.visibility >= requiredRole.visibility) next();
    else
      res
        .status(unauthorizedCode)
        .type("json")
        .send(new ErrorResponse("Invalid role", unauthorizedCode).toJson());
    return;
  };
}

export function injectCookie(
  res: ExpressResponse,
  info: AuthInfo,
): ExpressResponse | null {
  const token = provider.generateToken(new UUIDEntityId(info.id), info.role);
  if (token instanceof GenerateTokenFailure) {
    return null;
  } else {
    const success = token as GenerateTokenSuccess;
    res.cookie("authToken", success.token, {
      httpOnly: true,
      secure: true,
      sameSite: "lax",
      maxAge: 1000 * 60 * 60 * 24 * 7, // 7 Days — aligned with JWT exp
      path: "/",
    });
    return res;
  }
}

export function clearCookie(res: ExpressResponse): ExpressResponse {
  res.clearCookie("authToken", {
    httpOnly: true,
    secure: true,
    sameSite: "lax",
    path: "/",
  });
  return res;
}
