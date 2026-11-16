// Helpers for table reservations: the arrival times offered to the customer, and how a
// reservation is shown. Times are the browser's local time, as everywhere else in the app
// (restaurants and customers are assumed to share a time zone).

import { toDateInput } from '@/utils/time'
import type { Reservation } from '@/types'

// table-reservation-service only accepts arrivals on a quarter of an hour.
export const ARRIVAL_STEP_MINUTES = 15
// How far ahead the UI offers to book. The service itself sets no limit.
export const BOOKABLE_DAYS = 14
// Last arrival offered before closing time, so that a party is not seated as the kitchen closes.
export const LAST_ARRIVAL_BEFORE_CLOSING_MINUTES = 60

function minutesOf(hhmm: string): number {
  const [h, m] = hhmm.split(':').map(Number)
  return h * 60 + m
}

function hhmm(minutes: number): string {
  return `${String(Math.floor(minutes / 60)).padStart(2, '0')}:${String(minutes % 60).padStart(2, '0')}`
}

/** "YYYY-MM-DD" + "HH:MM" (local time) to the Date of that moment. */
export function localDateTime(date: string, time: string): Date {
  const [y, mo, d] = date.split('-').map(Number)
  const [h, mi] = time.split(':').map(Number)
  return new Date(y, mo - 1, d, h, mi)
}

/**
 * Arrival times ("HH:MM") offered on `date`: every quarter of an hour from opening to an hour
 * before closing, leaving out those already past (a booking must start in the future).
 */
export function arrivalTimes(date: string, opening: string, closing: string, now: Date = new Date()): string[] {
  const first = Math.ceil(minutesOf(opening) / ARRIVAL_STEP_MINUTES) * ARRIVAL_STEP_MINUTES
  const last = minutesOf(closing) - LAST_ARRIVAL_BEFORE_CLOSING_MINUTES
  const times: string[] = []
  for (let m = first; m <= last; m += ARRIVAL_STEP_MINUTES) {
    const time = hhmm(m)
    if (localDateTime(date, time) > now) times.push(time)
  }
  return times
}

/** The next BOOKABLE_DAYS calendar days, starting today, as toggle options. */
export function bookableDays(now: Date = new Date()): { value: string; label: string }[] {
  const format = new Intl.DateTimeFormat('en-GB', { weekday: 'short', day: 'numeric', month: 'short' })
  return Array.from({ length: BOOKABLE_DAYS }, (_, i) => {
    const d = new Date(now)
    d.setDate(d.getDate() + i)
    return { value: toDateInput(d), label: i === 0 ? 'Today' : i === 1 ? 'Tomorrow' : format.format(d) }
  })
}

/** Local date ("YYYY-MM-DD") and time ("HH:MM") of an ISO instant, to prefill a form. */
export function splitInstant(iso: string): { date: string; time: string } {
  const d = new Date(iso)
  return { date: toDateInput(d), time: hhmm(d.getHours() * 60 + d.getMinutes()) }
}

const dayFormat = new Intl.DateTimeFormat('en-GB', { weekday: 'long', day: 'numeric', month: 'long' })
const timeFormat = new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit' })

/** "Friday 20 November" */
export function formatDay(iso: string): string {
  return dayFormat.format(new Date(iso))
}

/** "19:30 – 21:30" */
export function formatTimeRange(reservation: Pick<Reservation, 'start' | 'end'>): string {
  return `${timeFormat.format(new Date(reservation.start))} – ${timeFormat.format(new Date(reservation.end))}`
}

/** An accepted reservation that has not ended: it can still be used to order at the table. */
export function isActive(reservation: Reservation, now: Date = new Date()): boolean {
  return reservation.status === 'ACCEPTED' && new Date(reservation.end) > now
}

/** Only an accepted reservation that has not started may be changed or canceled. */
export function isChangeable(reservation: Reservation, now: Date = new Date()): boolean {
  return reservation.status === 'ACCEPTED' && new Date(reservation.start) > now
}

/** Seated now: started and not yet ended. */
export function isOngoing(reservation: Reservation, now: Date = new Date()): boolean {
  return isActive(reservation, now) && new Date(reservation.start) <= now
}
