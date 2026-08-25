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
  GetManagerRestaurantsAPI,
  GetManagerRestaurantsResponse,
  RestaurantServiceConfig,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class GetManagerRestaurantsRoute
  extends GetManagerRestaurantsAPI<
    GetManagerRestaurantsResponse | ErrorResponse
  >
  implements SimpleRoute<GetManagerRestaurantsResponse>
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

  async getManagerRestaurants(
    managerId: string,
  ): Promise<GetManagerRestaurantsResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const response = request<GetManagerRestaurantsResponse>(
      fillPath(
        uri +
        RestaurantServiceConfig.SERVICE_PATH +
        RestaurantServiceConfig.GET_MANAGER_RESTAURANTS_PATH,
        managerId,
      ),
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
    ) => Promise<GetManagerRestaurantsResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        return this.getManagerRestaurants(req.user!!.id);
      } catch (err: any) {
        return new ErrorResponse(
          "GetManagerRestaurants forward: \n" + String(err),
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
