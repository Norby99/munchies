import {
  Request,
  RequestHandler,
  Response,
} from "express";
import {
  HttpMethod,
  AuthRole,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
  DiscardOrderAPI,
  DiscardOrderResponse,
  OrderServiceConfig,
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

/**
 * `DELETE /orders/{id}`: cancels an order. The caller's id, taken from the session, is always
 * forwarded as `customerId`, whatever the role: order-service then cancels only the caller's own
 * orders and answers 404 for anyone else's (a manager has no orders of their own to cancel here).
 */
export class DiscardOrderRoute
  extends DiscardOrderAPI<DiscardOrderResponse | ErrorResponse>
  implements SimpleRoute<DiscardOrderResponse>
{
  constructor() {
    super();
    let api: DiscardOrderAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async discardOrder(
    id: string,
    customerId: string | null,
  ): Promise<DiscardOrderResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const query = customerId
      ? `?${new URLSearchParams({ [OrderServiceConfig.CUSTOMER_ID_QUERY]: customerId })}`
      : "";
    const response = request<DiscardOrderResponse>(
      fillPath(uri + this.path, encodeURIComponent(id)) + query,
      this.method,
      "",
      this.parseResponse,
      this.parseError,
    );

    return response;
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<DiscardOrderResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        return this.discardOrder(req.params.id, req.user!!.id);
      } catch (err: any) {
        return new ErrorResponse(
          "DiscardOrder forward: \n" + String(err),
          500,
        );
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
