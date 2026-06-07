package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo

sealed interface PlaceOrderCommand {
  val restaurantId: RestaurantId
  val customerId: CustomerId
  val items: List<OrderItem>

  data class Delivery(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val deliveryInfo: DeliveryInfo,
  ) : PlaceOrderCommand

  data class Takeaway(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val takeawayInfo: TakeawayInfo,
  ) : PlaceOrderCommand

  data class DineIn(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val tableInfo: TableInfo,
  ) : PlaceOrderCommand
}
