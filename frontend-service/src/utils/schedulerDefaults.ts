// The capacity policy scheduler-service applies to a restaurant that has not set its own
// (scheduler-service/src/main/ts/config.ts, overridable there with the DEFAULT_* variables). The
// scheduler exposes no endpoint for it, so it is mirrored here: keep the two in sync.
//
// Used to pre-fill the manager's scheduling dialog, and to show customers indicative opening
// hours for a restaurant that has not published its own (the scheduler books pickups in them).

import type { CapacityPolicy } from '@/api/restaurants'

export const SCHEDULER_DEFAULT_POLICY: Readonly<CapacityPolicy> = Object.freeze({
  slotMinutes: 30,
  maxCapacityUnits: 8,
  opening: '11:00',
  closing: '22:00',
  prepLeadMinutes: 30,
  maxAdvanceDays: 7,
  maxTripsPerSlot: 2,
  maxTripKmBudget: 15,
  maxDetourKm: 1,
})
