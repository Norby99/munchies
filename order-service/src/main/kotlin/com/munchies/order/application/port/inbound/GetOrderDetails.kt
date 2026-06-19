package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.infrastructure.adapter.dto.OrderDto

/**
 * Use case for retrieving the details of a specific order. This interface defines the contract
 * for fetching
 */
interface GetOrderDetails {

  /** Executes the use case to retrieve order details based on the provided command. The command
   * contains the necessary information to identify the order, such as the order ID and user ID.
   * The result of the execution is a sealed interface that can represent either a successful
   * retrieval of the order details or a failure scenario where the order was not found.
   *
   * @param command The command containing the information needed to retrieve the order details.
   * @return A Result sealed interface representing either success with the order details or
   * failure if the order was not found.
   */
  fun execute(command: GetOrderDetailsCommand): Result

  /**
   * Represents the result of the get order details operation. It can be either a success with the
   * order details or a failure indicating that the order was not found.
   * - `Success`: Contains the `OrderDto` with the details of the retrieved order.
   * - `Failure`: Indicates that the order was not found in the system.
   *  - `OrderNotFound`: No order exists with the provided identifier.
   */
  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Result
    }
  }
}
