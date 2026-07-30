import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  RemoveMenuItemAPI,
  DeleteMenuItemResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class RemoveMenuItemRoute
  extends RemoveMenuItemAPI
  implements SimpleRoute<DeleteMenuItemResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async removeMenuItem(restaurantId: string, menuId: string, categoryId: string, itemId: string): Promise<DeleteMenuItemResponse> {
    const result = await restaurantRequest(
      this.path, this.method, "",
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      restaurantId, menuId, categoryId, itemId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<DeleteMenuItemResponse | ErrorResponse> => {
    try { return await this.removeMenuItem(req.params.restaurantId, req.params.menuId, req.params.categoryId, req.params.itemId); }
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
