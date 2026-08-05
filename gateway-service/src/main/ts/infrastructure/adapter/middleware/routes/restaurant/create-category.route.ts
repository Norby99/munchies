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
  CreateCategoryRequest,
  CreateCategoryAPI,
  CreateCategoryResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class CreateCategoryRoute
  extends CreateCategoryAPI<CreateCategoryResponse | ErrorResponse>
  implements SimpleRoute<CreateCategoryResponse>
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

  async createCategory(
    restaurantId: string,
    menuId: string,
    req: CreateCategoryRequest,
  ): Promise<CreateCategoryResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<CreateCategoryResponse>(
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
    ) => Promise<CreateCategoryResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const createReq = this.parseRequest(String(req.body));
        return this.createCategory(
          req.params.restaurantId,
          req.params.menuId,
          createReq,
        );
      } catch (err: any) {
        return new ErrorResponse(
          "CreateCategory forward: \n" + String(err),
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
