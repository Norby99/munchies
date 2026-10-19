package com.munchies.order.infrastructure.adapter.outbound.mongo.document

/**
 * Schema of a document of the order event store collection.
 */
object OrderEventDocument {
  const val COLLECTION = "order_events"

  const val AGGREGATE_TYPE = "aggregateType"
  const val AGGREGATE_ID = "aggregateId"
  const val SEQUENCE = "sequence"
  const val EVENT_TYPE = "eventType"
  const val EVENT_VERSION = "eventVersion"
  const val PAYLOAD = "payload"
  const val OCCURRED_AT = "occurredAt"

  const val ORDER_AGGREGATE_TYPE = "Order"
}
