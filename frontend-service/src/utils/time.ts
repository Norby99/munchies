// Date helpers: local calendar dates for <input type="date"> and the scheduler, and display of the
// epoch-millisecond strings the order API returns.

function pad(value: number): string {
  return String(value).padStart(2, '0')
}

// Local calendar date as "YYYY-MM-DD" (the value of <input type="date">, and the scheduler's
// `date` query parameter).
export function toDateInput(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

// Formats an epoch-ms string coming from the API for display; falls back to the raw value.
export function formatEpoch(epochMs: string | undefined): string {
  if (!epochMs) return ''
  const ms = Number(epochMs)
  if (Number.isNaN(ms)) return epochMs
  return new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium', timeStyle: 'short' }).format(ms)
}
