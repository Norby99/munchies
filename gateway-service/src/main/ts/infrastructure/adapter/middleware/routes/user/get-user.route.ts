import {
  Response as ExpressResponse,
  Request,
  RequestHandler,
  Response,
} from "express";
import {
  HttpMethod,
  AuthRole,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
  GetUserRequest,
  GetUserAPI,
  GetUserResponse,
  UserServiceConfig,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { axiosClient, request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";
export class GetUserRoute
  extends GetUserAPI<GetUserResponse | ErrorResponse>
  implements SimpleRoute<GetUserResponse>
{
  constructor() {
    super();
    let api: GetUserAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async getUser(id: string): Promise<GetUserResponse | ErrorResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri) return Promise.resolve(new ErrorResponse("Missing User Service URL", 500));

    const response = request<GetUserResponse>(
      fillPath(uri + this.path + UserServiceConfig.GET_USER_PATH, id),
      this.method,
      "",
      this.parseResponse,
      this.parseError,
    );

    return response;
  }

  private handler: {
    forward: (req: AuthedRequest) => Promise<GetUserResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
      forward: async (req: AuthedRequest) => {
      try {
        return this.getUser(req.user!!.id);
      }
      catch (err: any) {
        return new ErrorResponse("GetUser forward: \n" + String(err), 500);
      }
    },
      respond: async (req: Request, res: Response) => {
        const result = await this.forward(req as AuthedRequest);
        res.status(result.code).type("json").send(result.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
