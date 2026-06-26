// user-modules.d.ts
import type { com } from "./munchies-user-shared";

export type UserDTO =
  com.munchies.user.infrastructure.adapter.dto.UserDTO;
export declare const UserDTO: typeof
  com.munchies.user.infrastructure.adapter.dto.UserDTO;

export type GetUserAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsGetUserAPI;
export declare const GetUserAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsGetUserAPI;

export type GetUserResponse = com.munchies.user.infrastructure.adapter.outbound.response.GetUserResponse;
export declare const GetUserResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.GetUserResponse;

export type GetUserResult = com.munchies.user.infrastructure.adapter.outbound.response.GetUserResult;
export declare const GetUserResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.GetUserResult;

export declare const getUserResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.getUserResponseFromJson;

export type GetUserSuccess = com.munchies.user.infrastructure.adapter.outbound.response.GetUserSuccess;
export declare const GetUserSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.GetUserSuccess;

export type GetUserFailure = com.munchies.user.infrastructure.adapter.outbound.response.GetUserFailure;
export declare const GetUserFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.GetUserFailure;

export type RegisterUserAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsRegisterUserAPI;
export declare const RegisterUserAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsRegisterUserAPI;

export type RegisterUserRequest = com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest;
export declare const RegisterUserRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest;

export type RegisterUserResponse = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserResponse;
export declare const RegisterUserResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserResponse;

export type RegisterUserResult = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserResult;
export declare const RegisterUserResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserResult;

export declare const registerUserResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.registerUserResponseFromJson;

export type RegisterUserSuccess = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserSuccess;
export declare const RegisterUserSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserSuccess;

export type RegisterUserFailure = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserFailure;
export declare const RegisterUserFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserFailure;
