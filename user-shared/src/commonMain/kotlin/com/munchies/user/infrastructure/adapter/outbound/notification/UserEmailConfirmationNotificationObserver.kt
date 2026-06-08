package com.munchies.user.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

@JsExport
abstract class UserEmailConfirmationNotificationObserver {
  abstract fun update(event: UserEmailConfirmationNotification): Unit
}
