
import { Response as ExpressResponse } from "express";
import { HttpMethod, AuthRole } from "munchies-commons/kotlin/commons-modules";
import {
  UpdateUserPasswordRequest,
  UpdateUserPasswordAPI,
  UpdateUserPasswordResponse,
  UpdateUserPasswordFailure,
  UpdateUserPasswordResult,
  UpdateUserPasswordSuccess,
  UserServiceConfig,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { RouteDefinition, InternalRoute } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { fillPath } from "../routes";
class InternalUpdateUserPasswordRoute
  extends UpdateUserPasswordAPI
  implements
    InternalRoute<
      UpdateUserPasswordAPI,
      UpdateUserPasswordRequest,
      UpdateUserPasswordResponse,
      UpdateUserPasswordResult,
      UpdateUserPasswordSuccess,
      UpdateUserPasswordFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = this.service.getRequiredAuthRole();
    this.method = this.service.getMethod();
  }
  service: UpdateUserPasswordAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): UpdateUserPasswordResponse {
    return new UpdateUserPasswordResponse(this.generateFailure(reason), code);
  }
  parseResult(result: UpdateUserPasswordResult): UpdateUserPasswordFailure | UpdateUserPasswordSuccess {
    if (result.type === UpdateUserPasswordSuccess.name) {
      return result as UpdateUserPasswordSuccess;
    } else if (result.type === UpdateUserPasswordFailure.name) {
      return result as UpdateUserPasswordFailure;
    } else {
      return this.generateFailure("Invalid Type in Result");
    }
  }

  request(request: UpdateUserPasswordRequest): Promise<UpdateUserPasswordResponse> {
    return this.updateUserPassword(request);
  }

  async updateUserPassword(request: UpdateUserPasswordRequest): Promise<UpdateUserPasswordResponse> {
    
    
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return this.generateErrorResponse("Missing User Service URL", 500);

    return await internalAxiosRequest(
      uri + this.path,
      this.getMethod(),
      request.toJson(),
      this.parseResponse,
      this.parseResult,
      this.generateResponse,
      this.generateFailure,
    );
  }
}

export class UpdateUserPasswordRoute implements RouteDefinition<
  UpdateUserPasswordResponse,
  UpdateUserPasswordFailure
> {
  constructor() {
    this.internalRoute = new InternalUpdateUserPasswordRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }
  internalRoute: InternalRoute<any, any, any, any, any, any>;

  path: string = UserServiceConfig.SERVICE_PATH;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => UpdateUserPasswordFailure;
  forward: (req: AuthedRequest) => Promise<UpdateUserPasswordResponse> = (
    req: AuthedRequest,
  ) => {
    const id = req.user!!.id;
    let request = this.internalRoute.service.parseRequest(req.body.toString()) 
    request = request.copy(id);
    return this.internalRoute.request(request);
  };
  respond: (req: AuthedRequest, res: ExpressResponse) => void = async (
    req,
    res,
  ) => {
    const response = await this.forward(req);
    res.status(response.code).type("json").send(response.toJson());
  };
}