package com.munchies.user.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

@JsExport
object UserEmailConfirmationNotificationInfo {
  const val USER_ID_KEY = "user_id_key"
  const val USER_CONFIRMATION_KEY = "user_confirmation_key"
  const val USER_EMAIL_CONFIRMATION_TOPIC = "user_email_confirmation_topic"
  const val USER_EMAIL_CONFIRMATION_GROUP_ID = "user_email_confirmation_group_id"
}
