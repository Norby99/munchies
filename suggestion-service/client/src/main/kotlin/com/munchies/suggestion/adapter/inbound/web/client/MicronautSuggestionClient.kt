package com.munchies.suggestion.adapter.inbound.web.client

import com.munchies.suggestion.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.adapter.inbound.web.config.SuggestionServiceConfig
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client(SuggestionServiceConfig.SERVICE_PATH)
interface MicronautSuggestionClient : SuggestionAPI<SuggestionRequestDTO, SuggestionResponseDTO> {
  @Get("/")
  override fun suggestMenuItem(request: SuggestionRequestDTO): SuggestionResponseDTO = TODO()
}
