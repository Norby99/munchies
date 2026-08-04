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
  AdvanceOrderStatusRequest,
  AdvanceOrderStatusAPI,
  AdvanceOrderStatusResponse,
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class AdvanceOrderStatusRoute
  extends AdvanceOrderStatusAPI<AdvanceOrderStatusResponse | ErrorResponse>
  implements SimpleRoute<AdvanceOrderStatusResponse>
{
  constructor() {
    super();
    let api: AdvanceOrderStatusAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async advanceOrderStatus(
    req: AdvanceOrderStatusRequest,
  ): Promise<AdvanceOrderStatusResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const response = request<AdvanceOrderStatusResponse>(
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
    ) => Promise<AdvanceOrderStatusResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const advanceReq = this.parseRequest(String(req.body));
        return this.advanceOrderStatus(advanceReq);
      } catch (err: any) {
        return new ErrorResponse(
          "AdvanceOrderStatus forward: \n" + String(err),
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
