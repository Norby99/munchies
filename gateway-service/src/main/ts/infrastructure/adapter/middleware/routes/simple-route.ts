import { RequestHandler } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import { AuthedRequest } from "../auth";

export interface SimpleRoute<Response> {
  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;
  onAuthFail: (msg: string) => ErrorResponse;
  forward: (req: AuthedRequest) => Promise<Response | ErrorResponse>;
  respond: RequestHandler;
}

export function createSimpleRoute<Request, Response>(
  parseRequest: (json: string) => Request,
  executor: (request: Request) => Promise<Response | ErrorResponse>,
  successStatus: number,
): Pick<SimpleRoute<Response>, "forward" | "respond"> {
  const forward = async (req: AuthedRequest): Promise<Response | ErrorResponse> => {
    try {
      const request = parseRequest(req.body.toString());
      return await executor(request);
    } catch (error: any) {
      if (error instanceof ErrorResponse) return error;
      return new ErrorResponse(String(error?.message ?? error));
    }
  };

  const respond: RequestHandler = async (_req, res) => {
    const result = await forward(_req as AuthedRequest);
    if (result instanceof ErrorResponse) {
      res.status(400).type("json").send(result.toJson());
    } else {
      res.status(successStatus).type("json").send((result as any).toJson());
    }
  };

  return { forward, respond };
}
