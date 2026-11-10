import { Booking } from "../model/booking";

/** Persistence port for the booking ledger. In-memory adapter today; Mongo is a drop-in. */
export interface BookingRepository {
  save(booking: Booking): Promise<void>;
  update(booking: Booking): Promise<void>;
  findById(id: string): Promise<Booking | null>;
  findByOrderId(orderId: string): Promise<Booking | null>;
  /** Sum of kitchen weight of active bookings per slot-start, restricted to the given slot-starts. */
  sumActiveWeightBySlot(restaurantId: string, slotStartsEpochMs: number[]): Promise<Map<number, number>>;
  listBySlotRange(restaurantId: string, fromEpochMs: number, toEpochMs: number): Promise<Booking[]>;
  /** CONFIRMED bookings whose prepareAt has passed — candidates for auto-advance. */
  findDueForPrep(nowEpochMs: number): Promise<Booking[]>;
}
