import { CapacityPolicy, minutesOfDay } from "./capacityPolicy";

/**
 * Pure slot arithmetic. No I/O, no clock reads except the `now` passed in — kept
 * side-effect free so it is trivially testable.
 */

export interface Slot {
  /** Local "HH:MM" of the slot start. */
  start: string;
  /** Local "HH:MM" of the slot end. */
  end: string;
  startEpochMs: number;
  endEpochMs: number;
}

export interface AvailableRange extends Slot {
  /** How many more orders may still be scheduled into this slot. */
  remaining: number;
}

const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;

export function parseDate(dateIso: string): { year: number; month: number; day: number } {
  if (!DATE_RE.test(dateIso)) {
    throw new RangeError(`date must be YYYY-MM-DD, got "${dateIso}"`);
  }
  const [year, month, day] = dateIso.split("-").map(Number);
  const probe = new Date(year, month - 1, day);
  if (probe.getFullYear() !== year || probe.getMonth() !== month - 1 || probe.getDate() !== day) {
    throw new RangeError(`date is not a real calendar date: "${dateIso}"`);
  }
  return { year, month, day };
}

function hhmm(totalMinutes: number): string {
  const h = Math.floor(totalMinutes / 60);
  const m = totalMinutes % 60;
  return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
}

/**
 * All slots for a given local calendar date, regardless of capacity or how far in
 * the future they are.
 */
export function generateSlots(dateIso: string, policy: CapacityPolicy): Slot[] {
  const { year, month, day } = parseDate(dateIso);
  const openMin = minutesOfDay(policy.opening);
  const closeMin = minutesOfDay(policy.closing);

  const slots: Slot[] = [];
  for (let startMin = openMin; startMin + policy.slotMinutes <= closeMin; startMin += policy.slotMinutes) {
    const endMin = startMin + policy.slotMinutes;
    const startEpochMs = new Date(year, month - 1, day, 0, startMin).getTime();
    const endEpochMs = new Date(year, month - 1, day, 0, endMin).getTime();
    slots.push({ start: hhmm(startMin), end: hhmm(endMin), startEpochMs, endEpochMs });
  }
  return slots;
}

/**
 * Filters slots down to the ones a customer may actually book right now:
 *  - far enough ahead that the kitchen still has its prep lead time
 *  - within the restaurant's max-advance window
 *  - not already at capacity
 */
export function availableRanges(
  slots: Slot[],
  bookedBySlotStart: Map<number, number>,
  policy: CapacityPolicy,
  nowEpochMs: number,
): AvailableRange[] {
  const earliestStart = nowEpochMs + policy.prepLeadMinutes * 60_000;
  const latestStart = nowEpochMs + policy.maxAdvanceDays * 24 * 60 * 60_000;

  return slots
    .filter((slot) => slot.startEpochMs >= earliestStart && slot.startEpochMs <= latestStart)
    .map((slot) => {
      const booked = bookedBySlotStart.get(slot.startEpochMs) ?? 0;
      return { ...slot, remaining: Math.max(0, policy.maxCapacityUnits - booked) };
    })
    .filter((range) => range.remaining > 0);
}

/**
 * True when `slotStartEpochMs` is a real slot for its own date under this policy and
 * is still bookable at `nowEpochMs` (ignores capacity — the caller checks that against
 * live bookings).
 */
export function isBookableSlot(
  slotStartEpochMs: number,
  policy: CapacityPolicy,
  nowEpochMs: number,
): boolean {
  const dateIso = toLocalDateIso(slotStartEpochMs);
  const slots = generateSlots(dateIso, policy);
  const matches = slots.some((slot) => slot.startEpochMs === slotStartEpochMs);
  if (!matches) return false;
  const earliestStart = nowEpochMs + policy.prepLeadMinutes * 60_000;
  const latestStart = nowEpochMs + policy.maxAdvanceDays * 24 * 60 * 60_000;
  return slotStartEpochMs >= earliestStart && slotStartEpochMs <= latestStart;
}

export function toLocalDateIso(epochMs: number): string {
  const d = new Date(epochMs);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(
    d.getDate(),
  ).padStart(2, "0")}`;
}
