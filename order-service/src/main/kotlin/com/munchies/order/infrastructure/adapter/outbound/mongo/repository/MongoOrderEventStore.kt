package com.munchies.order.infrastructure.adapter.outbound.mongo.repository

import com.mongodb.ErrorCategory
import com.mongodb.MongoBulkWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.port.ConcurrentOrderModificationException
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.infrastructure.adapter.inbound.event.OrderEventsAppended
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderEventDocumentFactory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.bson.Document

/**
 * MongoDB implementation of the [OrderEventStore] port, backed by the append-only
 * [OrderEventDocument.COLLECTION] collection.
 *
 * After every successful append it publishes an [OrderEventsAppended] application event, which
 * feeds the CQRS projection.
 */
@Singleton
@Requires(env = ["prod"])
class MongoOrderEventStore(
  @Named(OrderEventDocument.COLLECTION) private val collection: MongoCollection<Document>,
  private val appendedPublisher: ApplicationEventPublisher<OrderEventsAppended>,
) : OrderEventStore {

  override fun load(orderId: OrderId): List<OrderEvent> = collection
    .find(Filters.eq(OrderEventDocument.AGGREGATE_ID, orderId.value))
    .sort(Sorts.ascending(OrderEventDocument.SEQUENCE))
    .toList()
    .map(OrderEventDocumentFactory::toEvent)

  override fun append(orderId: OrderId, expectedVersion: Long, events: List<OrderEvent>) {
    if (events.isEmpty()) return
    val documents = events.mapIndexed { index, event ->
      OrderEventDocumentFactory.toDocument(event, expectedVersion + index)
    }
    try {
      // Ordered insert: the first document already carries the conflicting sequence when the
      // stream moved on, so a concurrent append is rejected before anything is written.
      collection.insertMany(documents)
    } catch (e: MongoBulkWriteException) {
      if (e.isDuplicateKey()) throw ConcurrentOrderModificationException(orderId)
      throw e
    }
    appendedPublisher.publishEvent(OrderEventsAppended(orderId, expectedVersion, events))
  }

  private fun MongoBulkWriteException.isDuplicateKey(): Boolean =
    writeErrors.any { ErrorCategory.fromErrorCode(it.code) == ErrorCategory.DUPLICATE_KEY }
}
