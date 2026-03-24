package com.munchies.order.domain.event

import com.munchies.commons.UUIDEntityId
import com.munchies.order.domain.model.CancellationReason
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.Money
import com.munchies.order.domain.model.OrderId
import java.time.Instant

sealed class OrderDomainEvent {
  abstract val orderId: OrderId
  abstract val occurredAt: Instant
}

data class OrderCreatedEvent(
  override val orderId: OrderId,
  val customerId: CustomerId,
  val total: Money,
  override val occurredAt: Instant = Instant.now(),
) : OrderDomainEvent()

data class OrderConfirmedEvent(
  override val orderId: OrderId,
  override val occurredAt: Instant = Instant.now(),
) : OrderDomainEvent()

data class OrderCancelledEvent(
  override val orderId: OrderId,
  val reason: CancellationReason,
  override val occurredAt: Instant = Instant.now(),
) : OrderDomainEvent()

data class OrderPaymentReceivedEvent(
  override val orderId: OrderId,
  val paymentId: UUIDEntityId,
  override val occurredAt: Instant = Instant.now(),
) : OrderDomainEvent()
