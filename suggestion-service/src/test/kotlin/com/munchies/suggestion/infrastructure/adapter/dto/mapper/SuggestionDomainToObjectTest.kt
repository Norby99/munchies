package com.munchies.suggestion.infrastructure.adapter.dto.mapper

import com.munchies.suggestion.domain.model.MenuItemId
import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionRequestId
import com.munchies.suggestion.infrastructure.adapter.dto.MenuItemDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestedMenuItemDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionConfidenceDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionRequestDTO
import com.munchies.suggestion.infrastructure.adapter.dto.SuggestionResponseDTO
import com.munchies.suggestion.infrastructure.adapter.dto.UserPreferenceDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.MenuItemMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.MenuItemMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestedMenuItemMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestedMenuItemMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionConfidenceMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionConfidenceMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionRequestMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionRequestMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionResponseMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.SuggestionResponseMapper.toDomain
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.UserPreferenceMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.UserPreferenceMapper.toDomain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SuggestionDomainToObjectTest {

  @Test
  fun `MenuItem mapping roundtrip`() {
    val dto = MenuItemDTO(
      id = "item-1",
      name = "Margherita",
      description = "Classic pizza",
      price = 8.5,
    )
    val domain = dto.toDomain()
    domain.id shouldBe MenuItemId("item-1")
    domain.name shouldBe "Margherita"
    domain.description shouldBe "Classic pizza"
    domain.price shouldBe 8.5

    val backToDto = domain.toDTO()
    backToDto shouldBe dto
  }

  @Test
  fun `UserPreference mapping roundtrip`() {
    val dto = UserPreferenceDTO(preference = "vegetarian")
    val domain = dto.toDomain()
    domain.preference shouldBe "vegetarian"

    val backToDto = domain.toDTO()
    backToDto shouldBe dto
  }

  @Test
  fun `SuggestionConfidence mapping roundtrip`() {
    val dto = SuggestionConfidenceDTO(level = "HIGH")
    val domain = dto.toDomain()
    domain shouldBe SuggestionConfidence.HIGH

    val backToDto = domain.toDTO()
    backToDto shouldBe dto
  }

  @Test
  fun `SuggestedMenuItem mapping roundtrip`() {
    val dto = SuggestedMenuItemDTO(itemId = "item-1", reason = "Matches preference")
    val domain = dto.toDomain()
    domain.itemId shouldBe "item-1"
    domain.reason shouldBe "Matches preference"

    val backToDto = domain.toDTO()
    backToDto shouldBe dto
  }

  @Test
  fun `SuggestionRequest mapping roundtrip`() {
    val dto = SuggestionRequestDTO(
      userId = "user-123",
      menu = listOf(
        MenuItemDTO(id = "1", name = "Pizza", description = "Tasty", price = 10.0),
      ),
      userPreferences = listOf(
        UserPreferenceDTO(preference = "vegan"),
      ),
    )
    val domain = dto.toDomain()
    domain.user shouldBe SuggestionRequestId("user-123")
    domain.menu.size shouldBe 1
    domain.userPreferences.size shouldBe 1

    val backToDto = domain.toDTO()
    backToDto.userId shouldBe dto.userId
    backToDto.menu shouldBe dto.menu
    backToDto.userPreferences shouldBe dto.userPreferences
  }

  @Test
  fun `SuggestionResponse mapping roundtrip`() {
    val dto = SuggestionResponseDTO(
      rationale = "Healthy option",
      confidence = SuggestionConfidenceDTO("MEDIUM"),
      suggestedMenuItems = listOf(
        SuggestedMenuItemDTO(itemId = "item-2", reason = "Low calorie"),
      ),
    )
    val domain = dto.toDomain()
    domain.rationale shouldBe "Healthy option"
    domain.confidence shouldBe SuggestionConfidence.MEDIUM
    domain.suggestedMenuItems.size shouldBe 1

    val backToDto = domain.toDTO()
    backToDto shouldBe dto
  }
}
