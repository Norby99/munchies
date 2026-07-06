import {
  TokenProvider,
  UUIDEntityId,
  GenerateTokenResult,
  GenerateTokenSuccess,
  GenerateTokenFailure,
  TokenDecoder,
  DecodedTokenSuccess,
  DecodedTokenFailure,
  DecodedTokenResult,
  ID_CLAIM,
  ROLE_CLAIM,
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
import { getTokenRepository, TokenRepository } from "../outbound/token-repository";
export class AuthTokenProvider extends TokenProvider {
  private readonly secret: string | undefined = process.env.JWT_SECRET;
  private readonly repository: TokenRepository<string> = getTokenRepository();
  generateToken(id: UUIDEntityId, role: AuthRole): GenerateTokenResult {
    if(!this.secret) return new GenerateTokenFailure("Secret is missing")
    const nowSeconds = Math.floor(Date.now() / 1000);
    const expSeconds = nowSeconds + 60 * 60 * 24 * 7;

    const payload: TokenClaims = {
      sub: id.value,
      [ID_CLAIM]: id.value,
      [ROLE_CLAIM]: role.name,
      exp: expSeconds,
      iat: nowSeconds,
    };

    const token = jwt.sign(payload, this.secret)
    if(this.repository.isRevoked(token)) return new GenerateTokenFailure("Token is revoked") 
    this.repository.add(token);
    return new GenerateTokenSuccess(token);
  }
  
  revokeToken(token: string): void {
    this.repository.revoke(token);
  }
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
