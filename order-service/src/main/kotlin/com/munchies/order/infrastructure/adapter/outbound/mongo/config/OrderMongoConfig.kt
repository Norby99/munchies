package com.munchies.order.infrastructure.adapter.outbound.mongo.config

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.bson.Document

/**
 * Wiring of the two MongoDB collections of order-service, both living in the service's own
 * database (the one named in `mongodb.uri`):
 * - [OrderEventDocument.COLLECTION]: the append-only event store (command side);
 * - [OrderViewDocument.COLLECTION]: the order read model (CQRS query side).
 *
 * Indexes are created idempotently when the collections are first requested.
 */
@Factory
@Requires(env = ["prod"])
class OrderMongoConfig(
  private val client: MongoClient,
  @Value("\${mongodb.uri}") private val uri: String,
) {

  private val databaseName: String
    get() = ConnectionString(uri).database ?: DEFAULT_DATABASE

  /**
   * The event store collection. The unique index on `(aggregateId, sequence)` is what enforces
   * optimistic concurrency: two writers appending the same sequence to the same stream cannot
   * both succeed.
   */
  @Singleton
  @Named(OrderEventDocument.COLLECTION)
  fun orderEvents(): MongoCollection<Document> =
    client.getDatabase(databaseName).getCollection(OrderEventDocument.COLLECTION).apply {
      createIndex(
        Indexes.ascending(OrderEventDocument.AGGREGATE_ID, OrderEventDocument.SEQUENCE),
        IndexOptions().unique(true),
      )
    }

  /**
   * The read model collection, indexed on the fields the query side filters by.
   */
  @Singleton
  @Named(OrderViewDocument.COLLECTION)
  fun orderViews(): MongoCollection<Document> =
    client.getDatabase(databaseName).getCollection(OrderViewDocument.COLLECTION).apply {
      createIndex(Indexes.ascending(OrderViewDocument.RESTAURANT_ID, OrderViewDocument.STATUS))
      createIndex(Indexes.ascending(OrderViewDocument.CUSTOMER_ID, OrderViewDocument.STATUS))
      createIndex(Indexes.ascending(OrderViewDocument.STATUS))
    }

  private companion object {
    const val DEFAULT_DATABASE = "orders"
  }
}
