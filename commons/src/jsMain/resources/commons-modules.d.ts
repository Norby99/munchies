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

export type HttpMethod =
  com.munchies.commons.infrastructure.adapter.HttpMethod;
export declare const HttpMethod: typeof com.munchies.commons.infrastructure.adapter.HttpMethod;

export type AuthRole =
  com.munchies.commons.domain.port.AuthRole;
export declare const AuthRole: typeof com.munchies.commons.domain.port.AuthRole;

export type TokenDecoder = com.munchies.commons.domain.port.TokenDecoder;
export declare const TokenDecoder: typeof com.munchies.commons.domain.port.TokenDecoder;

export type DecodedTokenResult = com.munchies.commons.domain.port.DecodedTokenResult;

export type DecodedTokenSuccess = com.munchies.commons.domain.port.DecodedTokenSuccess;
export declare const DecodedTokenSuccess: typeof com.munchies.commons.domain.port.DecodedTokenSuccess;

export type DecodedTokenFailure = typeof com.munchies.commons.domain.port.DecodedTokenFailure;
export declare const DecodedTokenFailure: typeof com.munchies.commons.domain.port.DecodedTokenFailure;

export declare const isAuthRoleGreaterThan: typeof com.munchies.commons.domain.port.isAuthRoleGreaterThan;

export type API = com.munchies.commons.infrastructure.adapter.API
export declare const API: typeof com.munchies.commons.infrastructure.adapter.API;

export type JsonEncodable = com.munchies.commons.infrastructure.adapter.JsonEncodable;
export declare const JsonEncodable: typeof com.munchies.commons.infrastructure.adapter.JsonEncodable;

export type WebResponse = com.munchies.commons.infrastructure.adapter.WebResponse;
export declare const WebResponse: typeof com.munchies.commons.infrastructure.adapter.WebResponse;

export type ResponseResult = com.munchies.commons.infrastructure.adapter.ResponseResult;
export declare const ResponseResult: typeof com.munchies.commons.infrastructure.adapter.ResponseResult;

export type WebFailure = com.munchies.commons.infrastructure.adapter.WebFailure;
export declare const WebFailure: typeof com.munchies.commons.infrastructure.adapter.WebFailure;
