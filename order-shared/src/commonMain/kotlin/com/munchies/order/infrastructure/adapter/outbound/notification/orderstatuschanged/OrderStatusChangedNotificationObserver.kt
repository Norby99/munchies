package com.munchies.order.infrastructure.adapter.outbound.notification.orderstatuschanged

import com.munchies.commons.domain.port.NotificationObserver
import kotlin.js.JsExport

@JsExport
abstract class OrderStatusChangedNotificationObserver :
  NotificationObserver<OrderStatusChangedNotification> {
  abstract override fun update(event: OrderStatusChangedNotification): Unit
}
