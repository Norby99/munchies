import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  UpdateMenuItemAPI,
  UpdateMenuItemRequest,
  UpdateMenuItemResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class UpdateMenuItemRoute
  extends UpdateMenuItemAPI
  implements SimpleRoute<UpdateMenuItemResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async updateMenuItem(_restaurantId: string, _menuId: string, _categoryId: string, _itemId: string, request: UpdateMenuItemRequest): Promise<UpdateMenuItemResponse> {
    const result = await restaurantRequest(
      this.path, this.method, request.toJson(),
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      _restaurantId, _menuId, _categoryId, _itemId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<UpdateMenuItemResponse | ErrorResponse> => {
    try {
      const request = this.parseRequest(req.body.toString());
      return await this.updateMenuItem(req.params.restaurantId, req.params.menuId, req.params.categoryId, req.params.itemId, request);
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
