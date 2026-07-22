import type {com} from "./munchies-order-shared";

// DTO

export type OrderDTO =
    com.munchies.order.infrastructure.adapter.dto.OrderDto;
export declare const OrderDTO: typeof com.munchies.order.infrastructure.adapter.dto.OrderDto;

export type DeliveryDTO =
    com.munchies.order.infrastructure.adapter.dto.Delivery;
export declare const DeliveryDTO: typeof com.munchies.order.infrastructure.adapter.dto.Delivery;

export type TakeawayDTO =
    com.munchies.order.infrastructure.adapter.dto.Takeaway;
export declare const TakeawayDTO: typeof com.munchies.order.infrastructure.adapter.dto.Takeaway;

export type DineInDTO =
    com.munchies.order.infrastructure.adapter.dto.DineIn;
export declare const DineInDTO: typeof com.munchies.order.infrastructure.adapter.dto.DineIn;

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
