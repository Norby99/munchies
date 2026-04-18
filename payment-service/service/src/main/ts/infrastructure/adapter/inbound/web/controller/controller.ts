import { Body, Post, Route, Tags } from "tsoa";

interface PaymentRequest {
  orderId: string;
  amountCents: number;
}

interface PaymentResponse {
  paymentId: string;
  accepted: boolean;
}

@Route("payments")
@Tags("Payments")
export class PaymentController {
  @Post()
  public async createPayment(
    @Body() body: PaymentRequest
  ): Promise<PaymentResponse> {
    return { paymentId: "p-123", accepted: true };
  }
}
