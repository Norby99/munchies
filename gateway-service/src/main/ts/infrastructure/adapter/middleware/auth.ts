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
} from "munchies-commons/kotlin/commons-modules";

interface TokenClaims {
  sub: string;
  [ID_CLAIM]: string;
  [ROLE_CLAIM]: string;
  [EXPIRATION_CLAIM]: string;
  exp: number;
  iat?: number;
}
import jwt from "jsonwebtoken";

export class AuthTokenDecoder extends TokenDecoder {
  private readonly secret: string | undefined = process.env.JWT_SECRET;

  validateAndDecodeToken(token: string): DecodedTokenResult {
    if (!this.secret) return DecodedTokenFailure;
    try {
      const decoded = jwt.verify(token, this.secret, {
        algorithms: ["HS256"],
      }) as TokenClaims;

      for (const claim of [ID_CLAIM, ROLE_CLAIM, EXPIRATION_CLAIM]) {
        if (!(claim in decoded)) {
          return DecodedTokenFailure;
        }
      }
      console.log("", decoded);
      return DecodedTokenSuccess;
    } catch (e: any) {
      return DecodedTokenFailure;
    }
  }

}