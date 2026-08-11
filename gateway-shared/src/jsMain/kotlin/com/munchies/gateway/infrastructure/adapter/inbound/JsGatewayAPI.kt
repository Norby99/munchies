package com.munchies.gateway.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.gateway.infrastructure.adapter.inbound.request.LogoutUserRequest
import com.munchies.gateway.infrastructure.adapter.inbound.request.logoutUserRequestFromJson
import com.munchies.gateway.infrastructure.adapter.inbound.web.config.GatewayServiceConfig
import com.munchies.gateway.infrastructure.adapter.outbound.response.LogoutUserResponse
import com.munchies.gateway.infrastructure.adapter.outbound.response.logoutUserResponseFromJson
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsLogoutUserAPI<E : WebResponse<Any>> :
  GatewayAPI.LogoutUserAPI<Promise<E>>,
  SimpleAPI<
    LogoutUserRequest,
    LogoutUserResponse,
    >() {
  override fun getPath(): String =
    GatewayServiceConfig.SERVICE_PATH + GatewayServiceConfig.LOGOUT_USER_PATH

  override fun getPort(): Int = GatewayServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER

  abstract override fun logoutUser(request: LogoutUserRequest): Promise<E>

  override fun parseRequest(json: String): LogoutUserRequest = logoutUserRequestFromJson(json)
  override fun parseResponse(json: String): LogoutUserResponse = logoutUserResponseFromJson(json)
}

@JsExport
abstract class JsRefreshTokenAPI
