import { Response as ExpressResponse } from "express";
import { HttpMethod, AuthRole } from "munchies-commons/kotlin/commons-modules";
import {
  DeleteUserRequest,
  DeleteUserAPI,
  DeleteUserResponse,
  DeleteUserFailure,
  DeleteUserResult,
  DeleteUserSuccess,
  UserServiceConfig,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { RouteDefinition, InternalRoute } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { fillPath } from "../routes";
class InternalDeleteUserRoute
  extends DeleteUserAPI
  implements
    InternalRoute<
      DeleteUserAPI,
      DeleteUserRequest,
      DeleteUserResponse,
      DeleteUserResult,
      DeleteUserSuccess,
      DeleteUserFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = this.service.getRequiredAuthRole();
    this.method = this.service.getMethod();
  }
  service: DeleteUserAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): DeleteUserResponse {
    return new DeleteUserResponse(this.generateFailure(reason), code);
  }
  parseResult(result: DeleteUserResult): DeleteUserFailure | DeleteUserSuccess {
    if (result.type === DeleteUserSuccess.name) {
      return result as DeleteUserSuccess;
    } else if (result.type === DeleteUserFailure.name) {
      return result as DeleteUserFailure;
    } else {
      return this.generateFailure("Invalid Type in Result");
    }
  }

  request(request: DeleteUserRequest): Promise<DeleteUserResponse> {
    return this.deleteUser(request.userId);
  }

  async deleteUser(id: string): Promise<DeleteUserResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return this.generateErrorResponse("Missing User Service URL", 500);

    return await internalAxiosRequest(
      fillPath(uri + this.path, id),
      this.getMethod(),
      "",
      this.parseResponse,
      this.parseResult,
      this.generateResponse,
      this.generateFailure
    );
  }
}

export class DeleteUserRoute
  implements RouteDefinition<DeleteUserResponse, DeleteUserFailure>
{
  constructor() {
    this.internalRoute = new InternalDeleteUserRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }
  internalRoute: InternalRoute<any, any, any, any, any, any>;

  path: string = UserServiceConfig.SERVICE_PATH;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => DeleteUserFailure;
  forward: (req: AuthedRequest) => Promise<DeleteUserResponse> = (
    req: AuthedRequest
  ) => {
    const id = req.user!!.id;
    return this.internalRoute.request(new DeleteUserRequest(id));
  };
  respond: (req: AuthedRequest, res: ExpressResponse) => void = async (
    req,
    res
  ) => {
    const response = await this.forward(req);
    res.status(response.code).type("json").send(response.toJson());
  };
}
