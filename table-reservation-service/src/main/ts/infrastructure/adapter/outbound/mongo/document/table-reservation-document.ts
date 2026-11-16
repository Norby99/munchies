import { Schema, model, HydratedDocument } from "mongoose";
import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";

/**
 * Mongo form of a table reservation.
 *
 * @property occupancy What the reservation holds while it is accepted: one key per step of the
 * arrival-time grid of its slot, each naming its table (see table-reservation-factory). Empty
 * for a reservation that holds no table.
 */
interface _TableReservationDocument {
  _id: string;
  restaurantId: string;
  customerId: string;
  start: Date;
  end: Date;
  numberOfGuests: number;
  tableNumber: number | null;
  status: string;
  occupancy: string[];
}

const TableReservationSchema = new Schema<_TableReservationDocument>(
  {
    _id: { type: String, required: true },
    restaurantId: { type: String, required: true },
    customerId: { type: String, required: true, index: true },
    start: { type: Date, required: true },
    end: { type: Date, required: true },
    numberOfGuests: { type: Number, required: true },
    tableNumber: { type: Number, default: null },
    status: {
      type: String,
      required: true,
      enum: ReservationStatus.values().map((status) => status.name),
    },
    occupancy: { type: [String], default: [] },
  },
  { _id: false, timestamps: true },
);

// The guard against double booking. `occupancy` is an array, so this is a multikey index: each
// (restaurant, table, grid step) key may belong to one accepted reservation only, and MongoDB
// enforces it atomically on every write. Two overlapping reservations of the same table share
// at least one key, so the second write fails with a duplicate-key error whatever the timing.
// Partial: canceled reservations keep their document but must not hold their table.
TableReservationSchema.index(
  { restaurantId: 1, occupancy: 1 },
  {
    unique: true,
    partialFilterExpression: { status: ReservationStatus.ACCEPTED.name },
  },
);

// Serves the availability lookup (accepted reservations of a restaurant overlapping a slot)
// and the restaurant's list ordered by start.
TableReservationSchema.index({ restaurantId: 1, start: 1 });

export const TableReservationModel = model<_TableReservationDocument>(
  "TableReservation",
  TableReservationSchema,
);
export type TableReservationDocument = HydratedDocument<_TableReservationDocument>;
export type TableReservationDocumentData = _TableReservationDocument;
