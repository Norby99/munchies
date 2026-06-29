// common-modules.d.ts
import type { com } from "./munchies-commons";

export type UUIDEntityId = com.munchies.commons.UUIDEntityId;
export declare const UUIDEntityId: typeof com.munchies.commons.UUIDEntityId;

export declare const newId = com.munchies.commons.UUIDEntityId.Companion.newId;

export type InputValidator = com.munchies.commons.domain.port.InputValidator;
export declare const InputValidator: typeof com.munchies.commons.domain.port.InputValidator;

export type InputValidatorResult = com.munchies.commons.domain.port.InputValidatorResult;

export type ValidInput = typeof com.munchies.commons.domain.port.ValidInput;
export declare const ValidInput: typeof com.munchies.commons.domain.port.ValidInput;

export type InvalidInput = com.munchies.commons.domain.port.InvalidInput;
export declare const InvalidInput: typeof com.munchies.commons.domain.port.InvalidInput;

export declare const JWT_SECRET_ENV_NAME: string;
export declare const ID_CLAIM: string;
export declare const ROLE_CLAIM: string;
export declare const EXPIRATION_CLAIM: string;
export declare const JWT_SECRET_ALGORITHM: string;

export type TokenProvider = com.munchies.commons.domain.port.TokenProvider;
export declare const TokenProvider: typeof com.munchies.commons.domain.port.TokenProvider;

export type GenerateTokenResult = com.munchies.commons.domain.port.GenerateTokenResult;

export type GenerateTokenSuccess = com.munchies.commons.domain.port.GenerateTokenSuccess;
export declare const GenerateTokenSuccess: typeof com.munchies.commons.domain.port.GenerateTokenSuccess;

export type GenerateTokenFailure = typeof com.munchies.commons.domain.port.GenerateTokenFailure;
export declare const GenerateTokenFailure: typeof com.munchies.commons.domain.port.GenerateTokenFailure;

export type ValidateTokenResult = com.munchies.commons.domain.port.ValidateTokenResult;

export type ValidateTokenSuccess = typeof com.munchies.commons.domain.port.ValidateTokenSuccess;
export declare const ValidateTokenSuccess: typeof com.munchies.commons.domain.port.ValidateTokenSuccess;

export type ValidateTokenFailure = typeof com.munchies.commons.domain.port.ValidateTokenFailure;
export declare const ValidateTokenFailure: typeof com.munchies.commons.domain.port.ValidateTokenFailure;

export type RefreshTokenResult = com.munchies.commons.domain.port.RefreshTokenResult;

export type RefreshTokenSuccess = com.munchies.commons.domain.port.RefreshTokenSuccess;
export declare const RefreshTokenSuccess: typeof com.munchies.commons.domain.port.RefreshTokenSuccess;

export type RefreshTokenFailure = typeof com.munchies.commons.domain.port.RefreshTokenFailure;
export declare const RefreshTokenFailure: typeof com.munchies.commons.domain.port.RefreshTokenFailure;

export type HttpMethod =
  com.munchies.commons.infrastructure.adapter.HttpMethod;
export declare const HttpMethod: typeof com.munchies.commons.infrastructure.adapter.HttpMethod;
