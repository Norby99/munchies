import { SimpleRoute } from "../simple-route";
import { CancelTableReservationRoute } from "./cancel-table-reservation.route";
import { GetTableReservationRoute } from "./get-table-reservation.route";
import { GetTableReservationsRoute } from "./get-table-reservations.route";
import { ModifyTableReservationRoute } from "./modify-table-reservation.route";
import { ReserveTableRoute } from "./reserve-table.route";

export const reservationRoutes: SimpleRoute<any>[] = [
  new ReserveTableRoute(),
  new GetTableReservationsRoute(),
  new GetTableReservationRoute(),
  new ModifyTableReservationRoute(),
  new CancelTableReservationRoute(),
];
