/**
 * Outcome of a request sent to order-service to update an order's payment status.
 */
export interface OrderPaymentNotificationResult {
  success: boolean;
  errorMessage?: string;
}

/**
 * What payment-service needs to know about an order before charging it.
 *
 * @property customerId The customer who placed the order: only they can pay it.
 * @property payed Whether the order has already been paid.
 * @property totalCents The amount owed, in euro cents, as priced by order-service from the
 * restaurant's menus. This, never a client-provided figure, is the amount charged.
 */
export interface PayableOrder {
  customerId: string;
  payed: boolean;
  totalCents: number;
}

export type OrderLookupResult =
  | { type: "FOUND"; order: PayableOrder }
  | { type: "NOT_FOUND" }
  | { type: "UNAVAILABLE"; reason: string };

/**
 * Outbound port towards order-service, over its REST API: reading an order before charging it,
 * and informing order-service that it has been paid.
 */
export interface OrderServiceClient {
  getOrder(orderId: string): Promise<OrderLookupResult>;
  markOrderAsPaid(orderId: string): Promise<OrderPaymentNotificationResult>;
}
