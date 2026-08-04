import { SimpleRoute } from "../simple-route";
import { GetOrderDetailsRoute } from "./get-order-details.route";
import { PlaceOrderRoute } from "./place-order.route";
import { AdvanceOrderStatusRoute } from "./advance-order-status.route";
import { DiscardOrderRoute } from "./discard-order.route";
import { UpdateOrderItemsRoute } from "./update-order-items.route";
import { UpdateDeliveryOrderRoute } from "./update-delivery-order.route";
import { UpdateTakeawayOrderRoute } from "./update-takeaway-order.route";

export const orderRoutes: SimpleRoute<any>[] = [
  new GetOrderDetailsRoute(),
  new PlaceOrderRoute(),
  new AdvanceOrderStatusRoute(),
  new DiscardOrderRoute(),
  new UpdateOrderItemsRoute(),
  new UpdateDeliveryOrderRoute(),
  new UpdateTakeawayOrderRoute(),
];
