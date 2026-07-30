import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  GetManagerRestaurantsAPI,
  GetManagerRestaurantsResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class GetManagerRestaurantsRoute
  extends GetManagerRestaurantsAPI
  implements SimpleRoute<GetManagerRestaurantsResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async getManagerRestaurants(managerId: string): Promise<GetManagerRestaurantsResponse> {
    const result = await restaurantRequest(
      this.path, this.method, "",
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      managerId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<GetManagerRestaurantsResponse | ErrorResponse> => {
    try { return await this.getManagerRestaurants(req.params.managerId); }
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
