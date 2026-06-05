package com.munchies.user.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class UserEmailConfirmationNotification(
  val user_id_key: String,
  val user_confirmation_key: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun userEmailConfirmationNotificationFromJson(json: String): UserEmailConfirmationNotification =
  Json.decodeFromString(json)
