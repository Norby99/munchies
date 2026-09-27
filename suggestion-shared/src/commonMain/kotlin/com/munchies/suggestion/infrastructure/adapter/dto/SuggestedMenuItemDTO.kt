package com.munchies.suggestion.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JsExport
@Serializable
@SerialName("SuggestedMenuItemDTO")
data class SuggestedMenuItemDTO(
  val itemId: String,
  val reason: String,
)
