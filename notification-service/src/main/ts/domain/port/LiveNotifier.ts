import type { LiveNotification } from "@main/domain/model/LiveNotification";

/**
 * Outbound port of the "produce" step of notification-service's consume/process/produce loop:
 * pushes a notification to whoever is connected right now. Delivery is best-effort and
 * fire-and-forget: a user who is offline simply does not get it, and sees the up-to-date data
 * the next time a screen loads.
 *
 * Implementations must not block (see the `event-driven` skill): they are called from the Kafka
 * message handlers.
 */
export interface LiveNotifier {
  /** Pushes to every connection of one user (e.g. the customer of an order). */
  toUser(userId: string, notification: LiveNotification): Promise<void>;

  /** Pushes to every connection of the staff of one restaurant (its manager). */
  toRestaurant(restaurantId: string, notification: LiveNotification): Promise<void>;
}
