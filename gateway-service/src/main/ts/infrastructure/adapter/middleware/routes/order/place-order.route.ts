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
  PlaceOrderRequest,
  PlaceOrderAPI,
  PlaceOrderResponse,
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class PlaceOrderRoute
  extends PlaceOrderAPI<PlaceOrderResponse | ErrorResponse>
  implements SimpleRoute<PlaceOrderResponse>
{
  constructor() {
    super();
    let api: PlaceOrderAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async placeOrder(
    req: PlaceOrderRequest,
  ): Promise<PlaceOrderResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const response = request<PlaceOrderResponse>(
      uri + this.path,
      this.method,
      req.toJson(),
      this.parseResponse,
      this.parseError,
    );

    return response;
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<PlaceOrderResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const placeOrderReq = this.parseRequest(String(req.body)).addId(
          req.user!!.id,
        );
        return this.placeOrder(placeOrderReq);
      } catch (err: any) {
        return new ErrorResponse(
          "PlaceOrder forward: \n" + String(err),
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
