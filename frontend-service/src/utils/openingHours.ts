// Opening hours of a restaurant, from its capacity policy ("HH:MM", restaurant-local time). The
// browser's clock is used for "open now": restaurants and customers are assumed to share a time
// zone, as the scheduler does (it runs on Europe/Rome).

export interface OpeningStatus {
  /** "11:00 – 22:00" */
  hours: string
  open: boolean
  /** "Open now · closes at 22:00", "Opens at 11:00", "Closed for today · opens at 11:00". */
  label: string
}

function minutesOfDay(hhmm: string): number | null {
  const match = /^(\d{2}):(\d{2})$/.exec(hhmm)
  return match ? Number(match[1]) * 60 + Number(match[2]) : null
}

export function openingStatus(opening: string, closing: string, now: Date = new Date()): OpeningStatus | null {
  const from = minutesOfDay(opening)
  const to = minutesOfDay(closing)
  if (from === null || to === null || from >= to) return null

  const current = now.getHours() * 60 + now.getMinutes()
  const open = current >= from && current < to
  let label: string
  if (open) label = `Open now · closes at ${closing}`
  else if (current < from) label = `Closed · opens at ${opening}`
  else label = `Closed for today · opens tomorrow at ${opening}`
  return { hours: `${opening} – ${closing}`, open, label }
}
