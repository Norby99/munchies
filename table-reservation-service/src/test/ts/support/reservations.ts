import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { CUSTOMER, RESTAURANT, at } from "./fakes";

/** An accepted two-hour reservation, by default for two at table 1 from 19:00. */
export function acceptedReservation(
  overrides: {
    time?: string;
    tableNumber?: number;
    guests?: number;
    customerId?: string;
    restaurantId?: string;
  } = {},
): TableReservation {
  return TableReservation.request(
    overrides.restaurantId ?? RESTAURANT,
    overrides.customerId ?? CUSTOMER,
    TimeSlot.startingAt(at(overrides.time ?? "19:00"), 120),
    overrides.guests ?? 2,
  ).accept(overrides.tableNumber ?? 1);
}
