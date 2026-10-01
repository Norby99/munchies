package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.view.OrderView
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderViewDocumentFactory.toDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderViewDocumentFactory.toNullableView
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.bson.Document

/**
 * MongoDB implementation of the [OrderViewRepository] port (CQRS query side), backed by the
 * [OrderViewDocument.COLLECTION] collection, separate from the event store.
 */
@Singleton
@Requires(env = ["prod"])
class MongoOrderViewRepository(
  @Named(OrderViewDocument.COLLECTION) private val collection: MongoCollection<Document>,
) : OrderViewRepository {

  override fun findById(orderId: OrderId): OrderView? = collection
    .find(Filters.eq(OrderViewDocument.ID, orderId.value))
    .first()
    ?.toNullableView()

  override fun findBy(
    restaurantId: RestaurantId?,
    customerId: CustomerId?,
    status: OrderStatus?,
  ): List<OrderView> {
    val filters = listOfNotNull(
      restaurantId?.let { Filters.eq(OrderViewDocument.RESTAURANT_ID, it.value) },
      customerId?.let { Filters.eq(OrderViewDocument.CUSTOMER_ID, it.value) },
      status?.let { Filters.eq(OrderViewDocument.STATUS, it.name) },
    )
    val query = if (filters.isEmpty()) Filters.empty() else Filters.and(filters)
    return collection.find(query)
      .sort(Sorts.ascending(OrderViewDocument.PLACED_AT))
      .mapNotNull { it.toNullableView() }
  }

  override fun save(view: OrderView) {
    collection.replaceOne(
      Filters.eq(OrderViewDocument.ID, view.orderId.value),
      view.toDocument(),
      ReplaceOptions().upsert(true),
    )
  }
}
