import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { ReservationId } from "@main/domain/model/ReservationId";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import {
  ReservationFilter,
  SaveReservationResult,
  TableReservationRepository,
} from "@main/domain/port/table-reservation-repository";
import { TableReservationModel } from "@main/infrastructure/adapter/outbound/mongo/document/table-reservation-document";
import { TableReservationFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/table-reservation-factory";

/** MongoDB's error code for a write violating a unique index. */
const DUPLICATE_KEY = 11000;

function isDuplicateKey(error: unknown): boolean {
  return (
    typeof error === "object" &&
    error !== null &&
    (error as { code?: unknown }).code === DUPLICATE_KEY
  );
}

/** MongoDB adapter of the reservation store. */
export class TableReservationMongoRepository implements TableReservationRepository {
  async findById(id: ReservationId): Promise<TableReservation | null> {
    const document = await TableReservationModel.findById(id.value).lean();
    return document ? TableReservationFactory.toDomain(document) : null;
  }

  async find(filter: ReservationFilter): Promise<TableReservation[]> {
    const query: Record<string, string> = {};
    if (filter.customerId) query.customerId = filter.customerId;
    if (filter.restaurantId) query.restaurantId = filter.restaurantId;
    const documents = await TableReservationModel.find(query).sort({ start: 1 }).lean();
    return documents.map(TableReservationFactory.toDomain);
  }

  async findAcceptedOverlapping(
    restaurantId: string,
    slot: TimeSlot,
  ): Promise<TableReservation[]> {
    const documents = await TableReservationModel.find({
      restaurantId,
      status: ReservationStatus.ACCEPTED.name,
      start: { $lt: slot.end },
      end: { $gt: slot.start },
    }).lean();
    return documents.map(TableReservationFactory.toDomain);
  }

  /**
   * Upserts the reservation in one atomic write. The unique index on the document's occupancy
   * keys (see table-reservation-document) rejects the write when another accepted reservation
   * already holds the table in an overlapping slot: that duplicate-key error is the
   * TABLE_TAKEN outcome, and leaves any previously stored version untouched.
   */
  async save(reservation: TableReservation): Promise<SaveReservationResult> {
    const document = TableReservationFactory.toDocument(reservation);
    try {
      await TableReservationModel.replaceOne({ _id: document._id }, document, {
        upsert: true,
      });
      return "SAVED";
    } catch (error: unknown) {
      if (isDuplicateKey(error)) return "TABLE_TAKEN";
      throw error;
    }
  }
}
