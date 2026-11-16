package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.application.pricing.PricingResult
import com.munchies.order.application.pricing.checkAndPrice
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.TableReservations
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import org.slf4j.LoggerFactory

/**
 * Use case implementation for placing an order.
 *
 * Prices the requested items against the restaurant's menus through [MenuCatalog] (the client
 * never sets prices), validates the new order through [OrderFactory], and starts the order's
 * stream in the event store with it. The prices are stored in the [OrderPlaced][
 * com.munchies.order.domain.model.event.OrderPlaced] event, so the order keeps them even if the
 * menu changes later.
 *
 * The returned DTO is built from the command-side state rebuilt from that very event,
 * not from the read model, so the caller always sees its own write.
 *
 * A dine-in order placed for a table reservation is first resolved through [TableReservations]:
 * the reservation must be an accepted one of the same customer at the same restaurant, and the
 * order is served at its table, for its number of guests.
 *
 * @property eventStore The event store holding the order streams.
 * @property menuCatalog The restaurant menus, source of the item prices.
 * @property tableReservations The table reservations, source of a reserved dine-in order's table.
 */
class PlaceOrderUseCase(
  private val eventStore: OrderEventStore,
  private val menuCatalog: MenuCatalog,
  private val tableReservations: TableReservations,
) : PlaceOrder {

  override fun execute(command: PlaceOrderCommand): PlaceOrder.Result {
    // Resolved before pricing: an order for a reservation that cannot back it is refused
    // whatever its items.
    val resolved = when (command) {
      is PlaceOrderCommand.ReservedDineIn -> when (val table = reservedTable(command)) {
        is ReservedTable.Usable -> command.at(table.info)
        is ReservedTable.Refused -> return table.failure
      }
      else -> command
    }
    return when (val pricing = menuCatalog.checkAndPrice(resolved.restaurantId, resolved.items)) {
      is PricingResult.Priced -> place(resolved, pricing.items)
      is PricingResult.Rejected -> rejected(resolved.restaurantId, pricing)
    }
  }

  /** Outcome of resolving the reservation of a [PlaceOrderCommand.ReservedDineIn]. */
  private sealed interface ReservedTable {
    data class Usable(val info: TableInfo) : ReservedTable
    data class Refused(val failure: PlaceOrder.Result.Failure) : ReservedTable
  }

  private fun reservedTable(command: PlaceOrderCommand.ReservedDineIn): ReservedTable =
    when (val lookup = tableReservations.find(command.reservationId, command.customerId)) {
      is TableReservations.Lookup.NotFound ->
        ReservedTable.Refused(PlaceOrder.Result.Failure.ReservationNotFound)
      is TableReservations.Lookup.Unavailable -> {
        logger.error("Order rejected: table-reservation-service did not return the reservation")
        ReservedTable.Refused(PlaceOrder.Result.Failure.ReservationUnavailable)
      }
      is TableReservations.Lookup.Found -> {
        val reservation = lookup.reservation
        when {
          !reservation.accepted -> notUsable(command, "The reservation is not active")
          reservation.restaurantId != command.restaurantId ->
            notUsable(command, "The reservation was made at another restaurant")
          else -> ReservedTable.Usable(
            TableInfo(
              tableNumber = reservation.tableNumber,
              numberOfGuests = reservation.numberOfGuests,
            ),
          )
        }
      }
    }

  private fun notUsable(
    command: PlaceOrderCommand.ReservedDineIn,
    reason: String,
  ): ReservedTable.Refused {
    logger.warn("Order rejected for reservation {}: {}", command.reservationId, reason)
    return ReservedTable.Refused(PlaceOrder.Result.Failure.ReservationNotUsable(reason))
  }

  /** The dine-in order served at the reservation's table. */
  private fun PlaceOrderCommand.ReservedDineIn.at(table: TableInfo) = PlaceOrderCommand.DineIn(
    restaurantId = restaurantId,
    customerId = customerId,
    items = items,
    tableNumber = table.tableNumber,
    numberOfGuests = table.numberOfGuests,
  )

  private fun rejected(
    restaurantId: RestaurantId,
    reason: PricingResult.Rejected,
  ): PlaceOrder.Result.Failure = when (reason) {
    is PricingResult.EmptyItems -> PlaceOrder.Result.Failure.EmptyItems
    is PricingResult.InvalidItemQuantity -> PlaceOrder.Result.Failure.InvalidItemQuantity
    is PricingResult.RestaurantNotFound -> PlaceOrder.Result.Failure.RestaurantNotFound
    is PricingResult.UnknownItems -> {
      logger.warn(
        "Order rejected for restaurant {}: unknown items {}",
        restaurantId.value,
        reason.menuItemIds.map { it.value },
      )
      PlaceOrder.Result.Failure.UnknownItems(reason.menuItemIds)
    }
    is PricingResult.CatalogUnavailable -> {
      logger.error("Order rejected: restaurant-service did not return the menu prices")
      PlaceOrder.Result.Failure.MenuUnavailable
    }
  }

  private fun place(command: PlaceOrderCommand, items: List<OrderItem>): PlaceOrder.Result {
    val id = OrderId()
    val restaurantId = command.restaurantId
    val customerId = command.customerId

    val creationResult = when (command) {
      is PlaceOrderCommand.Delivery -> OrderFactory.createDelivery(
        id,
        restaurantId,
        customerId,
        items,
        DeliveryInfo(
          deliveryAddress = command.deliveryAddress,
          bellName = command.bellName,
          customerPhone = command.customerPhone,
          estimatedDeliveryTime = command.estimatedDeliveryTime,
        ),
      )
      is PlaceOrderCommand.Takeaway -> OrderFactory.createTakeaway(
        id,
        restaurantId,
        customerId,
        items,
        TakeawayInfo(
          pickupTime = command.pickupTime,
          customerName = command.customerName,
        ),
      )
      is PlaceOrderCommand.DineIn -> OrderFactory.createDineIn(
        id,
        restaurantId,
        customerId,
        items,
        TableInfo(
          tableNumber = command.tableNumber,
          numberOfGuests = command.numberOfGuests,
        ),
      )
      is PlaceOrderCommand.ReservedDineIn ->
        error("A reserved dine-in order is resolved to its table before being placed")
    }

    if (creationResult is OrderCreationResult.Failure) {
      logger.warn(
        "Order rejected for restaurant {}: {}",
        restaurantId.value,
        creationResult::class.simpleName,
      )
    }

    return when (creationResult) {
      is OrderCreationResult.Failure.EmptyItems -> PlaceOrder.Result.Failure.EmptyItems
      is OrderCreationResult.Failure.InvalidItemQuantity ->
        PlaceOrder.Result.Failure.InvalidItemQuantity
      is OrderCreationResult.Failure.InvalidDate -> PlaceOrder.Result.Failure.InvalidDate
      is OrderCreationResult.Success -> {
        val order = checkNotNull(OrderFactory.fromHistory(creationResult.events)) {
          "Order creation events must start with OrderPlaced"
        }
        eventStore.append(id, expectedVersion = 0, events = creationResult.events)
        logger.info(
          "Order {} placed for restaurant {} with {} item(s)",
          id.value,
          restaurantId.value,
          items.size,
        )
        PlaceOrder.Result.Success(order.toDto())
      }
    }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(PlaceOrderUseCase::class.java)
  }
}
