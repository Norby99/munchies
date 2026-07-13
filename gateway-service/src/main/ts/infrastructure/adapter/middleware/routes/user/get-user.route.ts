import { Response as ExpressResponse } from "express";
import { HttpMethod, AuthRole } from "munchies-commons/kotlin/commons-modules";
import {
  GetUserRequest,
  GetUserAPI,
  GetUserResponse,
  GetUserFailure,
  GetUserResult,
  GetUserSuccess,
  UserServiceConfig,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { RouteDefinition, InternalRoute } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { fillPath } from "../routes";
class InternalGetUserRoute
  extends GetUserAPI
  implements
    InternalRoute<
      GetUserAPI,
      GetUserRequest,
      GetUserResponse,
      GetUserResult,
      GetUserSuccess,
      GetUserFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = this.service.getRequiredAuthRole();
    this.method = this.service.getMethod();
  }
  service: GetUserAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): GetUserResponse {
    return new GetUserResponse(this.generateFailure(reason), code);
  }
  parseResult(result: GetUserResult): GetUserFailure | GetUserSuccess {
    if (result.type === GetUserSuccess.name) {
      return result as GetUserSuccess;
    } else if (result.type === GetUserFailure.name) {
      return result as GetUserFailure;
    } else {
      return this.generateFailure("Invalid Type in Result");
    }
  }

  request(request: GetUserRequest): Promise<GetUserResponse> {
    return this.getUser(request.id);
    
  }

  async getUser(id: string): Promise<GetUserResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return this.generateErrorResponse("Missing User Service URL", 500);

    return await internalAxiosRequest(
      fillPath(uri + this.path, id),
      this.getMethod(),
      "id",
      this.parseResponse,
      this.parseResult,
      this.generateResponse,
      this.generateFailure,
    );
  }
}

export class GetUserRoute implements RouteDefinition<
  GetUserResponse,
  GetUserFailure
> {
  constructor() {
    this.internalRoute = new InternalGetUserRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }
  internalRoute: InternalRoute<any, any, any, any, any, any>;

  path: string = UserServiceConfig.SERVICE_PATH;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => GetUserFailure;
  forward: (req: AuthedRequest) => Promise<GetUserResponse> = (
    req: AuthedRequest,
  ) => {
    const id = req.user!!.id;
    return this.internalRoute.request(new GetUserRequest(id));
  };
  respond: (req: AuthedRequest, res: ExpressResponse) => void = async (
    req,
    res,
  ) => {
    const response = await this.forward(req);
    res.status(response.code).type("json").send(response.toJson());
  };
}
