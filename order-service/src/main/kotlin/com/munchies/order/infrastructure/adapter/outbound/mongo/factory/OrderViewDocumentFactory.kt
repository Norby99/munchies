package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.view.OrderView
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.readItems
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toDetailsDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toItemsDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toOrderDetails
import org.bson.Document

/**
 * Mapping between the [OrderView] read model and the documents of the read model collection
 * (see [OrderViewDocument] for the schema).
 */
object OrderViewDocumentFactory {

  fun OrderView.toDocument(): Document = Document(OrderViewDocument.ID, orderId.value)
    .append(OrderViewDocument.RESTAURANT_ID, restaurantId.value)
    .append(OrderViewDocument.CUSTOMER_ID, customerId.value)
    .append(OrderViewDocument.STATUS, status.name)
    .append(OrderViewDocument.ITEMS, items.toItemsDocument())
    .append(OrderViewDocument.PAYED, payed)
    .append(OrderViewDocument.DETAILS, details.toDetailsDocument())
    .append(OrderViewDocument.PLACED_AT, placedAt)
    .append(OrderViewDocument.LAST_UPDATED_AT, lastUpdatedAt)
    .append(OrderViewDocument.VERSION, version)

  /**
   * @return The view stored in the document, or `null` if the document is malformed.
   */
  fun Document.toNullableView(): OrderView? = runCatching {
    OrderView(
      orderId = OrderId(getString(OrderViewDocument.ID)),
      restaurantId = RestaurantId(getString(OrderViewDocument.RESTAURANT_ID)),
      customerId = CustomerId(getString(OrderViewDocument.CUSTOMER_ID)),
      status = OrderStatus.valueOf(getString(OrderViewDocument.STATUS)),
      items = readItems(OrderViewDocument.ITEMS),
      payed = getBoolean(OrderViewDocument.PAYED),
      details = get(OrderViewDocument.DETAILS, Document::class.java).toOrderDetails(),
      placedAt = getLong(OrderViewDocument.PLACED_AT),
      lastUpdatedAt = getLong(OrderViewDocument.LAST_UPDATED_AT),
      version = getLong(OrderViewDocument.VERSION),
    )
  }.getOrNull()
}
