import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  CreateMenuAPI,
  CreateMenuRequest,
  CreateMenuResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class CreateMenuRoute
  extends CreateMenuAPI
  implements SimpleRoute<CreateMenuResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async createMenu(_restaurantId: string, request: CreateMenuRequest): Promise<CreateMenuResponse> {
    const result = await restaurantRequest(
      this.path, this.method, request.toJson(),
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      _restaurantId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<CreateMenuResponse | ErrorResponse> => {
    try {
      const request = this.parseRequest(req.body.toString());
      return await this.createMenu(req.params.restaurantId, request);
    } catch (error: any) {
      if (error instanceof ErrorResponse) return error;
      return new ErrorResponse(String(error?.message ?? error));
    }
  };

  respond = async (_req: ExpressRequest, res: ExpressResponse) => {
    const result = await this.forward(_req as AuthedRequest);
    if (result instanceof ErrorResponse) { res.status(400).type("json").send(result.toJson()); }
    else { res.status(201).type("json").send(result.toJson()); }
  };
}
