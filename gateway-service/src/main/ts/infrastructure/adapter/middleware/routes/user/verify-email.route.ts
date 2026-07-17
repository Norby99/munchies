import {
  HttpMethod,
  AuthRole,
  newId,
} from "munchies-commons/kotlin/commons-modules";
import { InternalRoute, RouteDefinition } from "../route-definition";
import { internalAxiosRequest } from "../internal-client";
import { AuthedRequest, injectCookie, parseAuthRoleString } from "../../auth";
import { Response } from "express";
import {
  EmailVerificationAPI,
  VerifyEmailRequest,
  VerifyEmailResponse,
  VerifyEmailResult,
  VerifyEmailFailure,
  VerifyEmailSuccess,
  verifyEmailRequestFromJson,
  verifyEmailResponseFromJson,
} from "munchies-user-service-shared/kotlin/user-modules";

class InternalVerifyEmailRoute
  extends EmailVerificationAPI
  implements
    InternalRoute<
      EmailVerificationAPI,
      VerifyEmailRequest,
      VerifyEmailResponse,
      VerifyEmailResult,
      VerifyEmailSuccess,
      VerifyEmailFailure
    >
{
  constructor() {
    super();
    this.service = this;
    this.path = this.service.getPath();
    this.authRole = null;
    this.method = this.service.getMethod();
  }
  service: EmailVerificationAPI;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;

  generateErrorResponse(reason: string, code: number): VerifyEmailResponse {
    return this.generateResponse(this.generateFailure(reason), code);
  }

  parseResult(
    result: VerifyEmailResult
  ): VerifyEmailSuccess | VerifyEmailFailure {
    if (result.type === VerifyEmailSuccess.name) {
      return result as VerifyEmailSuccess;
    } else if (result.type === VerifyEmailFailure.name) {
      return result as VerifyEmailFailure;
    } else {
      return this.generateFailure("Invalid result type");
    }
  }
  async verifyEmail(request: VerifyEmailRequest): Promise<VerifyEmailResponse> {
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
  request(request: VerifyEmailRequest): Promise<VerifyEmailResponse> {
    return this.verifyEmail(request);
  }
}

export class VerifyEmailRoute
  implements RouteDefinition<VerifyEmailResponse, VerifyEmailFailure>
{
  constructor() {
    this.internalRoute = new InternalVerifyEmailRoute();
    this.path = this.internalRoute.path;
    this.method = this.internalRoute.method;
    this.authRole = this.internalRoute.authRole;
    this.onAuthFail = this.internalRoute.service.generateFailure;
  }

  internalRoute: InternalRoute<
    any,
    VerifyEmailRequest,
    VerifyEmailResponse,
    any,
    any,
    any
  >;
  path: string;

  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => VerifyEmailFailure;
  forward: (req: AuthedRequest) => Promise<VerifyEmailResponse> = (req) => {
    let request = this.internalRoute.service.parseRequest(req.body.toString());
    return this.internalRoute.request(request);
  };
  respond: (req: AuthedRequest, res: Response) => void = async (req, res) => {
    const response = await this.forward(req);
    res.status(response.code).type("json").send(response.toJson());
  };
}
