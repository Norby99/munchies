import {SimpleRoute} from "../simple-route";
import {GetOrderDetailsRoute} from "./get-order-details.route";
import {GetOrdersRoute} from "./get-orders.route";
import {PlaceOrderRoute} from "./place-order.route";
import {AdvanceOrderStatusRoute} from "./advance-order-status.route";
import {DiscardOrderRoute} from "./discard-order.route";
import {UpdateOrderItemsRoute} from "./update-order-items.route";
import {UpdateDeliveryOrderRoute} from "./update-delivery-order.route";
import {UpdateTakeawayOrderRoute} from "./update-takeaway-order.route";

// PATCH /orders/{id}/pay is deliberately not exposed: it marks an order as paid without any
// payment. Only payment-service calls it, directly on order-service, after charging the payment
// (POST /payments/, see payment/process-payment.route.ts).
export const orderRoutes: SimpleRoute<any>[] = [
    new GetOrderDetailsRoute(),
    new GetOrdersRoute(),
    new PlaceOrderRoute(),
    new AdvanceOrderStatusRoute(),
    new DiscardOrderRoute(),
    new UpdateOrderItemsRoute(),
    new UpdateDeliveryOrderRoute(),
    new UpdateTakeawayOrderRoute(),
];
