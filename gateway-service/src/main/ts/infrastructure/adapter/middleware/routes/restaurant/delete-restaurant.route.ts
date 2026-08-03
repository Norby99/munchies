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
  DeleteRestaurantAPI,
  DeleteRestaurantResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";

export class DeleteRestaurantRoute
  extends DeleteRestaurantAPI<DeleteRestaurantResponse | ErrorResponse>
  implements SimpleRoute<DeleteRestaurantResponse>
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

  async deleteRestaurant(
    managerId: string,
    restaurantId: string,
  ): Promise<DeleteRestaurantResponse | ErrorResponse> {
    const uri = process.env.RESTAURANT_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing Restaurant Service URL", 500),
      );

    const deleteReq = this.parseRequest(JSON.stringify({ managerId }));

    const response = request<DeleteRestaurantResponse>(
      fillPath(uri + this.path, restaurantId),
      this.method,
      deleteReq.toJson(),
      this.parseResponse,
      this.parseError,
    );

    return response;
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<DeleteRestaurantResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        return this.deleteRestaurant(req.user!!.id, req.params.restaurantId);
      } catch (err: any) {
        return new ErrorResponse(
          "DeleteRestaurant forward: \n" + String(err),
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
