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
 * Like every other authenticated route, the gateway only stamps the caller's id from the auth
 * token onto the request (overriding any id in the body) and forwards it. All the business
 * checks live in payment-service: the order must belong to that customer and be unpaid, and the
 * amount charged is the order total priced by order-service from the restaurant's menus.
 */
export class ProcessPaymentRoute
  extends ProcessPaymentAPI<PaymentEnvelope | ErrorResponse>
  implements SimpleRoute<PaymentEnvelope>
{
  constructor() {
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
    // Checked on the success type, not with `instanceof ErrorResponse`: the error parsed by
    // payment-shared is an ErrorResponse from the commons copy bundled in that package, a
    // different class from the gateway's own, so that check would wrap errors as successes.
    return response instanceof ProcessPaymentResponse
      ? new PaymentEnvelope(response)
      : (response as ErrorResponse);
  }

  private handler: {
    forward: (req: AuthedRequest) => Promise<PaymentEnvelope | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const paymentRequest = this.parseRequest(String(req.body)).addId(req.user!!.id);
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
