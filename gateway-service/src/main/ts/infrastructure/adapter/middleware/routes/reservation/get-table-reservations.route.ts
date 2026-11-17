import { Request, RequestHandler, Response } from "express";
import { AuthRole, ErrorResponse, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import {
  GetTableReservationsAPI,
  TableReservationServiceConfig,
  TableReservationsResponse,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { AuthedRequest } from "../../auth";
import { request } from "../internal-client";
import { SimpleRoute } from "../simple-route";
import { reservationServiceUrl } from "./reservation-service";

/**
 * `GET /reservations/`: lists reservations.
 *
 * Who is asking comes from the session token, never from the query string:
 * - a customer lists their own reservations, optionally at one restaurant (`?restaurantId=`);
 * - a manager lists the reservations at a restaurant (`?restaurantId=`, required), and
 *   table-reservation-service checks that they manage it.
 */
export class GetTableReservationsRoute
  extends GetTableReservationsAPI<TableReservationsResponse | ErrorResponse>
  implements SimpleRoute<TableReservationsResponse>
{
  constructor() {
    super();
    const api: GetTableReservationsAPI = this;
    this.path = api.getPath();
    this.method = api.getMethod();
    this.authRole = api.getRequiredAuthRole();
  }

  path: string;
  method: HttpMethod;
  authRole: AuthRole | null;

  async getTableReservations(
    customerId: string | null,
    restaurantId: string | null,
    managerId: string | null,
  ): Promise<TableReservationsResponse | ErrorResponse> {
    const uri = reservationServiceUrl();
    if (uri instanceof ErrorResponse) return uri;

    const params = new URLSearchParams();
    if (customerId) params.append(TableReservationServiceConfig.CUSTOMER_ID_QUERY, customerId);
    if (restaurantId) {
      params.append(TableReservationServiceConfig.RESTAURANT_ID_QUERY, restaurantId);
    }
    if (managerId) params.append(TableReservationServiceConfig.MANAGER_ID_QUERY, managerId);
    const queryString = params.toString();

    return request<TableReservationsResponse>(
      uri + this.path + (queryString ? `?${queryString}` : ""),
      this.method,
      "",
      this.parseResponse,
      this.parseError,
    );
  }

  private handler: {
    forward: (req: AuthedRequest) => Promise<TableReservationsResponse | ErrorResponse>;
    respond: RequestHandler;
  } = {
    forward: async (req: AuthedRequest) => {
      try {
        const restaurantId = req.query.restaurantId ? String(req.query.restaurantId) : null;
        const isCustomer = req.user?.role.name === AuthRole.CUSTOMER.name;
        return isCustomer
          ? this.getTableReservations(req.user!!.id, restaurantId, null)
          : this.getTableReservations(null, restaurantId, req.user!!.id);
      } catch (err: any) {
        return new ErrorResponse("GetTableReservations forward: \n" + String(err), 500);
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
