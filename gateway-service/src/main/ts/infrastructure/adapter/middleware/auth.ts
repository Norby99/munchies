import { com } from "munchies-commons";
import {
  TokenProvider,
  UUIDEntityId,
  GenerateTokenResult,
  GenerateTokenSuccess,
  GenerateTokenFailure,
  ValidateTokenResult,
  ValidateTokenSuccess,
  ValidateTokenFailure,
  RefreshTokenResult,
  RefreshTokenSuccess,
  RefreshTokenFailure,
  DecodedTokenResult,
  TokenDecoder,
  DecodedTokenSuccess,
  DecodedTokenFailure,
  ID_CLAIM,
  EXPIRATION_CLAIM,
  ROLE_CLAIM,
  JWT_SECRET_ALGORITHM,
  JWT_SECRET_ENV_NAME,
  AuthRole,
} from "munchies-commons/kotlin/commons-modules";

interface TokenClaims {
  sub: string;
  [ID_CLAIM]: string;
  [ROLE_CLAIM]: string;
  exp: number;
  iat?: number;
}
import jwt from "jsonwebtoken";
export class AuthTokenProvider extends TokenProvider {
  constructor(secret: string) {
    super();
    this.secret = secret;
  }
  private readonly secret: string;
  generateToken(id: UUIDEntityId, role: AuthRole): GenerateTokenResult {
    const nowSeconds = Math.floor(Date.now() / 1000);
    const expSeconds = nowSeconds + 60 * 60 * 24 * 7;

    const payload: TokenClaims = {
      sub: id.value,
      [ID_CLAIM]: id.value,
      [ROLE_CLAIM]: role.name,
      exp: expSeconds,
      iat: nowSeconds,
    };

    return new GenerateTokenSuccess(jwt.sign(payload, this.secret));
  }
  refreshToken(expiredToken: string): RefreshTokenResult {
    return new RefreshTokenSuccess("");
  }
  validateToken(token: string): ValidateTokenResult {
    return ValidateTokenSuccess;
  }
  revokeToken(token: string): void {}
}

export class AuthTokenDecoder extends TokenDecoder {
  private readonly secret: string | undefined = process.env.JWT_SECRET;

  validateAndDecodeToken(token: string): DecodedTokenResult {
    if (!this.secret) return new DecodedTokenFailure("Absent secret");
    try {
      const decoded = jwt.verify(token, this.secret) as TokenClaims;

      for (const claim of [ID_CLAIM, ROLE_CLAIM]) {
        if (!(claim in decoded)) {
          return new DecodedTokenFailure("Not all claims are present");
        }
      }
      console.log("decoded token: ", decoded);
        return new DecodedTokenSuccess(
          decoded[ID_CLAIM]!!?.toString(),
          decoded[ROLE_CLAIM]!!.toString() == AuthRole.CUSTOMER.name
            ? AuthRole.CUSTOMER
            : AuthRole.MANAGER,
        );
    } catch (e: any) {
      return new DecodedTokenFailure("error: " + e);
    }
  }
}
const jwt_secret = process.env["JWT_SECRET"];
if (!jwt_secret) throw new Error("Cannot be missing JWT SECRET");
const provider = new AuthTokenProvider(jwt_secret);
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
export function requireAuth(): ExpressRequestHandler {
  return async (
    req: AuthedRequest,
    res: ExpressResponse,
    next: NextFunction,
  ) => {
    const missingToken = 500
    console.log("cookies", req.cookies["authToken"]);
    if (req.cookies["authToken"] === undefined) {
      res.status(missingToken).type("json").send({ error: "missing token"})
      return;
    }
    else {  
      const tokenRes = decoder.validateAndDecodeToken(req.cookies.authToken);
      
      if (tokenRes instanceof DecodedTokenSuccess) {
        req.user = { id: tokenRes.id, role: tokenRes.role } 
        next();
      }
      else {
        console.log("Token decode was not successful")
        res.status(missingToken).type("json").send({ error: "invalid token: " + (tokenRes as DecodedTokenFailure).toString() })
        return;  
      }
    }
  };
}
import { isAuthRoleGreaterThan } from "munchies-commons/kotlin/commons-modules";
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
      maxAge: 1000 * 60 * 60 * 24 * 7,
      path: "/",
    });
    return res;
  }
}
