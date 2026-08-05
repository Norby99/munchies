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
  UpdateCategoryRequest,
  UpdateCategoryAPI,
  UpdateCategoryResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class UpdateCategoryRoute
  extends UpdateCategoryAPI<UpdateCategoryResponse | ErrorResponse>
  implements SimpleRoute<UpdateCategoryResponse>
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

  async updateCategory(
    restaurantId: string,
    menuId: string,
    categoryId: string,
    req: UpdateCategoryRequest,
  ): Promise<UpdateCategoryResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<UpdateCategoryResponse>(
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
    ) => Promise<UpdateCategoryResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const updateReq = this.parseRequest(String(req.body));
        return this.updateCategory(
          req.params.restaurantId,
          req.params.menuId,
          req.params.categoryId,
          updateReq,
        );
      } catch (err: any) {
        return new ErrorResponse(
          "UpdateCategory forward: \n" + String(err),
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
