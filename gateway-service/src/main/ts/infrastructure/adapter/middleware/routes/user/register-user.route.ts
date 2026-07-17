import {
  RegisterUserAPI,
  RegisterUserFailure,
  RegisterUserRequest,
  RegisterUserResponse,
  registerUserResponseFromJson,
  RegisterUserResult,
  RegisterUserSuccess,
  UserDTO,
} from "munchies-user-service-shared/kotlin/user-modules";
import {
  HttpMethod,
  AuthRole,
  newId,
} from "munchies-commons/kotlin/commons-modules";
import { InternalRoute, RouteDefinition } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { AuthedRequest, injectCookie, parseAuthRoleString } from "../../auth";
import { Response } from "express";

class InternalRegisterUserRoute
  extends RegisterUserAPI
  implements
    InternalRoute<
      RegisterUserAPI,
      RegisterUserRequest,
      RegisterUserResponse,
      RegisterUserResult,
      RegisterUserSuccess,
      RegisterUserFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = null;
    this.method = this.service.getMethod();
  }
  service: RegisterUserAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): RegisterUserResponse {
    return this.generateResponse(this.generateFailure(reason), code);
  }

  parseResult(
    result: RegisterUserResult
  ): RegisterUserSuccess | RegisterUserFailure {
    if (result.type === RegisterUserSuccess.name) {
      return result as RegisterUserSuccess;
    } else if (result.type === RegisterUserFailure.name) {
      return result as RegisterUserFailure;
    } else {
      return this.generateFailure("Invalid result type");
    }
  }
  async registerUser(
    request: RegisterUserRequest
  ): Promise<RegisterUserResponse> {
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
      this.generateFailure
    );
  }
  request(request: RegisterUserRequest): Promise<RegisterUserResponse> {
    return this.registerUser(request);
  }
}

export class RegisterUserRoute
  implements RouteDefinition<RegisterUserResponse, RegisterUserFailure>
{
  constructor() {
    this.internalRoute = new InternalRegisterUserRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }

  internalRoute: InternalRoute<
    any,
    RegisterUserRequest,
    RegisterUserResponse,
    any,
    any,
    any
  >;
  path: string;

  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => RegisterUserFailure;
  forward: (req: AuthedRequest) => Promise<RegisterUserResponse> = (req) => {
    let request = this.internalRoute.service.parseRequest(req.body.toString());
    return this.internalRoute.request(request);
  };
  respond: (req: AuthedRequest, res: Response) => void = async (req, res) => {
    const response = await this.forward(req);
    switch (response.result.type) {
      case RegisterUserSuccess.name:
        const result = response.result as RegisterUserSuccess;
        const id = result.user.id;
        try {
          const role = parseAuthRoleString(result.user.role);
          injectCookie(res, { id: id, role: role });
        } catch (_: any) {
          res
            .status(500)
            .type("json")
            .send(
              this.internalRoute
                .generateErrorResponse("Invalid Role", 500)
                .toJson()
            );
        }
        break;
    }
    res.status(response.code).type("json").send(response.toJson());
  };
}
