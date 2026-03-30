package com.munchies.suggestion.adapter.inbound.web.controller

import com.munchies.commons.Entity
import com.munchies.commons.UUIDEntityId
import com.munchies.suggestion.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.adapter.inbound.SuggestionAPI
import com.munchies.suggestion.adapter.inbound.web.config.SuggestionServiceConfig
import com.munchies.suggestion.application.port.inbound.SuggestMenuItem
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.inject.Inject

@Controller(
  port = SuggestionServiceConfig.SERVICE_PORT.toString(),
  value = SuggestionServiceConfig.SERVICE_PATH,
)
class SuggestionController(
  @Inject
  private val suggestionService: SuggestMenuItem,
) : SuggestionAPI<SuggestionRequestDTO, HttpResponse<SuggestionResponseDTO>> {
  @Get("/")
  fun suggestMenuItem(): String {
    val menus = listOf(object : Entity<UUIDEntityId>(id = UUIDEntityId()) {
      override fun toString(): String {
        return "Menu(id=${id.value}, " +
          "items=[MenuItem(id=1, name=Pizza), " +
          "MenuItem(id=2, name=Burger)])"
      }
    })

    val preferences = listOf(object : Entity<UUIDEntityId>(id = UUIDEntityId()) {
      override fun toString(): String {
        return "UserPreferences(id=${id.value}, " +
          "dietaryRestrictions=[Vegetarian], " +
          "cuisinePreferences=[Italian])"
      }
    })

    val request = SuggestionRequest(
      user = UUIDEntityId(),
      menu = menus,
      userPreferences = preferences,
      id = SuggestionRequestId(),
    )
    return suggestionService.execute(request).toString()
  }

  override fun suggestMenuItem(request: SuggestionRequestDTO): HttpResponse<SuggestionResponseDTO> {
    // TODO
    return HttpResponse.notFound()
  }
}
