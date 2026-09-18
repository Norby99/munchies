# Event sourcing in Kotlin + Micronaut

Worked example for an event-sourced `Ticket` aggregate (a kitchen ticket derived from an order), following
`order-service`'s package layout (`com.munchies.<service>.*`). Adapt names/fields to whatever aggregate you're
actually implementing.

## 1. Events — `domain/model/TicketEvent.kt`

```kotlin
package com.munchies.kitchen.domain.model

import java.time.Instant

sealed interface TicketEvent {
    val ticketId: String
    val occurredAt: Instant
}

data class TicketCreated(
    override val ticketId: String,
    val orderId: String,
    val items: List<TicketItem>,
    override val occurredAt: Instant = Instant.now(),
) : TicketEvent

data class TicketAccepted(
    override val ticketId: String,
    val readyBy: Instant,
    override val occurredAt: Instant = Instant.now(),
) : TicketEvent

data class TicketCompleted(
    override val ticketId: String,
    override val occurredAt: Instant = Instant.now(),
) : TicketEvent
```

A `sealed interface` gives exhaustive `when` handling in `apply()` for free — the compiler flags a missing branch
the moment a new event type is added.

## 2. Aggregate — `domain/model/Ticket.kt`

```kotlin
package com.munchies.kitchen.domain.model

import java.time.Instant

class Ticket private constructor() {
    lateinit var ticketId: String
        private set
    lateinit var status: TicketStatus
        private set
    var readyBy: Instant? = null
        private set

    companion object {
        // Used for replay: an empty instance events get applied onto.
        fun empty(): Ticket = Ticket()

        // Used for first creation: process() has no prior state to validate against.
        fun create(ticketId: String, orderId: String, items: List<TicketItem>): List<TicketEvent> =
            listOf(TicketCreated(ticketId, orderId, items))
    }

    // process(): validates against current state, returns events, never mutates.
    fun accept(readyBy: Instant): List<TicketEvent> {
        check(status == TicketStatus.CREATED) { "Ticket $ticketId cannot be accepted from status $status" }
        return listOf(TicketAccepted(ticketId, readyBy))
    }

    fun complete(): List<TicketEvent> {
        check(status == TicketStatus.ACCEPTED) { "Ticket $ticketId cannot be completed from status $status" }
        return listOf(TicketCompleted(ticketId))
    }

    // apply(): mutates state from a fact that already happened. Cannot fail.
    fun apply(event: TicketEvent) {
        when (event) {
            is TicketCreated -> {
                ticketId = event.ticketId
                status = TicketStatus.CREATED
            }
            is TicketAccepted -> {
                status = TicketStatus.ACCEPTED
                readyBy = event.readyBy
            }
            is TicketCompleted -> status = TicketStatus.COMPLETED
        }
    }
}

enum class TicketStatus { CREATED, ACCEPTED, COMPLETED }
```

## 3. Event store port — `domain/port/TicketEventStore.kt`

```kotlin
package com.munchies.kitchen.domain.port

import com.munchies.kitchen.domain.model.TicketEvent

interface TicketEventStore {
    /** Appends [events] starting at [expectedSequence]; throws on a concurrent append (sequence conflict). */
    suspend fun append(ticketId: String, expectedSequence: Long, events: List<TicketEvent>)

    /** Loads the full ordered event stream for [ticketId]. Empty list if the aggregate doesn't exist yet. */
    suspend fun load(ticketId: String): List<TicketEvent>
}
```

Keep this separate from any cross-service publisher port (below) — the store is the source of truth, publication is
a side effect triggered after a successful append.

## 4. Use case — `application/usecase/AcceptTicketUseCase.kt`

```kotlin
package com.munchies.kitchen.application.usecase

import com.munchies.kitchen.application.port.inbound.AcceptTicket
import com.munchies.kitchen.application.port.inbound.command.AcceptTicketCommand
import com.munchies.kitchen.domain.model.Ticket
import com.munchies.kitchen.domain.port.TicketEventStore
import jakarta.inject.Singleton

@Singleton
class AcceptTicketUseCase(
    private val eventStore: TicketEventStore,
) : AcceptTicket {
    override suspend fun accept(command: AcceptTicketCommand) {
        val history = eventStore.load(command.ticketId)
        check(history.isNotEmpty()) { "Ticket ${command.ticketId} not found" }

        val ticket = Ticket.empty()
        history.forEach(ticket::apply)

        val newEvents = ticket.accept(command.readyBy)
        newEvents.forEach(ticket::apply)

        eventStore.append(command.ticketId, expectedSequence = history.size.toLong(), events = newEvents)
    }
}
```

Loading, replaying, validating, and appending all happen in the use case — the aggregate itself never touches
persistence.

## 5. Mongo adapter — `infrastructure/adapter/outbound/mongo/`

`document/TicketEventDocument.kt`:

```kotlin
package com.munchies.kitchen.infrastructure.adapter.outbound.mongo.document

import java.time.Instant

data class TicketEventDocument(
    val aggregateId: String,
    val sequence: Long,
    val eventType: String,
    val eventVersion: Int,
    val payload: Map<String, Any?>,
    val occurredAt: Instant,
)
```

`factory/TicketEventDocumentFactory.kt` maps `TicketEvent <-> TicketEventDocument` (serialize/deserialize `payload`,
switch on `eventType` to pick the right event class — this is also where upcasting old `eventVersion` payloads to
the current shape happens, see the pitfalls section of `SKILL.md`).

`repository/MongoTicketEventStore.kt`:

```kotlin
package com.munchies.kitchen.infrastructure.adapter.outbound.mongo.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.munchies.kitchen.domain.model.TicketEvent
import com.munchies.kitchen.domain.port.TicketEventStore
import com.munchies.kitchen.infrastructure.adapter.outbound.mongo.factory.TicketEventDocumentFactory
import jakarta.inject.Singleton
import org.bson.conversions.Bson

@Singleton
class MongoTicketEventStore(
    private val collection: com.mongodb.reactivestreams.client.MongoCollection<org.bson.Document>,
    private val factory: TicketEventDocumentFactory,
) : TicketEventStore {

    override suspend fun append(ticketId: String, expectedSequence: Long, events: List<TicketEvent>) {
        val docs = events.mapIndexed { i, event ->
            factory.toDocument(ticketId, sequence = expectedSequence + i, event = event)
        }
        // Unique index on (aggregateId, sequence) turns a concurrent append into a duplicate-key error here.
        collection.insertMany(docs)
    }

    override suspend fun load(ticketId: String): List<TicketEvent> {
        val filter: Bson = Filters.eq("aggregateId", ticketId)
        return collection.find(filter)
            .sort(Sorts.ascending("sequence"))
            .map(factory::toEvent)
            .toList()
    }
}
```

Create the unique index `(aggregateId, sequence)` in the collection's Mongo config/migration, same place
`order-service` sets up its own collection indexes.

## Cross-service publication (only if needed)

If another service needs to consume `TicketEvent`s, add a `domain/port/TicketNotificationPublisher.kt` and a
`infrastructure/adapter/outbound/kafka/KafkaTicketEventPublisher.kt` implementing it, following the shape of
`payment-service`'s `KafkaPaymentNotificationPublisher` (see `references/express.md` in this skill, or the file
directly). Call the publisher from the use case, after a successful `append()`, not instead of it.
