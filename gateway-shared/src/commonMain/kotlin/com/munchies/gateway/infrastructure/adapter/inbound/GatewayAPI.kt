package com.munchies.gateway.infrastructure.adapter.inbound

import com.munchies.gateway.infrastructure.adapter.inbound.request.LogoutUserRequest
import kotlin.js.JsExport

@JsExport
object GatewayAPI {
  interface LogoutUserAPI<Response> {
    fun logoutUser(request: LogoutUserRequest): Response
  }
}
