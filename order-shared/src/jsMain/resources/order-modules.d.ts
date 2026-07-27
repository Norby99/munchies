import type {com} from "./munchies-order-shared";

// DTO

export type OrderDTO =
    com.munchies.order.infrastructure.adapter.dto.OrderDto;
export declare const OrderDTO: typeof com.munchies.order.infrastructure.adapter.dto.OrderDto;

export type OrderType = com.munchies.order.infrastructure.adapter.dto.OrderType;
export declare const OrderType: typeof com.munchies.order.infrastructure.adapter.dto.OrderType;

export type OrderItemDto = com.munchies.order.infrastructure.adapter.dto.OrderItemDto;
export declare const OrderItemDto: typeof com.munchies.order.infrastructure.adapter.dto.OrderItemDto;

// Request

export type AdvanceOrderStatusRequest = com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest;
export declare const AdvanceOrderStatusRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest;

export type PlaceOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest;
export declare const PlaceOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest;

export type UpdateDeliveryOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest;
export declare const UpdateDeliveryOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest;

export type UpdateOrderItemsRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest;
export declare const UpdateOrderItemsRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest;

export type UpdateTakeawayOrderRequest = com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest;
export declare const UpdateTakeawayOrderRequest: typeof com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest;

// Response

export type AdvanceOrderStatusResponse = com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse;
export declare const AdvanceOrderStatusResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.AdvanceOrderStatusResponse;

export type DiscardOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse;
export declare const DiscardOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.DiscardOrderResponse;

export type GetOrderDetailsResponse = com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse;
export declare const GetOrderDetailsResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.GetOrderDetailsResponse;

export type PlaceOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse;
export declare const PlaceOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.PlaceOrderResponse;

export type UpdateDeliveryOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse;
export declare const UpdateDeliveryOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.UpdateDeliveryOrderResponse;

export type UpdateOrderItemsResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateOrderItemsResponse;
export declare const UpdateOrderItemsResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.UpdateOrderItemsResponse;

export type UpdateTakeawayOrderResponse = com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponse;
export declare const UpdateTakeawayOrderResponse: typeof com.munchies.order.infrastructure.adapter.outbound.request.UpdateTakeawayOrderResponse;

// API

export type JsAdvanceOrderStatusAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsAdvanceOrderStatusAPI;
export declare const JsAdvanceOrderStatusAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsAdvanceOrderStatusAPI;

export type JsDiscardOrderAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsDiscardOrderAPI;
export declare const JsDiscardOrderAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsDiscardOrderAPI;

export type JsGetOrderDetailsAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsGetOrderDetailsAPI;
export declare const JsGetOrderDetailsAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsGetOrderDetailsAPI;

export type JsPlaceOrderAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsPlaceOrderAPI;
export declare const JsPlaceOrderAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsPlaceOrderAPI;

export type JsUpdateDeliveryOrderInfoAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateDeliveryOrderInfoAPI;
export declare const JsUpdateDeliveryOrderInfoAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateDeliveryOrderInfoAPI;

export type JsUpdateOrderItemsAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateOrderItemsAPI;
export declare const JsUpdateOrderItemsAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateOrderItemsAPI;

export type JsUpdateTakeawayOrderInfoAPI =
    com.munchies.order.infrastructure.adapter.inbound.JsUpdateTakeawayOrderInfoAPI;
export declare const JsUpdateTakeawayOrderInfoAPI: typeof com.munchies.order.infrastructure.adapter.inbound.JsUpdateTakeawayOrderInfoAPI;
