package com.munchies.suggestion.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("MenuItemDTO")
data class MenuItemDTO(
  val id: String,
  val name: String,
  val description: String,
  val price: Double,
)


//TODO() validator?
