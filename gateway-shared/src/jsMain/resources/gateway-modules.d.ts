// gateway-modules.d.ts
import type { com } from "./munchies-gateway-shared";

export declare const GatewayServiceConfig: typeof com.munchies.gateway.infrastructure.adapter.inbound.web.config.GatewayServiceConfig;

export type LogoutUserAPI =
  com.munchies.gateway.infrastructure.adapter.inbound.JsLogoutUserAPI;
export declare const LogoutUserAPI: typeof
  com.munchies.gateway.infrastructure.adapter.inbound.JsLogoutUserAPI;

export type LogoutUserRequest = com.munchies.gateway.infrastructure.adapter.inbound.request.LogoutUserRequest;
export declare const LogoutUserRequest: typeof com.munchies.gateway.infrastructure.adapter.inbound.request.LogoutUserRequest;

export type LogoutUserResponse = com.munchies.gateway.infrastructure.adapter.outbound.response.LogoutUserResponse;
export declare const LogoutUserResponse: typeof com.munchies.gateway.infrastructure.adapter.outbound.response.LogoutUserResponse;

export declare const logoutUserResponseFromJson: typeof com.munchies.gateway.infrastructure.adapter.outbound.response.logoutUserResponseFromJson;
export declare const logoutUserRequestFromJson: typeof com.munchies.gateway.infrastructure.adapter.inbound.request.logoutUserRequestFromJson;
