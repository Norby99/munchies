package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderDetails
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.BELL_NAME
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.CUSTOMER_NAME
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.CUSTOMER_PHONE
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.DELIVERY
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.DELIVERY_ADDRESS
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.DETAILS_TYPE
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.DINE_IN
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.ESTIMATED_DELIVERY_TIME
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.MENU_ITEM_ID
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.NUMBER_OF_GUESTS
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.PICKUP_TIME
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.QUANTITY
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.TABLE_NUMBER
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderSubDocument.TAKEAWAY
import org.bson.Document

/**
 * Mapping of the value objects embedded both in event payloads and in the read model
 * (order items and order details) to and from BSON sub-documents.
 */
object OrderSubDocumentFactory {

  fun List<OrderItem>.toItemsDocument(): List<Document> = map {
    Document(MENU_ITEM_ID, it.menuItemId.value).append(QUANTITY, it.quantity)
  }

  fun Document.readItems(key: String): List<OrderItem> = getList(key, Document::class.java).map {
    OrderItem(MenuItemId(it.getString(MENU_ITEM_ID)), it.getInteger(QUANTITY))
  }

  fun DeliveryInfo.toDocument(): Document = Document(DETAILS_TYPE, DELIVERY)
    .append(ESTIMATED_DELIVERY_TIME, estimatedDeliveryTime)
    .append(DELIVERY_ADDRESS, deliveryAddress)
    .append(BELL_NAME, bellName)
    .append(CUSTOMER_PHONE, customerPhone)

  fun TakeawayInfo.toDocument(): Document = Document(DETAILS_TYPE, TAKEAWAY)
    .append(PICKUP_TIME, pickupTime)
    .append(CUSTOMER_NAME, customerName)

  fun TableInfo.toDocument(): Document = Document(DETAILS_TYPE, DINE_IN)
    .append(TABLE_NUMBER, tableNumber)
    .append(NUMBER_OF_GUESTS, numberOfGuests)

  fun OrderDetails.toDetailsDocument(): Document = when (this) {
    is DeliveryInfo -> toDocument()
    is TakeawayInfo -> toDocument()
    is TableInfo -> toDocument()
  }

  fun Document.toDeliveryInfo(): DeliveryInfo = DeliveryInfo(
    estimatedDeliveryTime = getLong(ESTIMATED_DELIVERY_TIME),
    deliveryAddress = getString(DELIVERY_ADDRESS),
    bellName = getString(BELL_NAME),
    customerPhone = getString(CUSTOMER_PHONE),
  )

  fun Document.toTakeawayInfo(): TakeawayInfo = TakeawayInfo(
    pickupTime = getLong(PICKUP_TIME),
    customerName = getString(CUSTOMER_NAME),
  )

  fun Document.toTableInfo(): TableInfo = TableInfo(
    tableNumber = getInteger(TABLE_NUMBER),
    numberOfGuests = getInteger(NUMBER_OF_GUESTS),
  )

  /**
   * @throws IllegalStateException if the discriminator is unknown.
   */
  fun Document.toOrderDetails(): OrderDetails = when (val type = getString(DETAILS_TYPE)) {
    DELIVERY -> toDeliveryInfo()
    TAKEAWAY -> toTakeawayInfo()
    DINE_IN -> toTableInfo()
    else -> error("Unknown order details type: $type")
  }
}
