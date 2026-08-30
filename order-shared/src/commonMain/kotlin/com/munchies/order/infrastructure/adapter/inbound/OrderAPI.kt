package com.munchies.order.infrastructure.adapter.inbound

import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest
import kotlin.js.JsExport

/**
 * The OrderAPI interface defines the contract for various order-related operations.
 * It serves as a base interface for specific order-related APIs, each of which handles
 * a different aspect of order management.
 */
@JsExport
object OrderAPI {

  /**
   * The AdvanceOrderStatusAPI interface defines the contract for advancing the status of an order.
   * It extends the OrderAPI interface and provides a method to advance the order status.
   *
   * @param Response The type of the response object returned after advancing the order status.
   */
  interface AdvanceOrderStatusAPI<Response> {
    fun advanceOrderStatus(request: AdvanceOrderStatusRequest): Response
  }

  /**
   * The DiscardOrderAPI interface defines the contract for discarding an order.
   * It extends the OrderAPI interface and provides a method to discard an order.
   *
   * @param Response The type of the response object returned after discarding the order.
   */
  interface DiscardOrderAPI<Response> {
    fun discardOrder(id: String): Response
  }

  /**
   * The GetOrderDetailsAPI interface defines the contract for retrieving the details of an order.
   * It extends the OrderAPI interface and provides a method to get order details.
   *
   * @param Response The type of the response object returned after retrieving the order details.
   */
  interface GetOrderDetailsAPI<Response> {
    fun getOrderDetails(id: String): Response
  }

  /**
   * The GetOrdersAPI interface defines the contract for retrieving a list of orders based on certain criteria.
   * It extends the OrderAPI interface and provides a method to get orders.
   *
   * @param Response The type of the response object returned after retrieving the orders.
   */
  interface GetOrdersAPI<Response> {
    fun getOrders(restaurantId: String?, customerId: String?, orderStatus: String?): Response
  }

  /**
   * The PlaceOrderAPI interface defines the contract for placing a new order.
   * It extends the OrderAPI interface and provides a method to place an order.
   *
   * @param Response The type of the response object returned after placing the order.
   */
  interface PlaceOrderAPI<Response> {
    fun placeOrder(request: PlaceOrderRequest): Response
  }

  /**
   * The UpdateDeliveryOrderInfoAPI interface defines the contract for updating the delivery information of an order.
   * It extends the OrderAPI interface and provides a method to update delivery order information.
   *
   * @param Response The type of the response object returned after updating the delivery order info.
   */
  interface UpdateDeliveryOrderInfoAPI<Response> {
    fun updateDeliveryOrderInfo(request: UpdateDeliveryOrderRequest): Response
  }

  /**
   * The UpdateOrderItemsAPI interface defines the contract for updating the items of an order.
   * It extends the OrderAPI interface and provides a method to update order items.
   *
   * @param Response The type of the response object returned after updating the order items.
   */
  interface UpdateOrderItemsAPI<Response> {
    fun updateOrderItems(request: UpdateOrderItemsRequest): Response
  }

  /**
   * The UpdateTakeawayOrderInfoAPI interface defines the contract for updating the takeaway information of an order.
   * It extends the OrderAPI interface and provides a method to update takeaway order information.
   *
   * @param Response The type of the response object returned after updating the takeaway order info.
   */
  interface UpdateTakeawayOrderInfoAPI<Response> {
    fun updateTakeawayOrderInfo(request: UpdateTakeawayOrderRequest): Response
  }
}
