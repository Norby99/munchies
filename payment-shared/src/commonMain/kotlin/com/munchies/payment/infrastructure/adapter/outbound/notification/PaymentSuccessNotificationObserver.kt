package com.munchies.payment.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.NotificationObserver
import kotlin.js.JsExport

@JsExport
abstract class PaymentSuccessNotificationObserver :
  NotificationObserver<PaymentSuccessNotification> {
  abstract override fun update(event: PaymentSuccessNotification): Unit
}
