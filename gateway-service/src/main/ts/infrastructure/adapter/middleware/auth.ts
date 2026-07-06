import { AuthTokenDecoder, AuthTokenProvider } from "./token";
import {
  AuthRole,
  DecodedTokenFailure,
  DecodedTokenSuccess,
  GenerateTokenSuccess,
  GenerateTokenFailure,
  UUIDEntityId,
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

export interface AuthedRequest extends ExpressRequest {
  user?: AuthInfo;
}
export function requireAuth<Response extends { toJson(): string}>(
    invalidRoleResponse: (msg: string, code: number) => Response,
): ExpressRequestHandler {
  return async (
    req: AuthedRequest,
    res: ExpressResponse,
    next: NextFunction,
  ) => {
    const missingToken = 500;
    console.log("cookies", req.cookies["authToken"]);
    if (req.cookies["authToken"] === undefined) {
      res.status(missingToken).type("json").send(invalidRoleResponse("missing token", missingToken).toJson());
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
            invalidRoleResponse("invalid token: " + (tokenRes as DecodedTokenFailure).toString(), missingToken).toJson(),
          );
        return;
      }
    }

  };
}
export function requireRole<
  Response extends {
    toJson(): string;
  },
>(
  requiredRole: AuthRole,
  invalidRoleResponse: (msg: string, code: number) => Response,
): ExpressRequestHandler {
  return async (
    req: AuthedRequest,
    res: ExpressResponse,
    next: NextFunction,
  ) => {
    const unauthorizedCode = 401;
    if (!req.user) {
      res
        .status(unauthorizedCode)
        .type("json")
        .send(invalidRoleResponse("Missing role", unauthorizedCode).toJson());
      return;
    }
    if (req.user?.role.visibility >= requiredRole.visibility) next();
    else
      res
        .status(unauthorizedCode)
        .type("json")
        .send(invalidRoleResponse("Invalid role", unauthorizedCode).toJson());
    return;
  };
}

export function injectCookie<Response extends { toJson(): string }>(
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
      maxAge: 1000 * 60 * 60, // 1 Hour
      path: "/",
    });
    return res;
  }
}
