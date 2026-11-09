// Conversions between <input type="datetime-local"> values (local wall-clock time, no zone,
// "YYYY-MM-DDTHH:MM") and the epoch-milliseconds the order API expects.

function pad(value: number): string {
  return String(value).padStart(2, '0')
}

export function toDateTimeLocal(date: Date): string {
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  )
}

// Returns null for an empty or malformed value. `new Date(string)` parses a zone-less ISO
// date-time as local time, which is exactly what datetime-local means.
export function fromDateTimeLocal(value: string): number | null {
  if (!value) return null
  const ms = new Date(value).getTime()
  return Number.isNaN(ms) ? null : ms
}

// Formats an epoch-ms string coming from the API for display; falls back to the raw value.
export function formatEpoch(epochMs: string | undefined): string {
  if (!epochMs) return ''
  const ms = Number(epochMs)
  if (Number.isNaN(ms)) return epochMs
  return new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium', timeStyle: 'short' }).format(ms)
}
