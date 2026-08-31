import type {com} from "./munchies-order-shared";

// DTO

export type OrderDTO =
    com.munchies.order.infrastructure.adapter.dto.OrderDto;
export declare const OrderDTO: typeof com.munchies.order.infrastructure.adapter.dto.OrderDto;

export type OrderType = com.munchies.order.infrastructure.adapter.dto.OrderType;
export declare const OrderType: typeof com.munchies.order.infrastructure.adapter.dto.OrderType;

export type OrderItemDto = com.munchies.order.infrastructure.adapter.dto.OrderItemDto;
export declare const OrderItemDto: typeof com.munchies.order.infrastructure.adapter.dto.OrderItemDto;

export declare const OrderServiceConfig: typeof com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig;

// Request

export type AdvanceOrderStatusRequest = com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest;
export declare const AdvanceOrderStatusRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest;
export declare const advanceOrderStatusRequestFromJson: typeof com.munchies.order.infrastructure.adapter.inbound.request.advanceOrderStatusRequestFromJson;

export type PlaceOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest;
export declare const PlaceOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest;
export declare const placeOrderRequestFromJson: typeof com.munchies.order.infrastructure.adapter.inbound.request.placeOrderRequestFromJson;

export type UpdateDeliveryOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest;
export declare const UpdateDeliveryOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest;
export declare const updateDeliveryOrderRequestFromJson: typeof com.munchies.order.infrastructure.adapter.inbound.request.updateDeliveryOrderRequestFromJson;

export type UpdateOrderItemsRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest;
export declare const UpdateOrderItemsRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest;
export declare const updateOrderItemsRequestFromJson: typeof com.munchies.order.infrastructure.adapter.inbound.request.updateOrderItemsRequestFromJson;

export type UpdateTakeawayOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest;
export declare const UpdateTakeawayOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest;
export declare const updateTakeawayOrderRequestFromJson: typeof com.munchies.order.infrastructure.adapter.inbound.request.updateTakeawayOrderRequestFromJson;

// Response

export type AdvanceOrderStatusResponse = com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse;
export declare const AdvanceOrderStatusResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse;
export declare const advanceOrderStatusResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.advanceOrderStatusResponseFromJson;

export type DiscardOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse;
export declare const DiscardOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse;
export declare const discardOrderResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.discardOrderResponseFromJson;

export type GetOrderDetailsResponse = com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse;
export declare const GetOrderDetailsResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse;
export declare const getOrderDetailsResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.getOrderDetailsResponseFromJson;

export type GetOrdersResponse = com.munchies.order.infrastructure.adapter.outbound.response.GetOrdersResponse;
export declare const GetOrdersResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.GetOrdersResponse;
export declare const getOrdersResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.getOrdersResponseFromJson;

export type PlaceOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse;
export declare const PlaceOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse;
export declare const placeOrderResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.placeOrderResponseFromJson;

export type UpdateDeliveryOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse;
export declare const UpdateDeliveryOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse;
export declare const updateDeliveryOrderResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.updateDeliveryOrderResponseFromJson;

export type UpdateOrderItemsResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateOrderItemsResponse;
export declare const UpdateOrderItemsResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.UpdateOrderItemsResponse;
export declare const updateOrderItemsResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.updateOrderItemsResponseFromJson;

export type UpdateTakeawayOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponse;
export declare const UpdateTakeawayOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponse;
export declare const updateTakeawayOrderResponseFromJson: typeof com.munchies.order.infrastructure.adapter.outbound.response.updateTakeawayOrderResponseFromJson;

// API

export type GetOrderDetailsAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsGetOrderDetailsAPI;
export declare const GetOrderDetailsAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsGetOrderDetailsAPI;

export type GetOrdersAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsGetOrdersAPI;
export declare const GetOrdersAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsGetOrdersAPI;

export type PlaceOrderAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsPlaceOrderAPI;
export declare const PlaceOrderAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsPlaceOrderAPI;

export type AdvanceOrderStatusAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsAdvanceOrderStatusAPI;
export declare const AdvanceOrderStatusAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsAdvanceOrderStatusAPI;

export type DiscardOrderAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsDiscardOrderAPI;
export declare const DiscardOrderAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsDiscardOrderAPI;

export type UpdateOrderItemsAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateOrderItemsAPI;
export declare const UpdateOrderItemsAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateOrderItemsAPI;

export type UpdateDeliveryOrderInfoAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateDeliveryOrderInfoAPI;
export declare const UpdateDeliveryOrderInfoAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateDeliveryOrderInfoAPI;

export type UpdateTakeawayOrderInfoAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateTakeawayOrderInfoAPI;
export declare const UpdateTakeawayOrderInfoAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateTakeawayOrderInfoAPI;
