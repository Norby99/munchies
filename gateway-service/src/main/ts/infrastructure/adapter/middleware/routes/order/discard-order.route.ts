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
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

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

  async discardOrder(id: string): Promise<DiscardOrderResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const response = request<DiscardOrderResponse>(
      fillPath(uri + this.path, id),
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
        const id = req.params.id;
        return this.discardOrder(id);
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
