import { InternalRoute, RouteDefinition } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import {
  LoginUserResponse,
  LoginUserFailure,
  LoginUserAPI,
  LoginUserRequest,
  LoginUserResult,
  LoginUserSuccess,
  loginUserRequestFromJson,
  loginUserResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthRole, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import { com } from "munchies-user-service-shared";
import { AuthedRequest, injectCookie, parseAuthRoleString } from "../../auth";
import { RequestHandler, Response } from "express";
import { ParamsDictionary } from "express-serve-static-core";
import { ParsedQs } from "qs";
class InternalLoginUserRoute
  extends LoginUserAPI
  implements
    InternalRoute<
      LoginUserAPI,
      LoginUserRequest,
      LoginUserResponse,
      LoginUserResult,
      LoginUserSuccess,
      LoginUserFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = this.service.getRequiredAuthRole();
    this.method = this.service.getMethod();
  }
  authRole: AuthRole | null;
  method: HttpMethod;
  path: string;
  service: LoginUserAPI;

  generateErrorResponse(reason: string, code: number): LoginUserResponse {
    return this.generateResponse(this.generateFailure(reason), code);
  }
  generateFailure(reason: string): LoginUserFailure {
    return new LoginUserFailure(reason);
  }
  generateResponse(result: LoginUserResult, code: number): LoginUserResponse {
    return new LoginUserResponse(result, code);
  }
  parseRequest(json: string): LoginUserRequest {
    return loginUserRequestFromJson(json);
  }
  parseResponse(json: string): LoginUserResponse {
    return loginUserResponseFromJson(json);
  }
  parseResult(result: LoginUserResult): LoginUserSuccess | LoginUserFailure {
    if (result.type === LoginUserSuccess.name) {
      return result as LoginUserSuccess;
    } else if (result.type === LoginUserFailure.name) {
      return result as LoginUserFailure;
    } else {
      return this.generateFailure("Invalid Type in Result");
    }
  }
  async loginUser(request: LoginUserRequest): Promise<LoginUserResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return this.generateErrorResponse("Missing User Service URL", 500);

    return await internalAxiosRequest(uri + this.path, this, request);
  }
  request(request: LoginUserRequest): Promise<LoginUserResponse> {
    return this.loginUser(request);
  }
}

export class LoginUserRoute implements RouteDefinition<
  LoginUserResponse,
  LoginUserFailure
> {
  constructor() {
    this.internalRoute = new InternalLoginUserRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.generateFailure;
  }
  internalRoute: InternalRoute<
    any,
    LoginUserRequest,
    LoginUserResponse,
    LoginUserResult,
    LoginUserSuccess,
    LoginUserFailure
  >;
  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => LoginUserFailure;

  forward: (req: AuthedRequest) => Promise<LoginUserResponse> = (req) => {
    const request = this.internalRoute.parseRequest(req.body);
    return this.internalRoute.request(request);
  };
  
  respond: (req: AuthedRequest, res: Response) => void = async (req, res) => {
    const response = await this.forward(req);
    switch (response.result.type) {
      case LoginUserSuccess.name:
        const result = response.result as LoginUserSuccess
        injectCookie(res, {id: result.id, role: result.role });
        break;  
    }
    res.status(response.code).type("json").send(response.toJson());
  };
}
