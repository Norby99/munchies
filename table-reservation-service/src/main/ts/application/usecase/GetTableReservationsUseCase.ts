import { GetTableReservationsCommand } from "@main/application/port/inbound/command/GetTableReservationsCommand";
import { GetTableReservations } from "@main/application/port/inbound/GetTableReservations";
import { ReservationsResult } from "@main/application/port/inbound/ReservationResult";
import { Logger } from "@main/domain/port/logger";
import { RestaurantServiceClient } from "@main/domain/port/restaurant-service-client";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";

/**
 * Use case listing reservations.
 *
 * A manager (`managerId` given) lists the reservations at one restaurant, and must manage it:
 * restaurant-service is asked, since the Restaurant context owns that relation. Without a
 * manager, the list is filtered by customer and/or restaurant as given.
 */
export class GetTableReservationsUseCase implements GetTableReservations {
  constructor(
    private readonly repository: TableReservationRepository,
    private readonly restaurantServiceClient: RestaurantServiceClient,
    private readonly logger: Logger,
  ) {}

  async execute(command: GetTableReservationsCommand): Promise<ReservationsResult> {
    const { customerId, restaurantId, managerId } = command;
    if (!customerId && !restaurantId) {
      return {
        type: "INVALID_REQUEST",
        reason: "A customer or a restaurant is required to list reservations",
      };
    }

    if (managerId) {
      if (!restaurantId) {
        return {
          type: "INVALID_REQUEST",
          reason: "A restaurant is required to list reservations as a manager",
        };
      }
      const check = await this.restaurantServiceClient.isManagedBy(restaurantId, managerId);
      if (check.type === "UNAVAILABLE") {
        return { type: "RESTAURANT_UNAVAILABLE", reason: check.reason };
      }
      if (check.type === "NOT_MANAGED") {
        this.logger.warn("Refused to list the reservations of an unmanaged restaurant", {
          restaurantId,
        });
        return {
          type: "FORBIDDEN",
          reason: "You can only list the reservations of your own restaurants",
        };
      }
    }

    const reservations = await this.repository.find({ customerId, restaurantId });
    return { type: "SUCCESS", reservations };
  }
}
