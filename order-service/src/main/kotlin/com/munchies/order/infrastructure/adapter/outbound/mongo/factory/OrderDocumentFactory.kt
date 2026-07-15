package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.*
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.*

/**
 * Factory object for converting between Order domain models and OrderDocument representations.
 *
 * This factory provides methods to convert Order objects to OrderDocument objects for persistence
 * in a MongoDB database, and vice versa. It handles different types of orders (Delivery, DineIn,
 * Takeaway) and their associated information.
 */
object OrderDocumentFactory {

  private const val DELIVERY = "DELIVERY"
  private const val DINE_IN = "DINE_IN"
  private const val TAKEAWAY = "TAKEAWAY"

  /**
   * Converts an Order domain model to an OrderDocument for persistence.
   *
   * @receiver The Order domain model to convert.
   * @return The corresponding OrderDocument representation.
   */
  fun Order.toDocument(): OrderDocument = when (this) {
    is DeliveryOrder -> OrderDocument(
      id = this.id.value,
      orderType = DELIVERY,
      restaurantId = this.restaurantId.value,
      customerId = this.customerId.value,
      status = this.status.name,
      items = this.items.map { it.toDocument() },
      deliveryInfo = this.deliveryInfo.toDocument(),
    )
    is DineInOrder -> OrderDocument(
      id = this.id.value,
      orderType = DINE_IN,
      restaurantId = this.restaurantId.value,
      customerId = this.customerId.value,
      status = this.status.name,
      items = this.items.map { it.toDocument() },
      tableInfo = this.tableInfo.toDocument(),
    )
    is TakeawayOrder -> OrderDocument(
      id = this.id.value,
      orderType = TAKEAWAY,
      restaurantId = this.restaurantId.value,
      customerId = this.customerId.value,
      status = this.status.name,
      items = this.items.map { it.toDocument() },
      takeawayInfo = this.takeawayInfo.toDocument(),
    )
  }

  /**
   * Converts an OrderDocument to an Order domain model.
   *
   * @receiver The OrderDocument to convert.
   * @return The corresponding Order domain model, or null if conversion fails.
   */
  fun OrderDocument.toNullableDomain(): Order? = runCatching {
    val id = OrderId(this.id)
    val restaurantId = RestaurantId(this.restaurantId)
    val customerId = CustomerId(this.customerId)
    val status = OrderStatus.valueOf(this.status)
    val items = this.items.map { it.toDomain() }

    when (this.orderType) {
      DELIVERY -> DeliveryOrder(
        id = id,
        restaurantId = restaurantId,
        customerId = customerId,
        status = status,
        items = items,
        deliveryInfo = requireNotNull(this.deliveryInfo) {
          "Missing deliveryInfo for DELIVERY order"
        }.toDomain(),
      )
      DINE_IN -> DineInOrder(
        id = id,
        restaurantId = restaurantId,
        customerId = customerId,
        status = status,
        items = items,
        tableInfo = requireNotNull(
          this.tableInfo,
        ) { "Missing tableInfo for DINE_IN order" }.toDomain(),
      )
      TAKEAWAY -> TakeawayOrder(
        id = id,
        restaurantId = restaurantId,
        customerId = customerId,
        status = status,
        items = items,
        takeawayInfo = requireNotNull(this.takeawayInfo) {
          "Missing takeawayInfo for TAKEAWAY order"
        }.toDomain(),
      )
      else -> error("Unknown order type: ${this.orderType}")
    }
  }.getOrNull()

  private fun OrderItem.toDocument(): OrderItemDocument =
    OrderItemDocument(menuItemId = this.menuItemId.value, quantity = this.quantity)

  private fun OrderItemDocument.toDomain(): OrderItem =
    OrderItem(menuItemId = MenuItemId(this.menuItemId), quantity = this.quantity)

  private fun DeliveryInfo.toDocument(): DeliveryInfoDocument = DeliveryInfoDocument(
    estimatedDeliveryTime = this.estimatedDeliveryTime,
    deliveryAddress = this.deliveryAddress,
    bellName = this.bellName,
    customerPhone = this.customerPhone,
  )

  private fun DeliveryInfoDocument.toDomain(): DeliveryInfo = DeliveryInfo(
    estimatedDeliveryTime = this.estimatedDeliveryTime,
    deliveryAddress = this.deliveryAddress,
    bellName = this.bellName,
    customerPhone = this.customerPhone,
  )

  private fun TableInfo.toDocument(): TableInfoDocument =
    TableInfoDocument(tableNumber = this.tableNumber, numberOfGuests = this.numberOfGuests)

  private fun TableInfoDocument.toDomain(): TableInfo =
    TableInfo(tableNumber = this.tableNumber, numberOfGuests = this.numberOfGuests)

  private fun TakeawayInfo.toDocument(): TakeawayInfoDocument =
    TakeawayInfoDocument(pickupTime = this.pickupTime, customerName = this.customerName)

  private fun TakeawayInfoDocument.toDomain(): TakeawayInfo =
    TakeawayInfo(pickupTime = this.pickupTime, customerName = this.customerName)
}
