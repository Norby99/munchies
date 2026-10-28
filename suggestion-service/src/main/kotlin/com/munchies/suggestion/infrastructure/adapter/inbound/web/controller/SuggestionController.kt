package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
import com.munchies.suggestion.infrastructure.adapter.dto.MenuItemDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestedMenuItemDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionConfidenceDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.infrastructure.adapter.dto.UserPreferenceDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionRequestMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionResponseMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.infrastructure.adapter.inbound.request.SuggestItemRequest
import com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.EngineTimeoutException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.MalformedResponseException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import com.munchies.suggestion.infrastructure.adapter.outbound.response.SuggestItemResponse as WebSuggestItemResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.serde.annotation.SerdeImport
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject

@SerdeImport(SuggestionRequestDTO::class)
@SerdeImport(MenuItemDTO::class)
@SerdeImport(UserPreferenceDTO::class)
@SerdeImport(SuggestionResponseDTO::class)
@SerdeImport(SuggestionConfidenceDTO::class)
@SerdeImport(SuggestedMenuItemDTO::class)
@SerdeImport(SuggestItemRequest::class)
@SerdeImport(WebSuggestItemResponse::class)
@SerdeImport(ErrorResponse::class)
@SerdeImport(EngineTimeoutException::class)
@SerdeImport(FactoryException::class)
@SerdeImport(MalformedResponseException::class)
@SerdeImport(UnexpectedException::class)
@Controller(
  port = SuggestionServiceConfig.SERVICE_PORT.toString(),
  value = SuggestionServiceConfig.SERVICE_PATH,
)
class SuggestionController(
  @Inject
  private val suggestionService: SuggestMenuItem,
) : SuggestionAPI.SuggestMenuItemAPI<HttpResponse<WebSuggestItemResponse>> {

  @Post("/")
  @Operation(
    summary = "Suggest menu item",
    description =
    "Provides menu item suggestions based on user preferences and available menu items.",
  )
  @ApiResponse(
    responseCode = "200",
    description = "Suggestion generated successfully",
  )
  @ApiResponse(
    responseCode = "422",
    description = "Engine returned malformed or empty suggestion",
  )
  @ApiResponse(
    responseCode = "500",
    description = "Engine timed out or unexpected error",
  )
  override fun suggestMenuItem(
    @Body request: SuggestItemRequest,
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
