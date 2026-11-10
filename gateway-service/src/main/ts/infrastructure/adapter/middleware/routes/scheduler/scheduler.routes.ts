import { AuthRole, HttpMethod } from "munchies-commons/kotlin/commons-modules";
import { SimpleRoute } from "../simple-route";
import { SchedulerProxyRoute } from "./scheduler-proxy.route";

/**
 * Gateway surface for scheduler-service.
 *
 *  GET  /schedule/{restaurantId}/availability?date=YYYY-MM-DD[&orderType=]  (CUSTOMER)
 *  GET  /schedule/{restaurantId}/load?date=YYYY-MM-DD                        (MANAGER)
 *  POST /schedule/{restaurantId}/orders                                     (CUSTOMER, customerId injected)
 *  POST /schedule/orders/{orderId}/advance                                  (MANAGER)
 *  GET  /schedule/bookings/{bookingId}                                      (MANAGER, includes tripId)
 */
export const schedulerRoutes: SimpleRoute<any>[] = [
  new SchedulerProxyRoute(
    "/schedule/{restaurantId}/availability",
    HttpMethod.GET,
    AuthRole.CUSTOMER,
    "none",
  ),
  new SchedulerProxyRoute("/schedule/{restaurantId}/load", HttpMethod.GET, AuthRole.MANAGER, "none"),
  new SchedulerProxyRoute("/schedule/bookings/{bookingId}", HttpMethod.GET, AuthRole.MANAGER, "none"),
  new SchedulerProxyRoute(
    "/schedule/{restaurantId}/orders",
    HttpMethod.POST,
    AuthRole.CUSTOMER,
    "injectCustomer",
  ),
  new SchedulerProxyRoute(
    "/schedule/orders/{orderId}/advance",
    HttpMethod.POST,
    AuthRole.MANAGER,
    "passthrough",
  ),
];
