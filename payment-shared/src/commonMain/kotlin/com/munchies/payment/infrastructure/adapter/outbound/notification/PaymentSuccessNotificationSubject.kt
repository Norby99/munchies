package com.munchies.payment.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.NotificationSubject
import kotlin.js.JsExport

@JsExport
abstract class PaymentSuccessNotificationSubject :
  NotificationSubject<
    PaymentSuccessNotification,
    PaymentSuccessNotificationObserver,
    > {
  abstract override fun attach(observer: PaymentSuccessNotificationObserver): Unit
  abstract override fun detach(observer: PaymentSuccessNotificationObserver): Unit
  abstract override fun emit(event: PaymentSuccessNotification): Unit
}
