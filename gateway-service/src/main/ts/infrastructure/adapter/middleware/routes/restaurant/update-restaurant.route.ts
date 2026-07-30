import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  UpdateRestaurantAPI,
  UpdateRestaurantRequest,
  UpdateRestaurantResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class UpdateRestaurantRoute
  extends UpdateRestaurantAPI
  implements SimpleRoute<UpdateRestaurantResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async updateRestaurant(_request: UpdateRestaurantRequest): Promise<UpdateRestaurantResponse> {
    throw new Error("restaurantId must be provided via path params");
  }

  forward = async (req: AuthedRequest): Promise<UpdateRestaurantResponse | ErrorResponse> => {
    try {
      const request = this.parseRequest(req.body.toString());
      const result = await restaurantRequest(
        this.path, this.method, request.toJson(),
        (json) => this.parseResponse(json),
        (json) => this.parseError(json),
        req.params.restaurantId,
      );
      if (result instanceof ErrorResponse) throw result;
      return result;
    } catch (error: any) {
      if (error instanceof ErrorResponse) return error;
      return new ErrorResponse(String(error?.message ?? error));
    }
  };

  respond = async (_req: ExpressRequest, res: ExpressResponse) => {
    const result = await this.forward(_req as AuthedRequest);
    if (result instanceof ErrorResponse) { res.status(400).type("json").send(result.toJson()); }
    else { res.status(200).type("json").send(result.toJson()); }
  };
}
