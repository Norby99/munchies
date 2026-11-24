import { DeliveryZone } from "../model/deliveryZone";

/**
 * The delivery zones the restaurant's manager has drawn. An empty list means the restaurant has
 * none, and deliveries are batched on detour and trip budget alone.
 */
export interface RestaurantZones {
  get(restaurantId: string): Promise<DeliveryZone[]>;
}
