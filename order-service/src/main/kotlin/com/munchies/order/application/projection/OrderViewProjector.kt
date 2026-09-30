package com.munchies.order.application.projection

import com.munchies.order.domain.model.OrderEvent
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderPlaced
import com.munchies.order.domain.model.OrderView
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderViewRepository

/**
 * Projector of the CQRS query side: folds events that were just appended to the event store
 * into the [OrderView] read model.
 *
 * It is the only component allowed to write the read model.
 *
 * @property views The repository of the order read model.
 * @property eventStore The event store, used to rebuild a view that is out of sync.
 */
class OrderViewProjector(
  private val views: OrderViewRepository,
  private val eventStore: OrderEventStore,
) {

  /**
   * Projects a batch of events just appended to the stream of [orderId].
   *
   * @param orderId The order whose stream was appended to.
   * @param firstSequence The stream sequence of the first event in [events]; the following
   * events have consecutive sequences.
   * @param events The appended events, in sequence order.
   * @return An [Outcome] describing how the read model was updated.
   */
  fun project(orderId: OrderId, firstSequence: Long, events: List<OrderEvent>): Outcome {
    val current = views.findById(orderId)
    val projectedVersion = current?.version ?: 0L
    val batchEnd = firstSequence + events.size

    return when {
      batchEnd <= projectedVersion -> Outcome.AlreadyUpToDate
      firstSequence > projectedVersion -> rebuild(orderId)
      else -> {
        // Skip the leading part of the batch that is already contained in the view.
        val pending = events.drop((projectedVersion - firstSequence).toInt())
        val projected = fold(current, pending)
        if (projected == null) {
          rebuild(orderId)
        } else {
          views.save(projected)
          Outcome.Projected
        }
      }
    }
  }

  /**
   * Folds [pending] into [current], or creates the view from [pending] when there is no view
   * yet. Returns `null` if a new view cannot be started because [pending] does not begin with
   * [OrderPlaced].
   */
  private fun fold(current: OrderView?, pending: List<OrderEvent>): OrderView? {
    if (current != null) return pending.fold(current) { view, event -> view.apply(event) }
    val placed = pending.first() as? OrderPlaced ?: return null
    return pending.drop(1).fold(OrderView.from(placed)) { view, event -> view.apply(event) }
  }

  private fun rebuild(orderId: OrderId): Outcome {
    val view = OrderView.fromHistory(eventStore.load(orderId)) ?: return Outcome.StreamNotFound
    views.save(view)
    return Outcome.Rebuilt
  }

  /**
   * Outcome of a projection step.
   *
   * - `Projected`: the new events were folded into the view (or started a new one).
   * - `Rebuilt`: the view was missing or out of sync and was rebuilt from the whole stream.
   * - `AlreadyUpToDate`: every event had already been projected (duplicate delivery).
   * - `StreamNotFound`: a rebuild was needed but the event store holds no valid stream.
   */
  sealed interface Outcome {
    data object Projected : Outcome
    data object Rebuilt : Outcome
    data object AlreadyUpToDate : Outcome
    data object StreamNotFound : Outcome
  }
}
