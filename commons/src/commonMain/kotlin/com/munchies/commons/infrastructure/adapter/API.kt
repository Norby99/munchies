package com.munchies.commons.infrastructure.adapter

import kotlin.js.JsExport

@JsExport
abstract class API {
  abstract fun getPath(): String
  abstract fun getPort(): Int
  abstract fun getMethod(): HttpMethod
}
