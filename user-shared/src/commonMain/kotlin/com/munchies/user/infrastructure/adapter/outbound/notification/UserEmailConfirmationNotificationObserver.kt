package com.munchies.user.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.NotificationObserver
import kotlin.js.JsExport

@JsExport
abstract class UserEmailConfirmationNotificationObserver :
  NotificationObserver<UserEmailConfirmationNotification> {
  abstract override fun update(event: UserEmailConfirmationNotification): Unit
}
