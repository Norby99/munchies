package com.munchies.suggestion.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JsExport
@Serializable
@SerialName("UserPreferenceDTO")
data class UserPreferenceDTO(
  val preference: String,
)
