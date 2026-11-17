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
import { GetOrderTracking } from "./application/getOrderTracking";
import { DriverSimulator } from "./application/driverSimulator";
import { HttpOrderClient } from "./infrastructure/outbound/httpOrderClient";
import { HttpRestaurantPolicyClient } from "./infrastructure/outbound/httpRestaurantPolicyClient";
import { RandomAdmissionQuotaAgent } from "./infrastructure/outbound/randomAdmissionQuotaAgent";
import { OsrmRoutePlanner } from "./infrastructure/outbound/osrmRoutePlanner";
import { StraightLineRoutePlanner } from "./infrastructure/outbound/straightLineRoutePlanner";
import { buildApp } from "./infrastructure/http/server";

function main(): void {
  const config = loadConfig();
  assertValidPolicy(config.defaultPolicy);

  const bookings = new InMemoryBookingRepository();
  const trips = new InMemoryTripRepository();
  const orders = new HttpOrderClient(config.orderServiceUrl);
  const policyClient = new HttpRestaurantPolicyClient(config.restaurantServiceUrl);
  // TEMPORARY: a random admission quota until the CapacityAgent (analysis-scala) is wired in.
  const agent = new RandomAdmissionQuotaAgent(config.admissionQuotaMin, config.admissionQuotaMax);
  const policies = new PolicyResolver(policyClient, config.defaultPolicy, agent);
  const delivery = new DeliveryGate(
    policyClient,
    new HttpRestaurantLocation(config.restaurantServiceUrl),
    agent,
  );

  const routes = config.osrmUrl ? new OsrmRoutePlanner(config.osrmUrl) : new StraightLineRoutePlanner();

  const app = buildApp({
    getAvailability: new GetAvailability(policies, bookings, trips, delivery),
    getLoad: new GetLoad(policies, bookings, trips),
    scheduleOrder: new ScheduleOrder(policies, bookings, trips, orders, delivery),
    getBooking: new GetBooking(bookings),
    advanceScheduledOrder: new AdvanceScheduledOrder(bookings, orders, trips, routes),
    getOrderTracking: new GetOrderTracking(bookings, trips, config.driverSpeedKmh),
  });

  const transitions = new TransitionScheduler(bookings, orders, config.transitionTickMs);
  transitions.start();
  // Mocked driver devices: move every departed trip's driver (see DriverSimulator).
  const drivers = new DriverSimulator(trips, config.driverTickMs, config.driverSpeedKmh);
  drivers.start();

  const server = app.listen(config.port, () => {
    console.log(`scheduler-service listening on :${config.port}`);
    console.log(`  order-service      -> ${config.orderServiceUrl}`);
    console.log(`  restaurant-service -> ${config.restaurantServiceUrl}`);
    console.log(`  transition tick    -> every ${config.transitionTickMs}ms`);
    console.log(`  driver simulation  -> every ${config.driverTickMs}ms at ${config.driverSpeedKmh} km/h (mocked)`);
    console.log(`  driver routes      -> ${config.osrmUrl ? `OSRM at ${config.osrmUrl}` : "straight lines (OSRM_URL is empty)"}`);
    console.log(`  admission quota    -> random in [${config.admissionQuotaMin}, ${config.admissionQuotaMax}] (temporary)`);
  });

  const shutdown = () => {
    transitions.stop();
    drivers.stop();
    server.close(() => process.exit(0));
  };
  process.on("SIGTERM", shutdown);
  process.on("SIGINT", shutdown);
}

main();
