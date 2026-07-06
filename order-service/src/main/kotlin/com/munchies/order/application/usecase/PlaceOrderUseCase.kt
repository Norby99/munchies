package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.domain.factory.OrderCreationResult
import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto

class PlaceOrderUseCase(
  private val repository: OrderRepository,
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

    return when (creationResult) {
      is OrderCreationResult.Failure.EmptyItems -> PlaceOrder.Result.Failure.EmptyItems
      is OrderCreationResult.Failure.InvalidItemQuantity ->
        PlaceOrder.Result.Failure.InvalidItemQuantity
      is OrderCreationResult.Failure.InvalidDate -> PlaceOrder.Result.Failure.InvalidDate
      is OrderCreationResult.Success -> {
        repository.save(creationResult.order)
        PlaceOrder.Result.Success(creationResult.order.toDto())
      }
    }
  }
}
