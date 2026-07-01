package com.munchies.commons.infrastructure.adapter

import com.munchies.commons.domain.port.AuthRole
import kotlin.js.JsExport

@JsExport
abstract class API {
  abstract fun getPath(): String
  abstract fun getPort(): Int
  abstract fun getMethod(): HttpMethod
  abstract fun getRequiredAuthRole(): AuthRole
}
