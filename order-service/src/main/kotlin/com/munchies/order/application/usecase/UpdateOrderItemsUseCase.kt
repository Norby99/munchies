package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateOrderItemsCommand
import com.munchies.order.application.pricing.PricingResult
import com.munchies.order.application.pricing.checkAndPrice
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.Order.ItemsValidationError
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.OrderEventStore
import org.slf4j.LoggerFactory

/**
 * Use case implementation for updating the items of an existing order.
 *
 * This class handles the business logic for modifying the items associated with an order, by
 * appending an [com.munchies.order.domain.model.event.OrderItemsUpdated] event to the order stream.
 *
 * The new items are priced against the restaurant's current menus, like when the order is
 * placed: the event carries the prices, so the order total always matches its items.
 *
 * @property eventStore The event store holding the order streams.
 * @property menuCatalog The restaurant menus, source of the item prices.
 */
class UpdateOrderItemsUseCase(
  private val eventStore: OrderEventStore,
  private val menuCatalog: MenuCatalog,
) : UpdateOrderItems {

  override fun execute(command: UpdateOrderItemsCommand): UpdateOrderItems.Result {
    val (order, version) = eventStore.loadOrder(command.orderId) ?: return OrderNotFound

    if (order.customerId != command.customerId) return Unauthorized

    // Refuse what the domain would refuse anyway before paying for a call to restaurant-service.
    if (!order.areItemsModifiable) return OrderNotModifiable
    return when (val pricing = menuCatalog.checkAndPrice(order.restaurantId, command.items)) {
      is PricingResult.Priced -> update(order, version, pricing.items)
      is PricingResult.Rejected -> rejected(order, command, pricing)
    }
  }

  private fun rejected(
    order: Order,
    command: UpdateOrderItemsCommand,
    reason: PricingResult.Rejected,
  ): UpdateOrderItems.Result.Failure = when (reason) {
    is PricingResult.EmptyItems, is PricingResult.InvalidItemQuantity -> EmptyItems
    is PricingResult.UnknownItems -> UnknownItems(reason.menuItemIds)
    // The order exists, so a missing restaurant means it was deleted: none of its items is
    // orderable any more.
    is PricingResult.RestaurantNotFound -> UnknownItems(command.items.map { it.menuItemId }.toSet())
    is PricingResult.CatalogUnavailable -> {
      logger.error("Order {} items not updated: menu prices unavailable", order.id.value)
      MenuUnavailable
    }
  }

  private fun update(order: Order, version: Long, items: List<OrderItem>): UpdateOrderItems.Result {
    return when (val result = order.updateItems(items)) {
      is Order.UpdateResult.Failure.NotModifiable -> {
        logger.warn("Order {} items cannot be updated in status {}", order.id.value, order.status)
        OrderNotModifiable
      }
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
