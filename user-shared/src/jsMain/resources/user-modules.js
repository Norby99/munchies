// user-modules.js
const generated = require("./munchies-user-shared.js");
const _user = generated.com.munchies.user;
const _inbound = _user.infrastructure.adapter.inbound;
const _outbound = _user.infrastructure.adapter.outbound;

module.exports = {
  UserDTO: _user.infrastructure.adapter.dto.UserDTO,
  
  GetUserAPI: _inbound.JsGetUserAPI,
  GetUserResponse: _outbound.response.GetUserResponse,
  getUserResponseFromJson: _outbound.response.getUserResponseFromJson,
  GetUserResult: _outbound.response.GetUserResult,
  GetUserSuccess: _outbound.response.GetUserSuccess,
  GetUserFailure: _outbound.response.GetUserFailure,
  
  RegisterUserAPI: _inbound.JsRegisterUserAPI,
  RegisterUserResponse: _outbound.response.RegisterUserResponse,
  registerUserResponseFromJson: _outbound.response.registerUserResponseFromJson,
  RegisterUserResult: _outbound.response.RegisterUserResult,
  RegisterUserSuccess: _outbound.response.RegisterUserSuccess,
  RegisterUserFailure: _outbound.response.RegisterUserFailure
}