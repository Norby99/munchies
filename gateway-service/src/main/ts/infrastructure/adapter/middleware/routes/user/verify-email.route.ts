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
  VerifyEmailRequest,
  EmailVerificationAPI,
  VerifyEmailResponse,
} from "munchies-user-service-shared/kotlin/user-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";

export class VerifyEmailRoute
  extends EmailVerificationAPI<VerifyEmailResponse | ErrorResponse>
  implements SimpleRoute<VerifyEmailResponse>
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

  async verifyEmail(
    req: VerifyEmailRequest,
  ): Promise<VerifyEmailResponse | ErrorResponse> {
    const uri = process.env.USER_SERVICE_URL;
    if (!uri)
      return Promise.resolve(
        new ErrorResponse("Missing User Service URL", 500),
      );

    const response = request<VerifyEmailResponse>(
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
    ) => Promise<VerifyEmailResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const verifyReq = this.parseRequest(
          String(req.body)).addId(req.user!!.id);
        return this.verifyEmail(verifyReq);
      } catch (err: any) {
        return new ErrorResponse("VerifyEmail forward: \n" + String(err), 500);
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
