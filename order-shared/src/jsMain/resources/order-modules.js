// order-modules.js
const generated = require("./munchies-order-shared.js");
const _order = generated.com.munchies.order;
const _inbound = _order.infrastructure.adapter.inbound;
const _outbound = _order.infrastructure.adapter.outbound;
const _dto = _order.infrastructure.adapter.dto;

module.exports = {
    OrderDTO: _dto.OrderDto,
    OrderType: _dto.OrderType,
    OrderItemDto: _dto.OrderItemDto,

    OrderServiceConfig: _inbound.web.config.OrderServiceConfig,

    // API
    GetOrderDetailsAPI: _inbound.JsGetOrderDetailsAPI,
    GetOrdersAPI: _inbound.JsGetOrdersAPI,
    PlaceOrderAPI: _inbound.JsPlaceOrderAPI,
    AdvanceOrderStatusAPI: _inbound.JsAdvanceOrderStatusAPI,
    DiscardOrderAPI: _inbound.JsDiscardOrderAPI,
    UpdateOrderItemsAPI: _inbound.JsUpdateOrderItemsAPI,
    UpdateDeliveryOrderInfoAPI: _inbound.JsUpdateDeliveryOrderInfoAPI,
    UpdateTakeawayOrderInfoAPI: _inbound.JsUpdateTakeawayOrderInfoAPI,

    // Requests & Deserializers
    PlaceOrderRequest: _inbound.request.PlaceOrderRequest,
    placeOrderRequestFromJson: _inbound.request.placeOrderRequestFromJson,

    AdvanceOrderStatusRequest: _inbound.request.AdvanceOrderStatusRequest,
    advanceOrderStatusRequestFromJson: _inbound.request.advanceOrderStatusRequestFromJson,

    UpdateDeliveryOrderRequest: _inbound.request.UpdateDeliveryOrderRequest,
    updateDeliveryOrderRequestFromJson: _inbound.request.updateDeliveryOrderRequestFromJson,

    UpdateOrderItemsRequest: _inbound.request.UpdateOrderItemsRequest,
    updateOrderItemsRequestFromJson: _inbound.request.updateOrderItemsRequestFromJson,

    UpdateTakeawayOrderRequest: _inbound.request.UpdateTakeawayOrderRequest,
    updateTakeawayOrderRequestFromJson: _inbound.request.updateTakeawayOrderRequestFromJson,

    // Responses & Deserializers
    GetOrderDetailsResponse: _outbound.response.GetOrderDetailsResponse,
    getOrderDetailsResponseFromJson: _outbound.response.getOrderDetailsResponseFromJson,

    GetOrdersResponse: _outbound.response.GetOrdersResponse,
    getOrdersResponseFromJson: _outbound.response.getOrdersResponseFromJson,

    PlaceOrderResponse: _outbound.response.PlaceOrderResponse,
    placeOrderResponseFromJson: _outbound.response.placeOrderResponseFromJson,

    AdvanceOrderStatusResponse: _outbound.response.AdvanceOrderStatusResponse,
    advanceOrderStatusResponseFromJson: _outbound.response.advanceOrderStatusResponseFromJson,

    DiscardOrderResponse: _outbound.response.DiscardOrderResponse,
    discardOrderResponseFromJson: _outbound.response.discardOrderResponseFromJson,

    UpdateDeliveryOrderResponse: _outbound.response.UpdateDeliveryOrderResponse,
    updateDeliveryOrderResponseFromJson: _outbound.response.updateDeliveryOrderResponseFromJson,

    UpdateOrderItemsResponse: _outbound.response.UpdateOrderItemsResponse,
    updateOrderItemsResponseFromJson: _outbound.response.updateOrderItemsResponseFromJson,

    UpdateTakeawayOrderResponse: _outbound.response.UpdateTakeawayOrderResponse,
    updateTakeawayOrderResponseFromJson: _outbound.response.updateTakeawayOrderResponseFromJson,
};
