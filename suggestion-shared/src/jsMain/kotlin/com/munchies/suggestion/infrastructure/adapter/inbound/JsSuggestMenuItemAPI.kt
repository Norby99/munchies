package com.munchies.suggestion.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest
import com.munchies.suggestion.infrastructure.adapter.inbound.request.suggestItemRequestFromJson
import com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse
import com.munchies.suggestion.infrastructure.adapter.outbound.response.suggestItemResponseFromJson
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsSuggestMenuItemAPI<E : WebResponse<Any>> :
  SuggestionAPI.SuggestMenuItemAPI<Promise<E>>,
  SimpleAPI<SuggestItemRequest, SuggestItemResponse>() {
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getPath(): String = SuggestionServiceConfig.SERVICE_PATH
  override fun getPort(): Int = SuggestionServiceConfig.SERVICE_PORT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER

  abstract override fun suggestMenuItem(request: SuggestItemRequest): Promise<E>

  override fun parseRequest(json: String): SuggestItemRequest = suggestItemRequestFromJson(json)
  override fun parseResponse(json: String): SuggestItemResponse = suggestItemResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}
