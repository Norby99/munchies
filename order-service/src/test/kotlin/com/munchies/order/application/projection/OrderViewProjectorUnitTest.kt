package com.munchies.order.application.projection

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.view.OrderView
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderViewProjectorUnitTest {

  private val views = mockk<OrderViewRepository>(relaxed = false)
  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val projector = OrderViewProjector(views, eventStore)

  /** A stream of 3 events: OrderPlaced, OrderStatusAdvanced (to PREPARING), OrderPaid. */
  private val history = createDeliveryOrder(status = OrderStatus.PREPARING)
    .copy(payed = true)
    .asHistory()

  @BeforeEach
  fun setUp() {
    every { views.save(any()) } just Runs
  }

  @Test
  fun `project should create the view from the first batch of a new order`() {
    every { views.findById(defaultOrderId) } returns null

    val outcome = projector.project(defaultOrderId, 0, history)

    outcome shouldBeEqual OrderViewProjector.Outcome.Projected
    verify(exactly = 1) { views.save(OrderView.fromHistory(history)!!) }
    verify(exactly = 0) { eventStore.load(any()) }
  }

  @Test
  fun `project should fold only the new events into an existing view`() {
    every { views.findById(defaultOrderId) } returns OrderView.fromHistory(history.take(2))

    val outcome = projector.project(defaultOrderId, 2, history.drop(2))

    outcome shouldBeEqual OrderViewProjector.Outcome.Projected
    verify(exactly = 1) {
      views.save(
        withArg {
          it.payed shouldBeEqual true
          it.version shouldBeEqual 3L
        },
      )
    }
  }

  @Test
  fun `project should skip events already contained in the view`() {
    every { views.findById(defaultOrderId) } returns OrderView.fromHistory(history)

    val outcome = projector.project(defaultOrderId, 1, history.drop(1))

    outcome shouldBeEqual OrderViewProjector.Outcome.AlreadyUpToDate
    verify(exactly = 0) { views.save(any()) }
  }

  @Test
  fun `project should only apply the unseen tail of a partially projected batch`() {
    every { views.findById(defaultOrderId) } returns OrderView.fromHistory(history.take(2))

    projector.project(defaultOrderId, 1, history.drop(1))

    verify(exactly = 1) {
      views.save(
        withArg {
          it.status shouldBeEqual OrderStatus.PREPARING
          it.version shouldBeEqual 3L
        },
      )
    }
  }

  @Test
  fun `project should rebuild the view from the event store when a gap is detected`() {
    val fullHistory = history + OrderPaid(defaultOrderId)
    every { views.findById(defaultOrderId) } returns OrderView.fromHistory(history.take(1))
    every { eventStore.load(defaultOrderId) } returns fullHistory

    val outcome = projector.project(defaultOrderId, 3, fullHistory.drop(3))

    outcome shouldBeEqual OrderViewProjector.Outcome.Rebuilt
    verify(exactly = 1) { views.save(OrderView.fromHistory(fullHistory)!!) }
  }

  @Test
  fun `project should return StreamNotFound when a rebuild finds no valid stream`() {
    every { views.findById(defaultOrderId) } returns null
    every { eventStore.load(defaultOrderId) } returns emptyList()

    val outcome = projector.project(defaultOrderId, 2, listOf(OrderPaid(defaultOrderId)))

    outcome shouldBeEqual OrderViewProjector.Outcome.StreamNotFound
    verify(exactly = 0) { views.save(any()) }
  }
}
