import { Booking, isActive } from "../../domain/model/booking";
import { BookingRepository } from "../../domain/ports/bookingRepository";

/** Process-local booking ledger. Swap for a Mongo adapter to run more than one replica. */
export class InMemoryBookingRepository implements BookingRepository {
  private readonly byId = new Map<string, Booking>();

  async save(booking: Booking): Promise<void> {
    this.byId.set(booking.id, { ...booking });
  }

  async update(booking: Booking): Promise<void> {
    if (!this.byId.has(booking.id)) throw new Error(`Unknown booking ${booking.id}`);
    this.byId.set(booking.id, { ...booking });
  }

  async findById(id: string): Promise<Booking | null> {
    const found = this.byId.get(id);
    return found ? { ...found } : null;
  }

  async findByOrderId(orderId: string): Promise<Booking | null> {
    for (const booking of this.byId.values()) {
      if (booking.orderId === orderId) return { ...booking };
    }
    return null;
  }

  async sumActiveWeightBySlot(restaurantId: string, slotStartsEpochMs: number[]): Promise<Map<number, number>> {
    const wanted = new Set(slotStartsEpochMs);
    const sums = new Map<number, number>();
    for (const start of slotStartsEpochMs) sums.set(start, 0);
    for (const booking of this.byId.values()) {
      if (booking.restaurantId !== restaurantId || !isActive(booking)) continue;
      if (!wanted.has(booking.slotStartEpochMs)) continue;
      sums.set(booking.slotStartEpochMs, (sums.get(booking.slotStartEpochMs) ?? 0) + booking.weight);
    }
    return sums;
  }

  async listBySlotRange(restaurantId: string, fromEpochMs: number, toEpochMs: number): Promise<Booking[]> {
    return [...this.byId.values()]
      .filter((b) => b.restaurantId === restaurantId && isActive(b))
      .filter((b) => b.slotStartEpochMs >= fromEpochMs && b.slotStartEpochMs <= toEpochMs)
      .map((b) => ({ ...b }))
      .sort((a, b) => a.slotStartEpochMs - b.slotStartEpochMs);
  }

  async findDueForPrep(nowEpochMs: number): Promise<Booking[]> {
    return [...this.byId.values()]
      .filter((b) => b.status === "CONFIRMED" && !!b.orderId && b.prepareAtEpochMs <= nowEpochMs)
      .map((b) => ({ ...b }))
      .sort((a, b) => a.prepareAtEpochMs - b.prepareAtEpochMs);
  }
}
