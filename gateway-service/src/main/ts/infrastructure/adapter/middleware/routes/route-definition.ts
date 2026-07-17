import {
  Request as ExpressRequest,
  Response as ExpressResponse,
  RequestHandler,
} from "express";
import {
  API,
  AuthRole,
  HttpMethod,
  JsonEncodable,
} from "munchies-commons/kotlin/commons-modules";
import { AuthedRequest } from "../auth";

export interface RouteDefinition<Response, Failure> {
  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => Failure;
  internalRoute: InternalRoute<any, any, any, any, any, any>;
  forward: (req: AuthedRequest) => Promise<Response>;
  respond: RequestHandler;
}

export interface InternalRoute<
  Service extends API,
  Request extends JsonEncodable,
  Response extends JsonEncodable,
  Result extends { type: string },
  Success extends Result,
  Failure extends Result & { reason: string }
> {
  service: Service;
  path: string;
  authRole: AuthRole | null;
  method: HttpMethod;
  parseResult(result: Result): Success | Failure;
  generateErrorResponse(reason: string, code: number): Response;
  request(request: Request): Promise<Response>;
}
