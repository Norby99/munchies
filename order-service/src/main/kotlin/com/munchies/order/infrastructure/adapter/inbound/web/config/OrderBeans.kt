package com.munchies.order.infrastructure.adapter.inbound.web.config

import com.munchies.order.application.port.inbound.*
import com.munchies.order.application.usecase.*
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderNotificationPublisher
import com.munchies.order.domain.port.OrderViewRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

/**
 * Wiring of the order use cases.
 *
 * Following CQRS, command use cases depend only on the [OrderEventStore] (command side) and
 * query use cases only on the [OrderViewRepository] (query side). The projector, the bridge
 * between the two, is wired in [OrderProjectionBeans].
 */
@Factory
class OrderBeans {

  // ---------- Command side ----------

  @Singleton
  fun advanceOrderStatus(
    eventStore: OrderEventStore,
    notificationPublisher: OrderNotificationPublisher,
  ): AdvanceOrderStatus = AdvanceOrderStatusUseCase(eventStore, notificationPublisher)

  @Singleton
  fun discardOrder(eventStore: OrderEventStore): DiscardOrder = DiscardOrderUseCase(eventStore)

  @Singleton
  fun payOrder(eventStore: OrderEventStore): PayOrder = PayOrderUseCase(eventStore)

  @Singleton
  fun placeOrder(eventStore: OrderEventStore): PlaceOrder = PlaceOrderUseCase(eventStore)

  @Singleton
  fun updateDeliveryOrderInfo(eventStore: OrderEventStore): UpdateDeliveryOrderInfo =
    UpdateDeliveryOrderInfoUseCase(eventStore)

  @Singleton
  fun updateOrderItems(eventStore: OrderEventStore): UpdateOrderItems =
    UpdateOrderItemsUseCase(eventStore)

  @Singleton
  fun updateTakeawayOrderInfo(eventStore: OrderEventStore): UpdateTakeawayOrderInfo =
    UpdateTakeawayOrderInfoUseCase(eventStore)

  // ---------- Query side ----------

  @Singleton
  fun getOrderDetails(views: OrderViewRepository): GetOrderDetails = GetOrderDetailsUseCase(views)

  @Singleton
  fun getOrders(views: OrderViewRepository): GetOrders = GetOrdersUseCase(views)

  @Singleton
  fun getOrderServices(
    advanceOrderStatus: AdvanceOrderStatus,
    discardOrder: DiscardOrder,
    getOrderDetails: GetOrderDetails,
    getOrders: GetOrders,
    payOrder: PayOrder,
    placeOrder: PlaceOrder,
    updateDeliveryOrderInfo: UpdateDeliveryOrderInfo,
    updateOrderItems: UpdateOrderItems,
    updateTakeawayOrderInfo: UpdateTakeawayOrderInfo,
  ) = OrderServices(
    advanceOrderStatus,
    discardOrder,
    getOrderDetails,
    getOrders,
    payOrder,
    placeOrder,
    updateDeliveryOrderInfo,
    updateOrderItems,
    updateTakeawayOrderInfo,
  )
}

open class OrderServices(
  val advanceOrderStatus: AdvanceOrderStatus,
  val discardOrder: DiscardOrder,
  val getOrderDetails: GetOrderDetails,
  val getOrders: GetOrders,
  val payOrder: PayOrder,
  val placeOrder: PlaceOrder,
  val updateDeliveryOrderInfo: UpdateDeliveryOrderInfo,
  val updateOrderItems: UpdateOrderItems,
  val updateTakeawayOrderInfo: UpdateTakeawayOrderInfo,
)
