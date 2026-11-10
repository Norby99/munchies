// Payment of an order, through the gateway's POST /payments/ (payment-service). The gateway
// refuses orders that are not the caller's or already paid, then payment-service charges the
// payment, marks the order as paid on order-service and notifies the customer.
//
// Known limitation: the amount is computed client-side from the menu prices (orders store no
// prices) and nothing on the server verifies it against the order yet.

import { http, unwrap } from '@/api/client'

// Methods offered online; CASH, BANK_TRANSFER and OTHER exist in payment-shared but make no
// sense for an immediate in-app payment.
export type OnlinePaymentMethod = 'CARD' | 'MOBILE_PAYMENT'

export interface PaymentResult {
  paymentId: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | string
  amount: number
  currency: string
}

export async function payOrder(
  orderId: string,
  amountCents: number,
  method: OnlinePaymentMethod,
): Promise<PaymentResult> {
  return unwrap(
    http.post('/payments/', {
      orderId,
      paymentDetails: { amount: amountCents, method, currency: 'EUR' },
    }),
  )
}
