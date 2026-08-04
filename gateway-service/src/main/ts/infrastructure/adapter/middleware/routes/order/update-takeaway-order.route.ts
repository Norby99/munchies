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
  UpdateTakeawayOrderInfoAPI,
  UpdateTakeawayOrderRequest,
  UpdateTakeawayOrderResponse,
} from "munchies-order-service-shared/kotlin/order-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class UpdateTakeawayOrderRoute
  extends UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderResponse | ErrorResponse>
  implements SimpleRoute<UpdateTakeawayOrderResponse>
{
  constructor() {
    super();
    let api: UpdateTakeawayOrderInfoAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async updateTakeawayOrderInfo(
    req: UpdateTakeawayOrderRequest,
  ): Promise<UpdateTakeawayOrderResponse | ErrorResponse> {
    const uri = process.env.ORDER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Order Service URL", 500),
      );

    const response = request<UpdateTakeawayOrderResponse>(
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
    ) => Promise<UpdateTakeawayOrderResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const updateReq = this.parseRequest(String(req.body)).addId(
          req.user!!.id,
        );
        return this.updateTakeawayOrderInfo(updateReq);
      } catch (err: any) {
        return new ErrorResponse(
          "UpdateTakeawayOrder forward: \n" + String(err),
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
