package com.munchies.order.domain.model

import com.munchies.commons.Entity

/**
 * Event-sourced aggregate root representing an order, which can be of different types
 * (Delivery, Takeaway, DineIn).
 *
 * The aggregate is never persisted as a current-state document: its state is rebuilt by
 * replaying its [OrderEvent] stream (see [from] and [apply]). Business operations are split in
 * two kinds of methods, following the Event Sourcing pattern:
 * - *process* methods ([nextStatus], [pay], [cancel], [updateItems].
 * - [apply] turns one event, i.e. a fact that already happened, into the next state. It never
 *   fails and performs no validation.
 *
 * @property id The unique identifier for the order.
 * @property restaurantId The unique identifier for the restaurant associated with the order.
 * @property customerId The unique identifier for the customer who placed the order.
 * @property status The current status of the order.
 * @property items The list of items included in the order.
 * @property payed Whether the order has been paid.
 */
sealed class Order(
  override val id: OrderId,
  open val restaurantId: RestaurantId,
  open val customerId: CustomerId,
  open val status: OrderStatus,
  open val items: List<OrderItem>,
  open val payed: Boolean,
) : Entity<OrderId>(id) {

  // ---------------------------------------------------------------------------------------------
  // Process (command) methods: validate and return events.
  // ---------------------------------------------------------------------------------------------

  /**
   * Advances the status of the order to the next logical state of its workflow.
   *
   * @return An [AdvanceStatusResult] carrying an [OrderStatusAdvanced] event on success.
   */
  fun nextStatus(): AdvanceStatusResult {
    val next = successorOf(status) ?: return AdvanceStatusResult.Failure.InvalidTransition
    return AdvanceStatusResult.Success(listOf(OrderStatusAdvanced(id, status, next)))
  }

  /**
   * Processes the payment for the order.
   *
   * @return A [PayResult] carrying an [OrderPaid] event on success.
   */
  fun pay(): PayResult {
    if (payed) return PayResult.Failure.AlreadyPaid
    return PayResult.Success(listOf(OrderPaid(id)))
  }

  /**
   * Cancels the order if it is in a cancellable state.
   *
   * @return A [CancelResult] carrying an [OrderCancelled] event on success.
   */
  fun cancel(): CancelResult {
    if (status != OrderStatus.PENDING) return CancelResult.Failure.InvalidTransition
    return CancelResult.Success(listOf(OrderCancelled(id)))
  }

  /**
   * Replaces the items in the order after validating them.
   *
   * @param newItems The new list of [OrderItem] for the order.
   * @return An [UpdateResult] carrying an [OrderItemsUpdated] event on success.
   */
  fun updateItems(newItems: List<OrderItem>): UpdateResult {
    val validationError = validateItems(newItems)
    if (validationError != null) return UpdateResult.Failure.InvalidItems(validationError)
    return UpdateResult.Success(listOf(OrderItemsUpdated(id, newItems)))
  }

  /**
   * Returns the status following [status] in this order type's workflow, or `null` if the
   * order cannot advance any further from it.
   */
  protected abstract fun successorOf(status: OrderStatus): OrderStatus?

  // ---------------------------------------------------------------------------------------------
  // Apply methods: turn an already happened event into the next state. They never fail.
  // ---------------------------------------------------------------------------------------------

  /**
   * Applies [event] to this order and returns the resulting state.
   *
   * Events that do not concern this order subtype and [OrderPlaced] (handled by [from])
   * leave the state unchanged: an event is a fact.
   * So applying it must never throw.
   */
  fun apply(event: OrderEvent): Order = when (event) {
    is OrderPlaced -> this
    is OrderStatusAdvanced -> copyWithStatus(event.newStatus)
    is OrderCancelled -> copyWithStatus(OrderStatus.CANCELLED)
    is OrderPaid -> copyWithPayed(true)
    is OrderItemsUpdated -> copyWithItems(event.items)
    is DeliveryInfoUpdated -> (this as? DeliveryOrder)?.copy(deliveryInfo = event.deliveryInfo)
      ?: this
    is TakeawayInfoUpdated -> (this as? TakeawayOrder)?.copy(takeawayInfo = event.takeawayInfo)
      ?: this
  }

  /**
   * Applies [events] in order and returns the resulting state.
   */
  fun applyAll(events: List<OrderEvent>): Order = events.fold(this) { order, event ->
    order.apply(event)
  }

  protected abstract fun copyWithStatus(status: OrderStatus): Order

  protected abstract fun copyWithItems(items: List<OrderItem>): Order

  protected abstract fun copyWithPayed(payed: Boolean): Order

  companion object {
    private const val DEFAULT_PAYED = false

    /**
     * Creates the initial state of an order from its creation event. The subtype is decided by
     * the runtime type of [OrderPlaced.details].
     */
    fun from(event: OrderPlaced): Order = when (val details = event.details) {
      is DeliveryInfo -> DeliveryOrder(
        event.orderId,
        event.restaurantId,
        event.customerId,
        OrderStatus.PENDING,
        event.items,
        DEFAULT_PAYED,
        details,
      )
      is TakeawayInfo -> TakeawayOrder(
        event.orderId,
        event.restaurantId,
        event.customerId,
        OrderStatus.PENDING,
        event.items,
        DEFAULT_PAYED,
        details,
      )
      is TableInfo -> DineInOrder(
        event.orderId,
        event.restaurantId,
        event.customerId,
        OrderStatus.PENDING,
        event.items,
        DEFAULT_PAYED,
        details,
      )
    }

    /**
     * Validates the list of order items.
     *
     * @param items The list of [OrderItem] to validate.
     * @return An [ItemsValidationError] if validation fails, or null if validation succeeds.
     */
    fun validateItems(items: List<OrderItem>): ItemsValidationError? {
      if (items.isEmpty()) return ItemsValidationError.EmptyItems
      if (items.any { !it.isValid() }) return ItemsValidationError.InvalidItemQuantity
      return null
    }
  }

  /**
   * Represents the result of attempting to advance the status of an order.
   */
  sealed interface AdvanceStatusResult {
    data class Success(val events: List<OrderEvent>) : AdvanceStatusResult
    sealed interface Failure : AdvanceStatusResult {
      data object InvalidTransition : Failure
    }
  }

  /**
   * Represents the result of attempting to pay for an order.
   */
  sealed interface PayResult {
    data class Success(val events: List<OrderEvent>) : PayResult
    sealed interface Failure : PayResult {
      data object AlreadyPaid : Failure
    }
  }

  /**
   * Represents the result of attempting to update the items in an order.
   */
  sealed interface UpdateResult {
    data class Success(val events: List<OrderEvent>) : UpdateResult
    sealed interface Failure : UpdateResult {
      data class InvalidItems(val error: ItemsValidationError) : Failure
    }
  }

  /**
   * Represents the result of attempting to cancel an order.
   */
  sealed interface CancelResult {
    data class Success(val events: List<OrderEvent>) : CancelResult
    sealed interface Failure : CancelResult {
      data object InvalidTransition : Failure
    }
  }

  /**
   * Represents possible validation errors for order items.
   */
  enum class ItemsValidationError {
    EmptyItems,
    InvalidItemQuantity,
  }
}

/**
 * Represents the unique identifier for a restaurant.
 *
 * @property value The string value of the restaurant ID.
 */
@JvmInline value class RestaurantId(val value: String)

/**
 * Represents the unique identifier for a customer.
 *
 * @property value The string value of the customer ID.
 */
@JvmInline value class CustomerId(val value: String)
