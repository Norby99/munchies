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
  LoginUserRequest,
  LoginUserAPI,
  LoginUserResponse,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest, injectCookie, parseAuthRoleString } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class LoginUserRoute
  extends LoginUserAPI<LoginUserResponse | ErrorResponse>
  implements SimpleRoute<LoginUserResponse>
{
  constructor() {
    super();
    let api = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = null;
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async loginUser(
    req: LoginUserRequest,
  ): Promise<LoginUserResponse | ErrorResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing User Service URL", 500),
      );

    const response = request<LoginUserResponse>(
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
    ) => Promise<LoginUserResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const bodyStr =
          typeof req.body === "string" ? req.body : JSON.stringify(req.body);
        const loginReq = this.parseRequest(bodyStr);
        return this.loginUser(loginReq);
      } catch (err: any) {
        return new ErrorResponse("LoginUser forward: \n" + String(err), 500);
      }
    },
    respond: async (req: Request, res: Response) => {
      const response = await this.forward(req as AuthedRequest);
      if (!(response instanceof ErrorResponse)) {
        try {
          const loginResult = (response as any).result ?? response;
          const roleVal =
            typeof loginResult.role === "string"
              ? parseAuthRoleString(loginResult.role)
              : loginResult.role;
          injectCookie(res, {
            id: loginResult.id,
            role: roleVal,
          });
        } catch (e: any) {
          res
            .status(500)
            .type("json")
            .send(
              new ErrorResponse(
                "Something went wrong during cookie injection: " + String(e),
                500,
              ).toJson(),
            );
          return;
        }
      }

      const code = (response as any).code ?? 200;
      res.status(code).type("json").send(response.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
