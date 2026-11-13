import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import {
  ModifyTableReservationAPI,
  ModifyTableReservationRequest,
  TableReservationResponse,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";
import { reservationServiceUrl } from "./reservation-service";

/**
 * `PUT /reservations/{id}/`: a customer moves their reservation or changes its party size. The
 * gateway stamps the caller's id from the auth token; table-reservation-service refuses anyone
 * but the customer who made the reservation.
 */
export class ModifyTableReservationRoute
  extends ModifyTableReservationAPI<TableReservationResponse | ErrorResponse>
  implements SimpleRoute<TableReservationResponse>
{
  constructor() {
    super();
    const api: ModifyTableReservationAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async modifyTableReservation(
    id: string,
    modifyRequest: ModifyTableReservationRequest,
  ): Promise<TableReservationResponse | ErrorResponse> {
    const uri = reservationServiceUrl();
    if (uri instanceof ErrorResponse) return uri;

    return request<TableReservationResponse>(
      fillPath(uri + this.path, encodeURIComponent(id)),
      this.method,
      modifyRequest.toJson(),
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
        const modifyRequest = this.parseRequest(String(req.body)).addId(req.user!!.id);
        return this.modifyTableReservation(req.params.id, modifyRequest);
      } catch (err: any) {
        // A body that does not decode as ModifyTableReservationRequest is the caller's fault.
        return new ErrorResponse("ModifyTableReservation forward: \n" + String(err), 400);
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
