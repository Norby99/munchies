package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateOrderItemsCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.Order.ItemsValidationError
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.port.OrderEventStore
import org.slf4j.LoggerFactory

/**
 * Use case implementation for updating the items of an existing order.
 *
 * This class handles the business logic for modifying the items associated with an order, by
 * appending an [com.munchies.order.domain.model.event.OrderItemsUpdated] event to the order stream.
 *
 * @property eventStore The event store holding the order streams.
 */
class UpdateOrderItemsUseCase(
  private val eventStore: OrderEventStore,
) : UpdateOrderItems {

  override fun execute(command: UpdateOrderItemsCommand): UpdateOrderItems.Result {
    val (order, version) = eventStore.loadOrder(command.orderId) ?: return OrderNotFound

    if (order.customerId != command.customerId) return Unauthorized

    val items = command.items.map { OrderItem(it.menuItemId, it.quantity) }
    return when (val result = order.updateItems(items)) {
      is Order.UpdateResult.Failure.InvalidItems -> when (result.error) {
        ItemsValidationError.EmptyItems -> EmptyItems
        ItemsValidationError.InvalidItemQuantity -> EmptyItems
      }
      is Order.UpdateResult.Success -> {
        eventStore.append(order.id, version, result.events)
        logger.info("Order {} items updated", order.id.value)
        Success
      }
    }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(UpdateOrderItemsUseCase::class.java)
  }
}
