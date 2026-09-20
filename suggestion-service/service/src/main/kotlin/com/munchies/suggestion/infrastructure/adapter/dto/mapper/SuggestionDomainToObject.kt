package com.munchies.suggestion.infrastructure.adapter.dto.mapper

import com.munchies.suggestion.domain.model.MenuItem
import com.munchies.suggestion.domain.model.MenuItemId
import com.munchies.suggestion.domain.model.SuggestedMenuItem
import com.munchies.suggestion.domain.model.SuggestionConfidence
import com.munchies.suggestion.domain.model.SuggestionRequest
import com.munchies.suggestion.domain.model.SuggestionRequestId
import com.munchies.suggestion.domain.model.SuggestionResponse
import com.munchies.suggestion.domain.model.UserPreference
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
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.UserPreferenceMapper.toDTO
import com.munchies.suggestion.infrastructure.adapter.dto.mapper.UserPreferenceMapper.toDomain

object SuggestionRequestMapper {
  fun SuggestionRequestDTO.toDomain(): SuggestionRequest = SuggestionRequest(
    user = SuggestionRequestId(this.userId),
    menu = this.menu.map { it.toDomain() },
    userPreferences = this.userPreferences.map { it.toDomain() },
  )

  fun SuggestionRequest.toDTO(): SuggestionRequestDTO = SuggestionRequestDTO(
    userId = this.user.value,
    menu = this.menu.map { it.toDTO() },
    userPreferences = this.userPreferences.map { it.toDTO() },
  )
}

object SuggestionResponseMapper {
  fun SuggestionResponseDTO.toDomain(): SuggestionResponse = SuggestionResponse(
    rationale = this.rationale,
    confidence = this.confidence.toDomain(),
    suggestedMenuItems = this.suggestedMenuItems.map { it.toDomain() },
  )

  fun SuggestionResponse.toDTO(): SuggestionResponseDTO = SuggestionResponseDTO(
    rationale = this.rationale,
    confidence = this.confidence.toDTO(),
    suggestedMenuItems = this.suggestedMenuItems.map { it.toDTO() },
  )
}

object MenuItemMapper {
  fun MenuItemDTO.toDomain(): MenuItem = MenuItem(
    id = MenuItemId(this.id),
    name = this.name,
    description = this.description,
    price = this.price,
  )

  fun MenuItem.toDTO(): MenuItemDTO = MenuItemDTO(
    id = this.id.value,
    name = this.name,
    description = this.description,
    price = this.price,
  )
}

object UserPreferenceMapper {
  fun UserPreferenceDTO.toDomain(): UserPreference = UserPreference(
    preference = this.preference,
  )

  fun UserPreference.toDTO(): UserPreferenceDTO = UserPreferenceDTO(
    preference = this.preference,
  )
}

object SuggestionConfidenceMapper {
  fun SuggestionConfidenceDTO.toDomain(): SuggestionConfidence = SuggestionConfidence.valueOf(
    this.level,
  )
  fun SuggestionConfidence.toDTO(): SuggestionConfidenceDTO = SuggestionConfidenceDTO(
    level = this.toString(),
  )
}

object SuggestedMenuItemMapper {
  fun SuggestedMenuItemDTO.toDomain(): SuggestedMenuItem = SuggestedMenuItem(
    itemId = this.itemId,
    reason = this.reason,
  )

  fun SuggestedMenuItem.toDTO(): SuggestedMenuItemDTO = SuggestedMenuItemDTO(
    itemId = this.itemId,
    reason = this.reason,
  )
}
