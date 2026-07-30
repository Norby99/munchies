import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";
import {
  CreateCategoryAPI,
  CreateCategoryRequest,
  CreateCategoryResponse,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import { Request as ExpressRequest, Response as ExpressResponse } from "express";
import { SimpleRoute } from "../simple-route";
import { AuthedRequest } from "../../auth";
import { restaurantRequest } from "./restaurant-route-helper";

export class CreateCategoryRoute
  extends CreateCategoryAPI
  implements SimpleRoute<CreateCategoryResponse>
{
  path = this.getPath();
  method = this.getMethod();
  authRole = this.getRequiredAuthRole();
  onAuthFail = (msg: string) => new ErrorResponse(msg);

  async createCategory(_restaurantId: string, _menuId: string, request: CreateCategoryRequest): Promise<CreateCategoryResponse> {
    const result = await restaurantRequest(
      this.path, this.method, request.toJson(),
      (json) => this.parseResponse(json),
      (json) => this.parseError(json),
      _restaurantId, _menuId,
    );
    if (result instanceof ErrorResponse) throw result;
    return result;
  }

  forward = async (req: AuthedRequest): Promise<CreateCategoryResponse | ErrorResponse> => {
    try {
      const request = this.parseRequest(req.body.toString());
      return await this.createCategory(req.params.restaurantId, req.params.menuId, request);
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
