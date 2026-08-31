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
  UpdateMenuRequest,
  UpdateMenuAPI,
  UpdateMenuResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class UpdateMenuRoute
  extends UpdateMenuAPI<UpdateMenuResponse | ErrorResponse>
  implements SimpleRoute<UpdateMenuResponse>
{
  constructor() {
    super();
    let api = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async updateMenu(
    restaurantId: string,
    menuId: string,
    req: UpdateMenuRequest,
  ): Promise<UpdateMenuResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<UpdateMenuResponse>(
      fillPath(uri + this.path, restaurantId, menuId),
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
    ) => Promise<UpdateMenuResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const updateReq = this.parseRequest(String(req.body)).addId(
          req.user!!.id,
        );
        return this.updateMenu(
          req.params.restaurantId,
          req.params.menuId,
          updateReq,
        );
      } catch (err: any) {
        return new ErrorResponse(
          "UpdateMenu forward: \n" + String(err),
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
