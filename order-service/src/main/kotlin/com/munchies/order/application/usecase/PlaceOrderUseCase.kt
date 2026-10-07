package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import org.slf4j.LoggerFactory

/**
 * Use case implementation for placing an order.
 *
 * Validates the new order through [OrderFactory], and starts the order's stream in the
 * event store with it.
 *
 * The returned DTO is built from the command-side state rebuilt from that very event,
 * not from the read model, so the caller always sees its own write.
 *
 * @property eventStore The event store holding the order streams.
 */
class PlaceOrderUseCase(
  private val eventStore: OrderEventStore,
) : PlaceOrder {

  override fun execute(command: PlaceOrderCommand): PlaceOrder.Result {
    val id = OrderId()
    val restaurantId = command.restaurantId
    val customerId = command.customerId
    val items = command.items

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
