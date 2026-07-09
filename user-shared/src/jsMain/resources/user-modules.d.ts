// user-modules.d.ts
import type { com } from "./munchies-user-shared";

export type UserDTO =
  com.munchies.user.infrastructure.adapter.dto.UserDTO;
export declare const UserDTO: typeof
  com.munchies.user.infrastructure.adapter.dto.UserDTO;

export declare const UserServiceConfig = com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig

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
export declare const registerUserRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.registerUserRequestFromJson;

export type RegisterUserSuccess = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserSuccess;
export declare const RegisterUserSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserSuccess;

export type RegisterUserFailure = com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserFailure;
export declare const RegisterUserFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserFailure;



export type LoginUserAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsLoginUserAPI;
export declare const LoginUserAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsLoginUserAPI;

export type LoginUserRequest = com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest;
export declare const LoginUserRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest;

export type LoginUserResponse = com.munchies.user.infrastructure.adapter.outbound.response.LoginUserResponse;
export declare const LoginUserResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.LoginUserResponse;

export type LoginUserResult = com.munchies.user.infrastructure.adapter.outbound.response.LoginUserResult;
export declare const LoginUserResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.LoginUserResult;

export declare const loginUserResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.loginUserResponseFromJson;
export declare const loginUserRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.loginUserRequestFromJson;

export type LoginUserSuccess = com.munchies.user.infrastructure.adapter.outbound.response.LoginUserSuccess;
export declare const LoginUserSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.LoginUserSuccess;

export type LoginUserFailure = com.munchies.user.infrastructure.adapter.outbound.response.LoginUserFailure;
export declare const LoginUserFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.LoginUserFailure;



export type UpdateUserPasswordAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsUpdateUserPasswordAPI;
export declare const UpdateUserPasswordAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsUpdateUserPasswordAPI;

export type UpdateUserPasswordRequest = com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest;
export declare const UpdateUserPasswordRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest;

export type UpdateUserPasswordResponse = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordResponse;
export declare const UpdateUserPasswordResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordResponse;

export type UpdateUserPasswordResult = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordResult;
export declare const UpdateUserPasswordResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordResult;

export declare const updateUserPasswordResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.updateUserPasswordResponseFromJson;
export declare const updateUserPasswordRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.updateUserPasswordRequestFromJson;

export type UpdateUserPasswordSuccess = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordSuccess;
export declare const UpdateUserPasswordSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordSuccess;

export type UpdateUserPasswordFailure = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordFailure;
export declare const UpdateUserPasswordFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserPasswordFailure;



export type UpdateUserInfoAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsUpdateUserInfoAPI;
export declare const UpdateUserInfoAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsUpdateUserInfoAPI;

export type UpdateUserInfoRequest = com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest;
export declare const UpdateUserInfoRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest;

export type UpdateUserInfoResponse = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoResponse;
export declare const UpdateUserInfoResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoResponse;

export type UpdateUserInfoResult = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoResult;
export declare const UpdateUserInfoResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoResult;

export declare const updateUserInfoResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.updateUserInfoResponseFromJson;
export declare const updateUserInfoRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.updateUserInfoRequestFromJson;

export type UpdateUserInfoSuccess = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoSuccess;
export declare const UpdateUserInfoSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoSuccess;

export type UpdateUserInfoFailure = com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoFailure;
export declare const UpdateUserInfoFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.UpdateUserInfoFailure;



export type DeleteUserAPI =
  com.munchies.user.infrastructure.adapter.inbound.JsDeleteUserAPI;
export declare const DeleteUserAPI: typeof
  com.munchies.user.infrastructure.adapter.inbound.JsDeleteUserAPI;

export type DeleteUserRequest = com.munchies.user.infrastructure.adapter.inbound.request.DeleteUserRequest;
export declare const DeleteUserRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.DeleteUserRequest;

export type DeleteUserResponse = com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserResponse;
export declare const DeleteUserResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserResponse;

export type DeleteUserResult = com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserResult;
export declare const DeleteUserResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserResult;

export declare const deleteUserResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.deleteUserResponseFromJson;
export declare const deleteUserRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.deleteUserRequestFromJson;

export type DeleteUserSuccess = com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserSuccess;
export declare const DeleteUserSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserSuccess;

export type DeleteUserFailure = com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserFailure;
export declare const DeleteUserFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.DeleteUserFailure;

export type EmailVerificationAPI =
    com.munchies.user.infrastructure.adapter.inbound.JsEmailVerificationAPI;
export declare const EmailVerificationAPI: typeof
    com.munchies.user.infrastructure.adapter.inbound.JsEmailVerificationAPI;

export type VerifyEmailRequest = com.munchies.user.infrastructure.adapter.inbound.request.VerifyEmailRequest;
export declare const VerifyEmailRequest: typeof com.munchies.user.infrastructure.adapter.inbound.request.VerifyEmailRequest;

export type VerifyEmailResponse = com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailResponse;
export declare const VerifyEmailResponse: typeof com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailResponse;

export type VerifyEmailResult = com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailResult;
export declare const VerifyEmailResult: typeof com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailResult;

export declare const verifyEmailResponseFromJson = com.munchies.user.infrastructure.adapter.outbound.response.verifyEmailResponseFromJson;
export declare const verifyEmailRequestFromJson = com.munchies.user.infrastructure.adapter.inbound.request.verifyEmailRequestFromJson;

export type VerifyEmailSuccess = com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailSuccess;
export declare const VerifyEmailSuccess: typeof com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailSuccess;

export type VerifyEmailFailure = com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailFailure;
export declare const VerifyEmailFailure: typeof com.munchies.user.infrastructure.adapter.outbound.response.VerifyEmailFailure;

