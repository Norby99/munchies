import { OrderType } from "../model/booking";

export interface OrderItem {
  menuItemId: string;
  quantity: number;
}

/** Mirrors order-service PlaceOrderRequest (the fields the scheduler fills). */
export interface PlaceOrderInput {
  restaurantId: string;
  customerId: string;
  items: OrderItem[];
  orderType: OrderType;
  /** Epoch ms as string — order-service encodes timestamps as stringified millis. */
  estimatedDeliveryTime?: string;
  deliveryAddress?: string;
  bellName?: string;
  customerPhone?: string;
  pickupTime?: string;
  customerName?: string;
  tableNumber?: number;
  numberOfGuests?: number;
}

export interface PlacedOrder {
  orderId: string;
  status: string;
}

export class OrderServiceError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = "OrderServiceError";
  }
}

/**
 * Outbound port to order-service. Used both for the customer-driven "confirm my
 * scheduled order" flow and for the internal timer that advances order status.
 */
export interface OrderClient {
  place(input: PlaceOrderInput): Promise<PlacedOrder>;
  advance(orderId: string): Promise<void>;
}
