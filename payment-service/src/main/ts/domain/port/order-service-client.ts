/**
 * Outcome of a request sent to order-service to update an order's payment status.
 */
export interface OrderPaymentNotificationResult {
  success: boolean;
  errorMessage?: string;
}

/**
 * Outbound port towards order-service. Implementations are responsible for
 * informing order-service, over its REST API, that an order has been paid.
 */
export interface OrderServiceClient {
  markOrderAsPaid(orderId: string): Promise<OrderPaymentNotificationResult>;
}
