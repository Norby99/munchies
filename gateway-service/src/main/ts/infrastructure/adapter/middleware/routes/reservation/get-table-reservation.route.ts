import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import {
  GetTableReservationAPI,
  TableReservationResponse,
  TableReservationServiceConfig,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { fillPath } from "../routes";
import { SimpleRoute } from "../simple-route";
import { reservationServiceUrl } from "./reservation-service";

/**
 * `GET /reservations/{id}/`: reads one reservation. A customer only gets their own: the owner
 * filter is taken from the session token, and table-reservation-service answers 404 for
 * anybody else's reservation.
 */
export class GetTableReservationRoute
  extends GetTableReservationAPI<TableReservationResponse | ErrorResponse>
  implements SimpleRoute<TableReservationResponse>
{
  constructor() {
    super();
    const api: GetTableReservationAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async getTableReservation(
    id: string,
    customerId: string | null,
  ): Promise<TableReservationResponse | ErrorResponse> {
    const uri = reservationServiceUrl();
    if (uri instanceof ErrorResponse) return uri;

    const params = new URLSearchParams();
    if (customerId) params.append(TableReservationServiceConfig.CUSTOMER_ID_QUERY, customerId);
    const queryString = params.toString();

    return request<TableReservationResponse>(
      fillPath(uri + this.path, encodeURIComponent(id)) + (queryString ? `?${queryString}` : ""),
      this.method,
      "",
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
        const isCustomer = req.user?.role.name === AuthRole.CUSTOMER.name;
        return this.getTableReservation(req.params.id, isCustomer ? req.user!!.id : null);
      } catch (err: any) {
        return new ErrorResponse("GetTableReservation forward: \n" + String(err), 500);
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
