import { loadConfig } from "./config";
import { assertValidPolicy } from "./domain/model/capacityPolicy";
import { PolicyResolver } from "./application/policyResolver";
import { GetAvailability } from "./application/getAvailability";
import { GetLoad } from "./application/getLoad";
import { ScheduleOrder } from "./application/scheduleOrder";
import { AdvanceScheduledOrder } from "./application/advanceScheduledOrder";
import { TransitionScheduler } from "./application/transitionScheduler";
import { InMemoryBookingRepository } from "./infrastructure/outbound/inMemoryBookingRepository";
import { InMemoryTripRepository } from "./infrastructure/outbound/inMemoryTripRepository";
import { HttpRestaurantLocation } from "./infrastructure/outbound/httpRestaurantLocation";
import { DeliveryGate } from "./application/deliveryGate";
import { GetBooking } from "./application/getBooking";
import { HttpOrderClient } from "./infrastructure/outbound/httpOrderClient";
import { HttpRestaurantPolicyClient } from "./infrastructure/outbound/httpRestaurantPolicyClient";
import { buildApp } from "./infrastructure/http/server";

function main(): void {
  const config = loadConfig();
  assertValidPolicy(config.defaultPolicy);

  const bookings = new InMemoryBookingRepository();
  const trips = new InMemoryTripRepository();
  const orders = new HttpOrderClient(config.orderServiceUrl);
  const policyClient = new HttpRestaurantPolicyClient(config.restaurantServiceUrl);
  const policies = new PolicyResolver(policyClient, config.defaultPolicy);
  const delivery = new DeliveryGate(
    policyClient,
    new HttpRestaurantLocation(config.restaurantServiceUrl),
  );

  const app = buildApp({
    getAvailability: new GetAvailability(policies, bookings, trips, delivery),
    getLoad: new GetLoad(policies, bookings, trips),
    scheduleOrder: new ScheduleOrder(policies, bookings, trips, orders, delivery),
    getBooking: new GetBooking(bookings),
    advanceScheduledOrder: new AdvanceScheduledOrder(bookings, orders),
  });

  const transitions = new TransitionScheduler(bookings, orders, config.transitionTickMs);
  transitions.start();

  const server = app.listen(config.port, () => {
    console.log(`scheduler-service listening on :${config.port}`);
    console.log(`  order-service      -> ${config.orderServiceUrl}`);
    console.log(`  restaurant-service -> ${config.restaurantServiceUrl}`);
    console.log(`  transition tick    -> every ${config.transitionTickMs}ms`);
  });

  const shutdown = () => {
    transitions.stop();
    server.close(() => process.exit(0));
  };
  process.on("SIGTERM", shutdown);
  process.on("SIGINT", shutdown);
}

main();
