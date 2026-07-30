import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  DeleteRestaurantAPI,
  DeleteRestaurantResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class DeleteRestaurantRoute
  extends DeleteRestaurantAPI
  implements SimpleRoute<DeleteRestaurantResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async deleteRestaurant(_managerId: string, restaurantId: string): Promise<DeleteRestaurantResponse> {
    const request = this.parseRequest(JSON.stringify({ managerId: _managerId }));
    const result = await restaurantRequest(
      this.path, this.method, request.toJson(),
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      restaurantId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<DeleteRestaurantResponse | ErrorResponse> => {
    try { return await this.deleteRestaurant(req.user!!.id, req.params.restaurantId); }
    catch (error: any) {
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
