import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import {
  CancelTableReservationAPI,
  CancelTableReservationRequest,
  TableReservationResponse,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";
import { reservationServiceUrl } from "./reservation-service";

/**
 * `PATCH /reservations/{id}/cancel/`: a customer cancels their reservation. No body is
 * expected from the client: the request sent on carries only the caller's id, taken from the
 * auth token, and table-reservation-service refuses anyone but the reservation's customer.
 */
export class CancelTableReservationRoute
  extends CancelTableReservationAPI<TableReservationResponse | ErrorResponse>
  implements SimpleRoute<TableReservationResponse>
{
  constructor() {
    super();
    const api: CancelTableReservationAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async cancelTableReservation(
    id: string,
    cancelRequest: CancelTableReservationRequest,
  ): Promise<TableReservationResponse | ErrorResponse> {
    const uri = reservationServiceUrl();
    if (uri instanceof ErrorResponse) return uri;

    return request<TableReservationResponse>(
      fillPath(uri + this.path, encodeURIComponent(id)),
      this.method,
      cancelRequest.toJson(),
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
        return this.cancelTableReservation(
          req.params.id,
          new CancelTableReservationRequest(req.user!!.id),
        );
      } catch (err: any) {
        return new ErrorResponse("CancelTableReservation forward: \n" + String(err), 500);
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
