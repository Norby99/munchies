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
  RemoveMenuItemAPI,
  DeleteMenuItemResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class RemoveMenuItemRoute
  extends RemoveMenuItemAPI<DeleteMenuItemResponse | ErrorResponse>
  implements SimpleRoute<DeleteMenuItemResponse>
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

  async removeMenuItem(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    itemId: string,
  ): Promise<DeleteMenuItemResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<DeleteMenuItemResponse>(
      fillPath(uri + this.path, restaurantId, menuId, categoryId, itemId),
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
    ) => Promise<DeleteMenuItemResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        return this.removeMenuItem(
          req.params.restaurantId,
          req.params.menuId,
          req.params.categoryId,
          req.params.itemId,
        );
      } catch (err: any) {
        return new ErrorResponse(
          "RemoveMenuItem forward: \n" + String(err),
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
