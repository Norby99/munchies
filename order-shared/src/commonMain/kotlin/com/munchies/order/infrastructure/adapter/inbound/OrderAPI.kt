package com.munchies.order.infrastructure.adapter.inbound

/**
 * The OrderAPI interface defines the contract for various order-related operations.
 * It serves as a base interface for specific order-related APIs, each of which handles
 * a different aspect of order management.
 */
sealed interface OrderAPI {

  /**
   * The AdvanceOrderStatusAPI interface defines the contract for advancing the status of an order.
   * It extends the OrderAPI interface and provides a method to advance the order status.
   *
   * @param Request The type of the request object containing the necessary information to advance the order status.
   * @param Response The type of the response object returned after advancing the order status.
   */
  interface AdvanceOrderStatusAPI<Request, Response> : OrderAPI {
    fun advanceOrderStatus(request: Request): Response
  }

  /**
   * The DiscardOrderAPI interface defines the contract for discarding an order.
   * It extends the OrderAPI interface and provides a method to discard an order.
   *
   * @param Request The type of the request object containing the necessary information to discard the order.
   * @param Response The type of the response object returned after discarding the order.
   */
  interface DiscardOrderAPI<Request, Response> : OrderAPI {
    fun discardOrder(request: Request): Response
  }

  /**
   * The GetOrderDetailsAPI interface defines the contract for retrieving the details of an order.
   * It extends the OrderAPI interface and provides a method to get order details.
   *
   * @param Request The type of the request object containing the necessary information to retrieve order details.
   * @param Response The type of the response object returned after retrieving the order details.
   */
  interface GetOrderDetailsAPI<Request, Response> : OrderAPI {
    fun getOrderDetails(id: Request): Response
  }

  /**
   * The PlaceOrderAPI interface defines the contract for placing a new order.
   * It extends the OrderAPI interface and provides a method to place an order.
   *
   * @param Request The type of the request object containing the necessary information to place the order.
   * @param Response The type of the response object returned after placing the order.
   */
  interface PlaceOrderAPI<Request, Response> : OrderAPI {
    fun placeOrder(request: Request): Response
  }

  /**
   * The UpdateDeliveryOrderInfoAPI interface defines the contract for updating the delivery information of an order.
   * It extends the OrderAPI interface and provides a method to update delivery order information.
   *
   * @param Request The type of the request object containing the necessary information to update delivery order info.
   * @param Response The type of the response object returned after updating the delivery order info.
   */
  interface UpdateDeliveryOrderInfoAPI<Request, Response> : OrderAPI {
    fun updateDeliveryOrderInfo(request: Request): Response
  }

  /**
   * The UpdateOrderItemsAPI interface defines the contract for updating the items of an order.
   * It extends the OrderAPI interface and provides a method to update order items.
   *
   * @param Request The type of the request object containing the necessary information to update order items.
   * @param Response The type of the response object returned after updating the order items.
   */
  interface UpdateOrderItemsAPI<Request, Response> : OrderAPI {
    fun updateOrderItems(request: Request): Response
  }

  /**
   * The UpdateTakeawayOrderInfoAPI interface defines the contract for updating the takeaway information of an order.
   * It extends the OrderAPI interface and provides a method to update takeaway order information.
   *
   * @param Request The type of the request object containing the necessary information to update takeaway order info.
   * @param Response The type of the response object returned after updating the takeaway order info.
   */
  interface UpdateTakeawayOrderInfoAPI<Request, Response> : OrderAPI {
    fun updateTakeawayOrderInfo(request: Request): Response
  }
}
