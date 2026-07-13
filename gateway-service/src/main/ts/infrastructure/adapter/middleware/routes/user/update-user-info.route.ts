
import { Response as ExpressResponse } from "express";
import { HttpMethod, AuthRole } from "munchies-commons/kotlin/commons-modules";
import {
  UpdateUserInfoRequest,
  UpdateUserInfoAPI,
  UpdateUserInfoResponse,
  UpdateUserInfoFailure,
  UpdateUserInfoResult,
  UpdateUserInfoSuccess,
  UserServiceConfig,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { RouteDefinition, InternalRoute } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { fillPath } from "../routes";
class InternalUpdateUserInfoRoute
  extends UpdateUserInfoAPI
  implements
    InternalRoute<
      UpdateUserInfoAPI,
      UpdateUserInfoRequest,
      UpdateUserInfoResponse,
      UpdateUserInfoResult,
      UpdateUserInfoSuccess,
      UpdateUserInfoFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = this.service.getRequiredAuthRole();
    this.method = this.service.getMethod();
  }
  service: UpdateUserInfoAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): UpdateUserInfoResponse {
    return new UpdateUserInfoResponse(this.generateFailure(reason), code);
  }
  parseResult(result: UpdateUserInfoResult): UpdateUserInfoFailure | UpdateUserInfoSuccess {
    if (result.type === UpdateUserInfoSuccess.name) {
      return result as UpdateUserInfoSuccess;
    } else if (result.type === UpdateUserInfoFailure.name) {
      return result as UpdateUserInfoFailure;
    } else {
      return this.generateFailure("Invalid Type in Result");
    }
  }

  request(request: UpdateUserInfoRequest): Promise<UpdateUserInfoResponse> {
    return this.updateUserInfo(request);
  }

  async updateUserInfo(request: UpdateUserInfoRequest): Promise<UpdateUserInfoResponse> {
    
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

export class UpdateUserInfoRoute implements RouteDefinition<
  UpdateUserInfoResponse,
  UpdateUserInfoFailure
> {
  constructor() {
    this.internalRoute = new InternalUpdateUserInfoRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }
  internalRoute: InternalRoute<any, any, any, any, any, any>;

  path: string = UserServiceConfig.SERVICE_PATH;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => UpdateUserInfoFailure;
  forward: (req: AuthedRequest) => Promise<UpdateUserInfoResponse> = (
    req: AuthedRequest,
  ) => {
    const id = req.user!!.id;
    let request = this.internalRoute.service.parseRequest(req.body.toString()) 
    request = request.copy(request.user.copy(id));
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