import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import axios, { AxiosRequestConfig } from "axios";
import { AuthedRequest } from "../../auth";
import { SimpleRoute } from "../simple-route";

/**
 * scheduler-service is a standalone Express service (not a Kotlin-shared contract),
 * so its gateway routes are thin pass-throughs: fill the path params, keep the query
 * string, forward method + body to SCHEDULER_SERVICE_URL, relay the response verbatim.
 *
 * `injectCustomer` mirrors what the order routes do — the customer id comes from the
 * verified JWT, never from the client body.
 */
export type SchedulerBodyMode = "none" | "passthrough" | "injectCustomer";

const proxy = axios.create({
  timeout: 12_000,
  headers: { "Content-Type": "application/json" },
  validateStatus: () => true,
  transformResponse: [(data) => data],
});

class RawResult {
  constructor(
    readonly code: number,
    private readonly body: string,
  ) {}
  toJson(): string {
    return this.body;
  }
}

export class SchedulerProxyRoute implements SimpleRoute<RawResult> {
  constructor(
    public readonly path: string,
    public readonly method: HttpMethod,
    public readonly authRole: AuthRole | null,
    private readonly bodyMode: SchedulerBodyMode = "none",
  ) {}

  private targetUrl(req: Request): string | null {
    const base = process.env.SCHEDULER_SERVICE_URL;
    if (!base) return null;
    let path = this.path;
    for (const [key, value] of Object.entries(req.params)) {
      path = path.replace(`{${key}}`, encodeURIComponent(String(value)));
    }
    const qsIndex = req.originalUrl.indexOf("?");
    const qs = qsIndex >= 0 ? req.originalUrl.slice(qsIndex) : "";
    return base.replace(/\/+$/, "") + path + qs;
  }

  private buildBody(req: AuthedRequest): string | undefined {
    if (this.bodyMode === "none") return undefined;
    const raw = req.body ? String(req.body) : "";
    if (this.bodyMode === "passthrough") return raw || "{}";
    const parsed = raw ? JSON.parse(raw) : {};
    parsed.customerId = req.user?.id;
    return JSON.stringify(parsed);
  }

  forward = async (req: AuthedRequest): Promise<RawResult | ErrorResponse> => {
    const url = this.targetUrl(req);
    if (!url) return new ErrorResponse("Missing Scheduler Service URL", 500);

    let data: string | undefined;
    try {
      data = this.buildBody(req);
    } catch (err) {
      return new ErrorResponse("Scheduler proxy: bad JSON body \n" + String(err), 400);
    }

    const config: AxiosRequestConfig = {
      url,
      method: this.method.name.toLowerCase() as AxiosRequestConfig["method"],
      data,
    };
    try {
      const res = await proxy.request(config);
      return new RawResult(
        res.status,
        typeof res.data === "string" ? res.data : JSON.stringify(res.data),
      );
    } catch (err) {
      return new ErrorResponse("Scheduler proxy request failed: \n" + String(err), 502);
    }
  };

  respond: RequestHandler = async (req: Request, res: Response) => {
    const result = await this.forward(req as AuthedRequest);
    res.status(result.code).type("json").send(result.toJson());
  };
}
