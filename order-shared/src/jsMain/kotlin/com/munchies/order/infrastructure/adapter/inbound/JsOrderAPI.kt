package com.munchies.order.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI.*
import com.munchies.order.infrastructure.adapter.inbound.request.*

@JsExport
abstract class JsAdvanceOrderStatusAPI :
  AdvanceOrderStatusAPI<AdvanceOrderStatusRequest, OrderDto>,
  API<AdvanceOrderStatusRequest, OrderDto>()

@JsExport
abstract class JsDiscardOrderAPI : DiscardOrderAPI<String, OrderDto>

@JsExport
abstract class JsGetOrderDetailsAPI : GetOrderDetailsAPI<String, OrderDto>

@JsExport
abstract class JsPlaceOrderAPI : PlaceOrderAPI<PlaceOrderRequest, OrderDto>

@JsExport
abstract class JsUpdateDeliveryOrderInfoAPI :
  UpdateDeliveryOrderInfoAPI<UpdateDeliveryOrderRequest, OrderDto>

@JsExport
abstract class JsUpdateOrderItemsAPI : UpdateOrderItemsAPI<UpdateOrderItemsRequest, OrderDto>

@JsExport
abstract class JsUpdateTakeawayOrderInfoAPI :
  UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderRequest, OrderDto>
