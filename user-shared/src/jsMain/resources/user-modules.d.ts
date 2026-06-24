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

