package com.munchies.order.infrastructure.adapter.outbound.mongo.document

/**
 * Schema of a document of the order read model collection (CQRS query side).
 */
object OrderViewDocument {
  const val COLLECTION = "order_views"

  const val ID = "_id"
  const val RESTAURANT_ID = "restaurantId"
  const val CUSTOMER_ID = "customerId"
  const val STATUS = "status"
  const val ITEMS = "items"
  const val PAYED = "payed"
  const val DETAILS = "details"
  const val PLACED_AT = "placedAt"
  const val LAST_UPDATED_AT = "lastUpdatedAt"
  const val VERSION = "version"
}

/**
 * Field names of the embedded sub-documents shared by the event payloads and the read model.
 */
object OrderSubDocument {
  const val MENU_ITEM_ID = "menuItemId"
  const val QUANTITY = "quantity"

  const val DETAILS_TYPE = "type"
  const val DELIVERY = "DELIVERY"
  const val TAKEAWAY = "TAKEAWAY"
  const val DINE_IN = "DINE_IN"

  const val ESTIMATED_DELIVERY_TIME = "estimatedDeliveryTime"
  const val DELIVERY_ADDRESS = "deliveryAddress"
  const val BELL_NAME = "bellName"
  const val CUSTOMER_PHONE = "customerPhone"
  const val PICKUP_TIME = "pickupTime"
  const val CUSTOMER_NAME = "customerName"
  const val TABLE_NUMBER = "tableNumber"
  const val NUMBER_OF_GUESTS = "numberOfGuests"
}
