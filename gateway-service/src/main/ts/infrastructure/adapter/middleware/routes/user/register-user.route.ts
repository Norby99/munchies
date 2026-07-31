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
  RegisterUserRequest,
  RegisterUserAPI,
  RegisterUserResponse,
  UserServiceConfig,
  UserDTO,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest, injectCookie, parseAuthRoleString } from "../../auth";
import { axiosClient, request } from "../internal-client";
import { fillPath } from "../routes";
import { createSimpleRoute, SimpleRoute } from "../simple-route";

export class RegisterUserRoute
  extends RegisterUserAPI<RegisterUserResponse | ErrorResponse>
  implements SimpleRoute<RegisterUserResponse>
{
  constructor() {
    super();
    let api: RegisterUserAPI = this;

    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = null;
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async registerUser(
    req: RegisterUserRequest,
  ): Promise<RegisterUserResponse | ErrorResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing User Service URL", 500),
      );

    const response = request<RegisterUserResponse>(
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
    ) => Promise<RegisterUserResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const registerReq = this.parseRequest(String(req.body));
        return this.registerUser(registerReq);
      } catch (err: any) {
        return new ErrorResponse("RegisterUser forward: \n" + String(err), 500);
      }
    },
    respond: async (req: Request, res: Response) => {
      const response = await this.forward(req as AuthedRequest);
      const result = response.result;
      if (result instanceof UserDTO) {
        try {
          injectCookie(res, {
            id: result.id,
            role: parseAuthRoleString(result.role),
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

      res.status(response.code).type("json").send(response.toJson());
    },
  };

  forward = this.handler.forward;
  respond = this.handler.respond;
}
