package com.munchies.user.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

@JsExport
abstract class UserEmailConfirmationNotificationSubject {
  abstract fun attach(observer: UserEmailConfirmationNotificationObserver): Unit
  abstract fun detach(observer: UserEmailConfirmationNotificationObserver): Unit
  abstract fun emit(event: UserEmailConfirmationNotification): Unit
}
