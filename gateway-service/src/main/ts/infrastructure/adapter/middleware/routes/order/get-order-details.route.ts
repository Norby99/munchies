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
  GetOrderDetailsAPI,
  GetOrderDetailsResponse,
  OrderServiceConfig,
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

/**
 * `GET /orders/{id}`: reads one order. A customer only gets their own: their id, taken from the
 * session, is forwarded as `customerId` and order-service answers 404 for anyone else's order.
 * Managers read the orders of their board without that filter.
 */
export class GetOrderDetailsRoute
  extends GetOrderDetailsAPI<GetOrderDetailsResponse | ErrorResponse>
  implements SimpleRoute<GetOrderDetailsResponse>
{
  constructor() {
    super();
    let api: GetOrderDetailsAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async getOrderDetails(
    id: string,
    customerId: string | null,
  ): Promise<GetOrderDetailsResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const query = customerId
      ? `?${new URLSearchParams({ [OrderServiceConfig.CUSTOMER_ID_QUERY]: customerId })}`
      : "";
    const response = request<GetOrderDetailsResponse>(
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
    ) => Promise<GetOrderDetailsResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const isCustomer = req.user?.role.name === AuthRole.CUSTOMER.name;
        return this.getOrderDetails(req.params.id, isCustomer ? req.user!!.id : null);
      } catch (err: any) {
        return new ErrorResponse(
          "GetOrderDetails forward: \n" + String(err),
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
