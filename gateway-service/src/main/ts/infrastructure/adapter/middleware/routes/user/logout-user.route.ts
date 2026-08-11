import { Request, RequestHandler, Response } from "express";
import {
  HttpMethod,
  AuthRole,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";
import {
  LogoutUserRequest,
  LogoutUserAPI,
  LogoutUserResponse,
} from "munchies-gateway-service-shared/kotlin/gateway-modules";
import { AuthedRequest, clearCookie } from "../../auth";
import { SimpleRoute } from "../simple-route";

export class LogoutUserRoute
  extends LogoutUserAPI<LogoutUserResponse | ErrorResponse>
  implements SimpleRoute<LogoutUserResponse>
{
  constructor() {
    super();
    let api: LogoutUserAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async logoutUser(
    req: LogoutUserRequest,
  ): Promise<LogoutUserResponse | ErrorResponse> {
    return new LogoutUserResponse("Logged out successfully", 200);
  }

  private handler: {
    forward: (
      req: AuthedRequest,
    ) => Promise<LogoutUserResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const logoutReq = this.parseRequest(String(req.body)).addId(
          req.user!!.id,
        );
        return this.logoutUser(logoutReq);
      } catch (err: any) {
        return new ErrorResponse("LogoutUser forward: \n" + String(err), 500);
      }
    },
    respond: async (req: Request, res: Response) => {
      const response = await this.forward(req as AuthedRequest);
      try {
        clearCookie(res);
      } catch (err: any) {
        res
          .status(500)
          .type("json")
          .send(
            new ErrorResponse(
              "Something went wrong during cookie clearing: " + String(err),
              500,
            ).toJson(),
          );
        return;
      }
      res.status(response.code).type("json").send(response.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
