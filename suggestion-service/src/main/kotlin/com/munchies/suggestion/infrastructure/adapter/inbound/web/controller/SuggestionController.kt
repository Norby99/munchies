package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller

import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.infrastructure.adapter.dto.*
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionRequestMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionResponseMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest
import com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.EngineTimeoutException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.MalformedResponseException
import com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse as WebSuggestItemResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Inject

@SerdeImport(SuggestionRequestDTO::class)
@SerdeImport(MenuItemDTO::class)
@SerdeImport(UserPreferenceDTO::class)
@SerdeImport(SuggestionResponseDTO::class)
@SerdeImport(SuggestionConfidenceDTO::class)
@SerdeImport(SuggestedMenuItemDTO::class)
@Controller(
  port = SuggestionServiceConfig.SERVICE_PORT.toString(),
  value = SuggestionServiceConfig.SERVICE_PATH,
)
class SuggestionController(
  @Inject
  private val suggestionService: SuggestMenuItem,
) : SuggestionAPI<HttpResponse<WebSuggestItemResponse>> {

  @Get("/")
  override fun suggestMenuItem(
    @PathVariable request: SuggestItemRequest,
  ): HttpResponse<WebSuggestItemResponse> {
    val suggestionRequest = request.dto.toDomain()
    return when (val suggestionResponse = suggestionService.execute(suggestionRequest)) {
      is SuggestionSuccess -> {
        HttpResponse.ok(WebSuggestItemResponse(suggestionResponse.result.toDTO()))
      }
      is EmptySuggestion -> {
        throw MalformedResponseException("Engine's response was empty")
      }
      is MalformedSuggestion -> {
        throw MalformedResponseException("Engine's response was malformed")
      }
      else -> {
        throw EngineTimeoutException("Engine timed out")
      }
    }
  }
}
