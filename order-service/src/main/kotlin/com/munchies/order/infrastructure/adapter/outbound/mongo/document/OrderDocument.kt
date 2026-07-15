package com.munchies.order.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable

/**
 * Represents the MongoDB document for storing order information.
 *
 * Since [Order] is a sealed hierarchy with three subtypes, this document uses
 * a discriminator field [orderType] plus mutually-exclusive optional fields
 * to flatten the hierarchy into a single MongoDB collection.
 *
 * @property id The unique identifier for the order document. This is the primary key.
 * @property orderType Discriminator indicating which [Order] subtype this document represents.
 * @property restaurantId The identifier of the restaurant the order belongs to.
 * @property customerId The identifier of the customer who placed the order.
 * @property status The current status of the order (e.g., "PENDING", "PREPARING").
 * @property items The list of items embedded within the order.
 * @property deliveryInfo Populated only when [orderType] is DELIVERY.
 * @property tableInfo Populated only when [orderType] is DINE_IN.
 * @property takeawayInfo Populated only when [orderType] is TAKEAWAY.
 */
@MappedEntity
data class OrderDocument(
  @field:Id
  val id: String,
  val orderType: String,
  val restaurantId: String,
  val customerId: String,
  val status: String,
  val items: List<OrderItemDocument>,
  val deliveryInfo: DeliveryInfoDocument? = null,
  val tableInfo: TableInfoDocument? = null,
  val takeawayInfo: TakeawayInfoDocument? = null,
)

/**
 * Embedded document representing a single item within an order.
 */
@Serdeable
data class OrderItemDocument(
  val menuItemId: String,
  val quantity: Int,
)

/**
 * Embedded document representing delivery information for a delivery order.
 */
@Serdeable
data class DeliveryInfoDocument(
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)

/**
 * Embedded document representing table information for a dine-in order.
 */
@Serdeable
data class TableInfoDocument(
  val tableNumber: Int,
  val numberOfGuests: Int,
)

/**
 * Embedded document representing takeaway information for a takeaway order.
 */
@Serdeable
data class TakeawayInfoDocument(
  val pickupTime: Long,
  val customerName: String,
)
