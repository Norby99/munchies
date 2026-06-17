package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI.*
import com.munchies.order.infrastructure.adapter.inbound.request.*

@JsExport
interface JsAdvanceOrderStatusAPI :
  AdvanceOrderStatusAPI<AdvanceOrderStatusRequest, OrderDto>

@JsExport
interface JsDiscardOrderAPI : DiscardOrderAPI<DiscardOrderRequest, OrderDto>

@JsExport
interface JsGetOrderDetailsAPI : GetOrderDetailsAPI<GetOrderDetailsRequest, OrderDto>

@JsExport
interface JsPlaceOrderAPI : PlaceOrderAPI<PlaceOrderRequest, OrderDto>

@JsExport
interface JsUpdateDeliveryOrderInfoAPI :
  UpdateDeliveryOrderInfoAPI<UpdateDeliveryOrderRequest, OrderDto>

@JsExport
interface JsUpdateOrderItemsAPI : UpdateOrderItemsAPI<UpdateOrderItemsRequest, OrderDto>

@JsExport
interface JsUpdateTakeawayOrderInfoAPI :
  UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderRequest, OrderDto>
