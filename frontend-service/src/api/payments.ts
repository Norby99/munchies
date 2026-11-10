// Payment of an order, through the gateway's POST /payments/ (payment-service).
//
// The client only picks the payment method: the gateway stamps the customer id from the session
// cookie, and payment-service checks that the order is the caller's and still unpaid, then
// charges the order total priced by order-service from the restaurant's menus.

import { http, unwrap } from '@/api/client'

// Methods offered online; CASH, BANK_TRANSFER and OTHER exist in payment-shared but make no
// sense for an immediate in-app payment.
export type OnlinePaymentMethod = 'CARD' | 'MOBILE_PAYMENT'

export interface PaymentResult {
  paymentId: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | string
  // Charged amount in cents, i.e. the order total.
  amount: number
  currency: string
}

export async function payOrder(orderId: string, method: OnlinePaymentMethod): Promise<PaymentResult> {
  return unwrap(
    http.post('/payments/', {
      orderId,
      // Filled by the gateway from the session; sent empty like the other authenticated requests.
      customerId: '',
      paymentDetails: { method },
    }),
  )
}
