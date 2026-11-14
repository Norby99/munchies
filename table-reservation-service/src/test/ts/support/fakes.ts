import { Table } from "@main/domain/model/DiningRoom";
import { TableReservation } from "@main/domain/model/TableReservation";
import { Logger } from "@main/domain/port/logger";
import {
  ManagementCheckResult,
  RestaurantServiceClient,
  TablesLookupResult,
} from "@main/domain/port/restaurant-service-client";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";

/** The instant every test runs at. */
export const NOW = new Date("2026-11-20T10:00:00.000Z");
export const now = () => NOW;

/** An arrival time later the same day, on the 15-minute grid. */
export const at = (time: string) => new Date(`2026-11-20T${time}:00.000Z`);

export const RESTAURANT = "restaurant-1";
export const CUSTOMER = "customer-1";
export const MANAGER = "manager-1";

/** restaurant-service stand-in: one restaurant, its tables and its manager. */
export class FakeRestaurantServiceClient implements RestaurantServiceClient {
  available = true;

  constructor(
    public tables: Table[] = [
      { number: 1, seats: 2 },
      { number: 2, seats: 4 },
    ],
    private readonly restaurantId: string = RESTAURANT,
    private readonly managerId: string = MANAGER,
  ) {}

  getTables(restaurantId: string): Promise<TablesLookupResult> {
    if (!this.available) return Promise.resolve({ type: "UNAVAILABLE", reason: "down" });
    return Promise.resolve(
      restaurantId === this.restaurantId
        ? { type: "FOUND", tables: this.tables }
        : { type: "NOT_FOUND" },
    );
  }

  isManagedBy(restaurantId: string, managerId: string): Promise<ManagementCheckResult> {
    if (!this.available) return Promise.resolve({ type: "UNAVAILABLE", reason: "down" });
    const managed = restaurantId === this.restaurantId && managerId === this.managerId;
    return Promise.resolve({ type: managed ? "MANAGED" : "NOT_MANAGED" });
  }
}

/** Records what would have been published on Kafka. */
export class RecordingPublisher implements TableReservationNotificationPublisher {
  readonly accepted: TableReservation[] = [];
  readonly modified: TableReservation[] = [];
  readonly canceled: TableReservation[] = [];
  /** When set, every publication fails with it. */
  failure: Error | null = null;

  private record(list: TableReservation[], reservation: TableReservation): Promise<void> {
    if (this.failure) return Promise.reject(this.failure);
    list.push(reservation);
    return Promise.resolve();
  }

  publishAccepted(reservation: TableReservation): Promise<void> {
    return this.record(this.accepted, reservation);
  }

  publishModified(reservation: TableReservation): Promise<void> {
    return this.record(this.modified, reservation);
  }

  publishCanceled(reservation: TableReservation): Promise<void> {
    return this.record(this.canceled, reservation);
  }
}

/** Collects log entries instead of printing them. */
export class RecordingLogger implements Logger {
  readonly errors: string[] = [];

  info(): void {}

  warn(): void {}

  error(message: string): void {
    this.errors.push(message);
  }
}
