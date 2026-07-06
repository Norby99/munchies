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
