// user-modules.js
const generated = require("./munchies-user-shared.js");
const _user = generated.com.munchies.user;
const _inbound = _user.infrastructure.adapter.inbound;
const _outbound = _user.infrastructure.adapter.outbound;

module.exports = {
  UserDTO: _user.infrastructure.adapter.dto.UserDTO,
  UserServiceConfig: _user.infrastructure.adapter.inbound.web.config.UserServiceConfig,
  
  GetUserAPI: _inbound.JsGetUserAPI,
  GetUserResponse: _outbound.response.GetUserResponse,
  getUserResponseFromJson: _outbound.response.getUserResponseFromJson,
  GetUserResult: _outbound.response.GetUserResult,
  GetUserSuccess: _outbound.response.GetUserSuccess,
  GetUserFailure: _outbound.response.GetUserFailure,
  
  RegisterUserAPI: _inbound.JsRegisterUserAPI,
  RegisterUserRequest: _inbound.request.RegisterUserRequest,
  RegisterUserResponse: _outbound.response.RegisterUserResponse,
  registerUserResponseFromJson: _outbound.response.registerUserResponseFromJson,
  RegisterUserResult: _outbound.response.RegisterUserResult,
  RegisterUserSuccess: _outbound.response.RegisterUserSuccess,
  RegisterUserFailure: _outbound.response.RegisterUserFailure,

  LoginUserAPI: _inbound.JsLoginUserAPI,
  LoginUserRequest: _inbound.request.LoginUserRequest,
  LoginUserResponse: _outbound.response.LoginUserResponse,
  loginUserResponseFromJson: _outbound.response.loginUserResponseFromJson,
  LoginUserResult: _outbound.response.LoginUserResult,
  LoginUserSuccess: _outbound.response.LoginUserSuccess,
  LoginUserFailure: _outbound.response.LoginUserFailure,

  UpdateUserPasswordAPI: _inbound.JsUpdateUserPasswordAPI,
  UpdateUserPasswordRequest: _inbound.request.UpdateUserPasswordRequest,
  UpdateUserPasswordResponse: _outbound.response.UpdateUserPasswordResponse,
  updateUserPasswordResponseFromJson: _outbound.response.updateUserPasswordResponseFromJson,
  UpdateUserPasswordResult: _outbound.response.UpdateUserPasswordResult,
  UpdateUserPasswordSuccess: _outbound.response.UpdateUserPasswordSuccess,
  UpdateUserPasswordFailure: _outbound.response.UpdateUserPasswordFailure,  

  UpdateUserInfoAPI: _inbound.JsUpdateUserInfoAPI,
  UpdateUserInfoRequest: _inbound.request.UpdateUserInfoRequest,
  UpdateUserInfoResponse: _outbound.response.UpdateUserInfoResponse,
  updateUserInfoResponseFromJson: _outbound.response.updateUserInfoResponseFromJson,
  UpdateUserInfoResult: _outbound.response.UpdateUserInfoResult,
  UpdateUserInfoSuccess: _outbound.response.UpdateUserInfoSuccess,
  UpdateUserInfoFailure: _outbound.response.UpdateUserInfoFailure,

  DeleteUserAPI: _inbound.JsDeleteUserAPI,
  DeleteUserRequest: _inbound.request.DeleteUserRequest,
  DeleteUserResponse: _outbound.response.DeleteUserResponse,
  deleteUserResponseFromJson: _outbound.response.deleteUserResponseFromJson,
  DeleteUserResult: _outbound.response.DeleteUserResult,
  DeleteUserSuccess: _outbound.response.DeleteUserSuccess,
  DeleteUserFailure: _outbound.response.DeleteUserFailure,
}