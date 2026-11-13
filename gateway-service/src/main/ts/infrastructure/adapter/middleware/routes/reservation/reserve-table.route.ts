import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import {
  ReserveTableAPI,
  ReserveTableRequest,
  TableReservationResponse,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";
import { reservationServiceUrl } from "./reservation-service";

/**
 * `POST /reservations/`: a customer reserves a table. The gateway only stamps the caller's id
 * from the auth token onto the request (overriding any id in the body) and forwards it; whether
 * a table is free is decided by table-reservation-service.
 */
export class ReserveTableRoute
  extends ReserveTableAPI<TableReservationResponse | ErrorResponse>
  implements SimpleRoute<TableReservationResponse>
{
  constructor() {
    super();
    const api: ReserveTableAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async reserveTable(
    reserveRequest: ReserveTableRequest,
  ): Promise<TableReservationResponse | ErrorResponse> {
    const uri = reservationServiceUrl();
    if (uri instanceof ErrorResponse) return uri;

    return request<TableReservationResponse>(
      uri + this.path,
      this.method,
      reserveRequest.toJson(),
      this.parseResponse,
      this.parseError,
    );
  }

  private handler: {
    forward: (req: AuthedRequest) => Promise<TableReservationResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const reserveRequest = this.parseRequest(String(req.body)).addId(req.user!!.id);
        return this.reserveTable(reserveRequest);
      } catch (err: any) {
        // A body that does not decode as ReserveTableRequest is the caller's fault.
        return new ErrorResponse("ReserveTable forward: \n" + String(err), 400);
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
