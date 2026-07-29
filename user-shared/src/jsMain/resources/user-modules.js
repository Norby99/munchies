// user-modules.js
const generated = require("./munchies-user-shared.js");
const _user = generated.com.munchies.user;
const _inbound = _user.infrastructure.adapter.inbound;
const _outbound = _user.infrastructure.adapter.outbound;

module.exports = {
  UserDTO: _user.infrastructure.adapter.dto.UserDTO,
  UserServiceConfig: _user.infrastructure.adapter.inbound.web.config.UserServiceConfig,
  
  GetUserAPI: _inbound.JsGetUserAPI,
  GetUserRequest: _inbound.request.GetUserRequest,
  getUserRequestFromJson: _inbound.request.getUserRequestFromJson,
  GetUserResponse: _outbound.response.GetUserResponse,
  getUserResponseFromJson: _outbound.response.getUserResponseFromJson,

  RegisterUserAPI: _inbound.JsRegisterUserAPI,
  RegisterUserRequest: _inbound.request.RegisterUserRequest,
  RegisterUserResponse: _outbound.response.RegisterUserResponse,
  registerUserResponseFromJson: _outbound.response.registerUserResponseFromJson,
  registerUserRequestFromJson: _inbound.request.registerUserRequestFromJson,

  LoginUserAPI: _inbound.JsLoginUserAPI,
  LoginUserRequest: _inbound.request.LoginUserRequest,
  LoginUserResponse: _outbound.response.LoginUserResponse,
  loginUserResponseFromJson: _outbound.response.loginUserResponseFromJson,
  loginUserRequestFromJson: _inbound.request.loginUserRequestFromJson,

  UpdateUserPasswordAPI: _inbound.JsUpdateUserPasswordAPI,
  UpdateUserPasswordRequest: _inbound.request.UpdateUserPasswordRequest,
  UpdateUserPasswordResponse: _outbound.response.UpdateUserPasswordResponse,
  updateUserPasswordResponseFromJson: _outbound.response.updateUserPasswordResponseFromJson,
  updateUserPasswordRequestFromJson: _inbound.request.updateUserPasswordRequestFromJson,

  UpdateUserInfoAPI: _inbound.JsUpdateUserInfoAPI,
  UpdateUserInfoRequest: _inbound.request.UpdateUserInfoRequest,
  UpdateUserInfoResponse: _outbound.response.UpdateUserInfoResponse,
  updateUserInfoResponseFromJson: _outbound.response.updateUserInfoResponseFromJson,
  updateUserInfoRequestFromJson: _inbound.request.updateUserInfoRequestFromJson,

  DeleteUserAPI: _inbound.JsDeleteUserAPI,
  DeleteUserRequest: _inbound.request.DeleteUserRequest,
  DeleteUserResponse: _outbound.response.DeleteUserResponse,
  deleteUserResponseFromJson: _outbound.response.deleteUserResponseFromJson,
  deleteUserRequestFromJson: _inbound.request.deleteUserRequestFromJson,

  EmailVerificationAPI: _inbound.JsEmailVerificationAPI,
  VerifyEmailRequest: _inbound.request.VerifyEmailRequest,
  VerifyEmailResponse: _outbound.response.VerifyEmailResponse,
  verifyEmailResponseFromJson: _outbound.response.verifyEmailResponseFromJson,
  verifyEmailRequestFromJson: _inbound.request.verifyEmailRequestFromJson,
}