package com.munchies.suggestion.infrastructure.adapter.inbound.web.client

import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.infrastructure.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.infrastructure.adapter.inbound.web.config.SuggestionServiceConfig
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client(SuggestionServiceConfig.SERVICE_PATH)
interface MicronautSuggestionClient : SuggestionAPI<SuggestionRequestDTO, SuggestionResponseDTO> {
  @Get("/")
  override fun suggestMenuItem(request: SuggestionRequestDTO): SuggestionResponseDTO = TODO()
}
