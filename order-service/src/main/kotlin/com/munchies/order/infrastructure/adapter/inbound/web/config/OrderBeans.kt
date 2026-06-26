package com.munchies.order.infrastructure.adapter.inbound.web.config

import com.munchies.order.application.port.inbound.*
import com.munchies.order.application.usecase.*
import com.munchies.order.domain.ports.OrderRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class OrderBeans {

  @Singleton
  fun advanceOrderStatus(repo: OrderRepository): AdvanceOrderStatus =
    AdvanceOrderStatusUseCase(repo)

  @Singleton
  fun discardOrder(repo: OrderRepository): DiscardOrder = DiscardOrderUseCase(repo)

  @Singleton
  fun getOrderDetails(repo: OrderRepository): GetOrderDetails = GetOrderDetailsUseCase(repo)

  @Singleton
  fun placeOrder(repo: OrderRepository): PlaceOrder = PlaceOrderUseCase(repo)

  @Singleton
  fun updateDeliveryOrderInfo(repo: OrderRepository): UpdateDeliveryOrderInfo =
    UpdateDeliveryOrderInfoUseCase(repo)

  @Singleton
  fun updateOrderItems(repo: OrderRepository): UpdateOrderItems = UpdateOrderItemsUseCase(repo)

  @Singleton
  fun updateTakeawayOrderInfo(repo: OrderRepository): UpdateTakeawayOrderInfo =
    UpdateTakeawayOrderInfoUseCase(repo)

  @Singleton
  fun getOrderServices(
    advanceOrderStatus: AdvanceOrderStatus,
    discardOrder: DiscardOrder,
    getOrderDetails: GetOrderDetails,
    placeOrder: PlaceOrder,
    updateDeliveryOrderInfo: UpdateDeliveryOrderInfo,
    updateOrderItems: UpdateOrderItems,
    updateTakeawayOrderInfo: UpdateTakeawayOrderInfo,
  ) = OrderServices(
    advanceOrderStatus,
    discardOrder,
    getOrderDetails,
    placeOrder,
    updateDeliveryOrderInfo,
    updateOrderItems,
    updateTakeawayOrderInfo,
  )
}

data class OrderServices(
  val advanceOrderStatus: AdvanceOrderStatus,
  val discardOrder: DiscardOrder,
  val getOrderDetails: GetOrderDetails,
  val placeOrder: PlaceOrder,
  val updateDeliveryOrderInfo: UpdateDeliveryOrderInfo,
  val updateOrderItems: UpdateOrderItems,
  val updateTakeawayOrderInfo: UpdateTakeawayOrderInfo,
)
