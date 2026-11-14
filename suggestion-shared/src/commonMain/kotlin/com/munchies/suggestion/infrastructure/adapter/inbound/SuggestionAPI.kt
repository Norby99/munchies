package com.munchies.suggestion.infrastructure.adapter.inbound

import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest
import kotlin.js.JsExport

@JsExport
object SuggestionAPI {
  interface SuggestMenuItemAPI<Response> {
    fun suggestMenuItem(request: SuggestItemRequest): Response
  }
}
