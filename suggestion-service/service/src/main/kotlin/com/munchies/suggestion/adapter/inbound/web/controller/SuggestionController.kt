package com.munchies.suggestion.adapter.inbound.web.controller

import com.munchies.suggestion.adapter.dto.MenuItemDTO
import com.munchies.suggestion.adapter.dto.SuggestedMenuItemDTO
import com.munchies.suggestion.adapter.dto.SuggestionConfidenceDTO
import com.munchies.suggestion.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.adapter.dto.UserPreferenceDTO
import com.munchies.suggestion.adapter.dto.mapper.SuggestionRequestMapper.toDomain
import com.munchies.suggestion.adapter.dto.mapper.SuggestionResponseMapper.toDTO
import com.munchies.suggestion.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.EmptySuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.MalformedSuggestion
import com.munchies.suggestion.domain.port.SuggestionEngine.SuggestionResult.Companion.SuggestionSuccess
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
) : SuggestionAPI<SuggestionRequestDTO, HttpResponse<SuggestionResponseDTO>> {

  @Get("/")
  override fun suggestMenuItem(
    @PathVariable request: SuggestionRequestDTO,
  ): HttpResponse<SuggestionResponseDTO> {
    val suggestionRequest = request.toDomain()
    return when (val suggestionResponse = suggestionService.execute(suggestionRequest)) {
      is SuggestionSuccess -> {
        HttpResponse.ok(suggestionResponse.result.toDTO())
      }
      is EmptySuggestion -> {
        HttpResponse.noContent()
      }
      is MalformedSuggestion -> {
        HttpResponse.unprocessableEntity()
      }
      else -> {
        HttpResponse.serverError()
      }
    }
  }
}
