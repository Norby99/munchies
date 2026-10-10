package com.munchies.suggestion.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport
import kotlinx.serialization.SerialName


@JsExport
@SerialName("SuggestionServiceConfig")
object SuggestionServiceConfig {
  const val SERVICE_PORT = 8080
  const val SERVICE_PATH = "/suggestion"
}
