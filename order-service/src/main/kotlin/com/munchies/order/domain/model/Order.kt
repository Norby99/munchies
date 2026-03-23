package com.munchies.order.domain.model

import com.munchies.commons.UUIDEntityId

class Order private constructor(
  val id: OrderId,
  val customerId: CustomerId,
  val restaurantId: RestaurantId,
  val items: List<OrderItem>,
  val deliveryAddress: DeliveryAddress,
  val deliveryFee: Money,
  val createdAt: Instant,
  var status: OrderStatus = OrderStatus.PENDING,
  var paymentId: UUIDEntityId? = null,
  var paymentStatus: PaymentStatus = PaymentStatus.PENDING,
  private val domainEvents: MutableList<OrderDomainEvent> = mutableListOf(),
) {
  companion object {
    fun create(
      customerId: CustomerId,
      restaurantId: RestaurantId,
      items: List<OrderItem>,
      deliveryAddress: DeliveryAddress,
      deliveryFee: Money,
    ): Order {
      require(items.isNotEmpty()) { "Order must have at least one item" }
      val order = Order(
        id = OrderId(UUIDEntityId.randomUUID()),
        customerId = customerId,
        restaurantId = restaurantId,
        items = items,
        deliveryAddress = deliveryAddress,
        deliveryFee = deliveryFee,
        createdAt = Instant.now(),
      )
      order.domainEvents.add(OrderCreatedEvent(order.id, order.customerId, order.total()))
      return order
    }
  }

  fun total(): Money = items
    .map { it.subtotal() }
    .reduce { acc, m -> acc + m } + deliveryFee

  fun confirm() = transition(OrderStatus.CONFIRMED) {
    domainEvents.add(OrderConfirmedEvent(id))
  }

  fun cancel(reason: CancellationReason) = transition(OrderStatus.CANCELLED) {
    domainEvents.add(OrderCancelledEvent(id, reason))
  }

  fun markPaymentReceived(externalPaymentId: UUIDEntityId) {
    this.paymentId = externalPaymentId
    this.paymentStatus = PaymentStatus.PAID
  }

  fun pullDomainEvents(): List<OrderDomainEvent> {
    val events = domainEvents.toList()
    domainEvents.clear()
    return events
  }

  private fun transition(next: OrderStatus, block: () -> Unit) {
    require(status.canTransitionTo(next)) {
      "Cannot transition from $status to $next"
    }
    status = next
    block()
  }
}

// TODO: Temporary/To be discussed
enum class PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

// TODO: Temporary/To be discussed
enum class CancellationReason { CUSTOMER_REQUEST, RESTAURANT_REJECTED, PAYMENT_FAILED, SYSTEM }
