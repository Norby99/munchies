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
  CreateMenuItemRequest,
  CreateMenuItemAPI,
  CreateMenuItemResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class CreateMenuItemRoute
  extends CreateMenuItemAPI<CreateMenuItemResponse | ErrorResponse>
  implements SimpleRoute<CreateMenuItemResponse>
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

  async createMenuItem(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    req: CreateMenuItemRequest,
  ): Promise<CreateMenuItemResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<CreateMenuItemResponse>(
      fillPath(uri + this.path, restaurantId, menuId, categoryId),
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
    ) => Promise<CreateMenuItemResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const createReq = this.parseRequest(String(req.body));
        return this.createMenuItem(
          req.params.restaurantId,
          req.params.menuId,
          req.params.categoryId,
          createReq,
        );
      } catch (err: any) {
        return new ErrorResponse(
          "CreateMenuItem forward: \n" + String(err),
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
