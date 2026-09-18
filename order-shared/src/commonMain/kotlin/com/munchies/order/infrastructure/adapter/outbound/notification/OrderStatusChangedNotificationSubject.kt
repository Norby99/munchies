package com.munchies.order.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.NotificationSubject
import kotlin.js.JsExport

@JsExport
abstract class OrderStatusChangedNotificationSubject :
  NotificationSubject<
    OrderStatusChangedNotification,
    OrderStatusChangedNotificationObserver,
    > {
  abstract override fun attach(observer: OrderStatusChangedNotificationObserver): Unit
  abstract override fun detach(observer: OrderStatusChangedNotificationObserver): Unit
  abstract override fun emit(event: OrderStatusChangedNotification): Unit
}
