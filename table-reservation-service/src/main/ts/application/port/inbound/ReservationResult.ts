import { TableReservation } from "@main/domain/model/TableReservation";

/**
 * Why a reservation use case did not succeed. Every use case returns a subset of these.
 * - INVALID_REQUEST: the input breaks a rule (missing ids, no guests, a start in the past or
 *   off the arrival-time grid).
 * - RESTAURANT_NOT_FOUND: no such restaurant.
 * - RESERVATION_NOT_FOUND: no such reservation (or not one the requester may see).
 * - FORBIDDEN: the requester is not allowed to act on the reservation or restaurant.
 * - NO_TABLE_AVAILABLE: no table of the restaurant can seat the party in that time slot.
 * - NOT_CHANGEABLE: the reservation can no longer change: canceled, or already started.
 * - RESTAURANT_UNAVAILABLE: restaurant-service could not be asked; retrying may succeed.
 */
export type ReservationFailure =
  | { type: "INVALID_REQUEST"; reason: string }
  | { type: "RESTAURANT_NOT_FOUND"; reason: string }
  | { type: "RESERVATION_NOT_FOUND"; reason: string }
  | { type: "FORBIDDEN"; reason: string }
  | { type: "NO_TABLE_AVAILABLE"; reason: string }
  | { type: "NOT_CHANGEABLE"; reason: string }
  | { type: "RESTAURANT_UNAVAILABLE"; reason: string };

/** Outcome of a use case about one reservation. */
export type ReservationResult =
  | { type: "SUCCESS"; reservation: TableReservation }
  | ReservationFailure;

/** Outcome of a use case listing reservations. */
export type ReservationsResult =
  | { type: "SUCCESS"; reservations: TableReservation[] }
  | ReservationFailure;
