// gateway-modules.js
const generated = require("./munchies-gateway-shared.js");
const _gateway = generated.com.munchies.gateway;
const _inbound = _gateway.infrastructure.adapter.inbound;
const _outbound = _gateway.infrastructure.adapter.outbound;

module.exports = {
  GatewayServiceConfig: _inbound.web.config.GatewayServiceConfig,

  LogoutUserAPI: _inbound.JsLogoutUserAPI,
  LogoutUserRequest: _inbound.request.LogoutUserRequest,
  logoutUserRequestFromJson: _inbound.request.logoutUserRequestFromJson,
  LogoutUserResponse: _outbound.response.LogoutUserResponse,
  logoutUserResponseFromJson: _outbound.response.logoutUserResponseFromJson,
};
