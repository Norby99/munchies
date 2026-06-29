package com.munchies.user.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.NotificationSubject
import kotlin.js.JsExport

@JsExport
abstract class UserEmailConfirmationNotificationSubject :
  NotificationSubject<
    UserEmailConfirmationNotification,
    UserEmailConfirmationNotificationObserver,
    > {
  abstract override fun attach(observer: UserEmailConfirmationNotificationObserver): Unit
  abstract override fun detach(observer: UserEmailConfirmationNotificationObserver): Unit
  abstract override fun emit(event: UserEmailConfirmationNotification): Unit
}
