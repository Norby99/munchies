package com.munchies.order.infrastructure.adapter.inbound

sealed interface OrderAPI {

  interface AdvanceOrderStatusAPI<Request, Response> : OrderAPI {
    fun advanceOrderStatus(request: Request): Response
  }

  interface DiscardOrderAPI<Request, Response> : OrderAPI {
    fun discardOrder(request: Request): Response
  }

  interface GetOrderDetailsAPI<Request, Response> : OrderAPI {
    fun getOrderDetails(id: Request): Response
  }

  interface PlaceOrderAPI<Request, Response> : OrderAPI {
    fun placeOrder(request: Request): Response
  }

  interface UpdateDeliveryOrderInfoAPI<Request, Response> : OrderAPI {
    fun updateDeliveryOrderInfo(request: Request): Response
  }

  interface UpdateOrderItemsAPI<Request, Response> : OrderAPI {
    fun updateOrderItems(request: Request): Response
  }

  interface UpdateTakeawayOrderInfoAPI<Request, Response> : OrderAPI {
    fun updateTakeawayOrderInfo(request: Request): Response
  }
}
