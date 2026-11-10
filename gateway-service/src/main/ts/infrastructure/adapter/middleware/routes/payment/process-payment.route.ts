import { Request, RequestHandler, Response } from "express";
import {
  AuthRole,
  ErrorResponse,
  HttpMethod,
  WebResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
  ProcessPaymentAPI,
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";
import { GetOrderDetailsRoute } from "../order/get-order-details.route";

/**
 * Successful payment, wrapped in the `{ result, code }` envelope every other gateway route
 * answers with. payment-service returns a bare ProcessPaymentResponse (not a WebResponse), so the
 * gateway adds the envelope here and clients keep a single response shape.
 */
export class PaymentEnvelope extends WebResponse<ProcessPaymentResponse> {
  constructor(
    private readonly payment: ProcessPaymentResponse,
    private readonly status: number = 200,
  ) {
    super();
  }

  get result(): ProcessPaymentResponse {
    return this.payment;
  }

  get code(): number {
    return this.status;
  }

  toJson(): string {
    return JSON.stringify({ result: JSON.parse(this.payment.toJson()), code: this.status });
  }
}

/**
 * `POST /payments/`: the customer-facing entry point of the payment workflow.
 *
 * The gateway checks the order before any money moves, because payment-service charges first and
 * only then marks the order as paid on order-service, without compensation:
 * - the order must belong to the caller (403 otherwise), so nobody pays, or probes, other
 *   customers' orders;
 * - the order must not be paid already (409), so a double click cannot charge twice.
 *
 * The amount is still the one sent by the client: orders carry no prices, so nothing verifies it
 * against the menu yet. This is a known limitation, documented in the frontend as well.
 */
export class ProcessPaymentRoute
  extends ProcessPaymentAPI<PaymentEnvelope | ErrorResponse>
  implements SimpleRoute<PaymentEnvelope>
{
  constructor(private readonly orders: GetOrderDetailsRoute = new GetOrderDetailsRoute()) {
    super();
    const api: ProcessPaymentAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async processPayment(
    paymentRequest: ProcessPaymentRequest,
  ): Promise<PaymentEnvelope | ErrorResponse> {
    const uri = process.env.PAYMENT_SERVICE_URL;
    if (!uri) return new ErrorResponse("Missing Payment Service URL", 500);

    const response = await request<ProcessPaymentResponse>(
      uri + this.path,
      this.method,
      paymentRequest.toJson(),
      this.parseResponse,
      this.parseError,
    );
    return response instanceof ErrorResponse ? response : new PaymentEnvelope(response);
  }

  /** Returns an error when the caller may not pay this order, or null when the payment can go on. */
  async checkPayable(orderId: string, customerId: string): Promise<ErrorResponse | null> {
    const details = await this.orders.getOrderDetails(orderId);
    if (details instanceof ErrorResponse) return details;

    const order = details.result;
    if (order.customerId !== customerId) {
      return new ErrorResponse("You can only pay your own orders", 403);
    }
    if (order.payed) {
      return new ErrorResponse("Order already paid", 409);
    }
    return null;
  }

  private handler: {
    forward: (req: AuthedRequest) => Promise<PaymentEnvelope | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const paymentRequest = this.parseRequest(String(req.body));
        const refusal = await this.checkPayable(paymentRequest.orderId, req.user!!.id);
        if (refusal) return refusal;
        return this.processPayment(paymentRequest);
      } catch (err: any) {
        // A body that does not decode as ProcessPaymentRequest is the caller's fault.
        return new ErrorResponse("ProcessPayment forward: \n" + String(err), 400);
      }
    },
    respond: async (req: Request, res: Response) => {
      const result = await this.forward(req as AuthedRequest);
      res.status(result.code).type("json").send(result.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
