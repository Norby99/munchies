import { RequestHandler } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import { AuthedRequest } from "../auth";

export interface SimpleRoute<Response> {
  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;
  forward: (req: AuthedRequest) => Promise<Response | ErrorResponse>;
  respond: RequestHandler;
}

