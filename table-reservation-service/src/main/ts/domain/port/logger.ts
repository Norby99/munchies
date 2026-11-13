/**
 * Outbound port for diagnostic logging.
 *
 * Lets the application layer report what happened without depending on a logging library or on
 * where the logs end up; the adapter in `infrastructure/adapter/outbound/logging` decides both.
 *
 * `context` holds the structured fields of the entry (ids, error messages), kept apart from
 * the human-readable `message` so they stay searchable once logs are aggregated.
 */
export interface Logger {
  info(message: string, context?: Record<string, unknown>): void;
  warn(message: string, context?: Record<string, unknown>): void;
  error(message: string, context?: Record<string, unknown>): void;
}
