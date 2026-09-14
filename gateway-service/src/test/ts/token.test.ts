import "./setup-env";
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import "../../main/ts/infrastructure/adapter/middleware/auth";
import { getTokenRepository } from "../../main/ts/infrastructure/adapter/outbound/token-repository";
import { AuthTokenProvider, AuthTokenDecoder } from "../../main/ts/infrastructure/adapter/middleware/token";
import {
  AuthRole,
  UUIDEntityId,
  GenerateTokenSuccess,
  GenerateTokenFailure,
  DecodedTokenSuccess,
  DecodedTokenFailure,
  ID_CLAIM,
  ROLE_CLAIM,
} from "munchies-commons/kotlin/commons-modules";
import jwt from "jsonwebtoken";

describe("TokenRepository", () => {
  it("should add, check isRevoked, and revoke tokens", () => {
    const repo = getTokenRepository();
    const tokenId = "test-token-" + Date.now();

    expect(repo.isRevoked(tokenId)).toBe(false);

    repo.add(tokenId);
    expect(repo.isRevoked(tokenId)).toBe(true);

    repo.revoke(tokenId);
    expect(repo.isRevoked(tokenId)).toBe(false);
  });
});

describe("AuthTokenProvider & AuthTokenDecoder", () => {
  const originalSecret = process.env.JWT_SECRET;
  const testSecret = "my-test-secret-key-12345";

  beforeEach(() => {
    process.env.JWT_SECRET = testSecret;
  });

  afterEach(() => {
    if (originalSecret !== undefined) {
      process.env.JWT_SECRET = originalSecret;
    } else {
      delete process.env.JWT_SECRET;
    }
  });

  it("fails to generate token when secret is missing", () => {
    delete process.env.JWT_SECRET;
    const provider = new AuthTokenProvider();
    const result = provider.generateToken(new UUIDEntityId("123e4567-e89b-12d3-a456-426614174000"), AuthRole.CUSTOMER);
    expect(result).toBeInstanceOf(GenerateTokenFailure);
  });

  it("successfully generates and validates token", () => {
    const provider = new AuthTokenProvider();
    const decoder = new AuthTokenDecoder();
    const userId = "123e4567-e89b-12d3-a456-426614174000";

    const genResult = provider.generateToken(new UUIDEntityId(userId), AuthRole.CUSTOMER);
    expect(genResult).toBeInstanceOf(GenerateTokenSuccess);
    const token = (genResult as GenerateTokenSuccess).token;

    const decodeResult = decoder.validateAndDecodeToken(token);
    expect(decodeResult).toBeInstanceOf(DecodedTokenSuccess);
    const success = decodeResult as DecodedTokenSuccess;
    expect(success.id).toBe(userId);
    expect(success.role).toBe(AuthRole.CUSTOMER);
  });

  it("fails to generate token when token is already revoked/exists in repo", () => {
    const provider = new AuthTokenProvider();
    const userId = "123e4567-e89b-12d3-a456-426614174001";
    // First generation adds token to repository (isRevoked returns true)
    const gen1 = provider.generateToken(new UUIDEntityId(userId), AuthRole.CUSTOMER);
    expect(gen1).toBeInstanceOf(GenerateTokenSuccess);

    // Calling again with same id in same second produces identical token, which repo.isRevoked() says true
    const gen2 = provider.generateToken(new UUIDEntityId(userId), AuthRole.CUSTOMER);
    expect(gen2).toBeInstanceOf(GenerateTokenFailure);
  });

  it("revokes a token via revokeToken", () => {
    const provider = new AuthTokenProvider();
    const repo = getTokenRepository();
    const token = "some-revoked-token";
    repo.add(token);
    expect(repo.isRevoked(token)).toBe(true);

    provider.revokeToken(token);
    expect(repo.isRevoked(token)).toBe(false);
  });

  it("fails to decode when secret is missing", () => {
    delete process.env.JWT_SECRET;
    const decoder = new AuthTokenDecoder();
    const result = decoder.validateAndDecodeToken("dummy-token");
    expect(result).toBeInstanceOf(DecodedTokenFailure);
  });

  it("fails to decode when token is malformed or signature invalid", () => {
    const decoder = new AuthTokenDecoder();
    const result = decoder.validateAndDecodeToken("invalid.token.here");
    expect(result).toBeInstanceOf(DecodedTokenFailure);
  });

  it("fails to decode when required claims are missing", () => {
    const decoder = new AuthTokenDecoder();
    // sign a token without ID_CLAIM or ROLE_CLAIM
    const tokenWithoutClaims = jwt.sign({ sub: "user-1" }, testSecret);
    const result = decoder.validateAndDecodeToken(tokenWithoutClaims);
    expect(result).toBeInstanceOf(DecodedTokenFailure);

    // token with only ID_CLAIM
    const tokenWithOnlyId = jwt.sign({ sub: "user-1", [ID_CLAIM]: "user-1" }, testSecret);
    const result2 = decoder.validateAndDecodeToken(tokenWithOnlyId);
    expect(result2).toBeInstanceOf(DecodedTokenFailure);
  });
});
