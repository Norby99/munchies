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
  UpdateUserInfoRequest,
  UpdateUserInfoAPI,
  UpdateUserInfoResponse,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class UpdateUserInfoRoute
  extends UpdateUserInfoAPI<UpdateUserInfoResponse | ErrorResponse>
  implements SimpleRoute<UpdateUserInfoResponse>
{
  constructor() {
    super();
    let api = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async updateUserInfo(
    req: UpdateUserInfoRequest,
  ): Promise<UpdateUserInfoResponse | ErrorResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing User Service URL", 500),
      );

    const response = request<UpdateUserInfoResponse>(
      uri + this.path,
      this.method,
      req.toJson(),
      this.parseResponse,
      this.parseError,
    );

    return response;
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<UpdateUserInfoResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        let bodyObj =
          typeof req.body === "string" ? JSON.parse(req.body) : req.body || {};
        if (bodyObj.user) {
          bodyObj.user.id = req.user!!.id;
          if (!bodyObj.user.role && req.user?.role) {
            bodyObj.user.role = req.user.role.name;
          }
        } else {
          bodyObj.id = req.user!!.id;
          if (!bodyObj.role && req.user?.role) {
            bodyObj.role = req.user.role.name;
          }
          bodyObj = { user: bodyObj };
        }
        const updateReq = this.parseRequest(JSON.stringify(bodyObj));
        return this.updateUserInfo(updateReq);
      } catch (err: any) {
        return new ErrorResponse(
          "UpdateUserInfo forward: \n" + String(err),
          500,
        );
      }
    },
    respond: async (req: Request, res: Response) => {
      const result = await this.forward(req as AuthedRequest);
      const code = (result as any).code ?? 200;
      res.status(code).type("json").send(result.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
