import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  UpdateCategoryAPI,
  UpdateCategoryRequest,
  UpdateCategoryResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class UpdateCategoryRoute
  extends UpdateCategoryAPI
  implements SimpleRoute<UpdateCategoryResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async updateCategory(_restaurantId: string, _menuId: string, _categoryId: string, request: UpdateCategoryRequest): Promise<UpdateCategoryResponse> {
    const result = await restaurantRequest(
      this.path, this.method, request.toJson(),
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      _restaurantId, _menuId, _categoryId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<UpdateCategoryResponse | ErrorResponse> => {
    try {
      const request = this.parseRequest(req.body.toString());
      return await this.updateCategory(req.params.restaurantId, req.params.menuId, req.params.categoryId, request);
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
