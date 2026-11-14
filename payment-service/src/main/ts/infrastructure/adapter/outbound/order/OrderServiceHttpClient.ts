import axios from "axios";
import { com } from "munchies-order-service-shared";
import {
  OrderLookupResult,
  OrderPaymentNotificationResult,
  OrderServiceClient,
} from "@main/domain/port/order-service-client";

import OrderServiceConfig = com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig;
import getOrderDetailsResponseFromJson = com.munchies.order.infrastructure.adapter.outbound.response.getOrderDetailsResponseFromJson;

/**
 * REST adapter towards order-service.
 *
 * It reads an order with `GET {SERVICE_PATH}{GET_ORDER_PATH}` and marks it as paid with
 * `PATCH {SERVICE_PATH}{PAY_ORDER_PATH}` (see MicronautOrderController). Paths and the response
 * parser are reused from order-shared, so this adapter cannot drift from the real contract.
 */
export class OrderServiceHttpClient implements OrderServiceClient {
  async getOrder(orderId: string): Promise<OrderLookupResult> {
    const baseUrl = process.env.ORDER_SERVICE_URL;
    if (!baseUrl) {
      return { type: "UNAVAILABLE", reason: "Missing Order Service URL" };
    }

    const path = (
      OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.GET_ORDER_PATH
    ).replace("{id}", encodeURIComponent(orderId));

    try {
      // Raw text: the shared parser does the mapping, as for every other contract.
      const response = await axios.get<string>(baseUrl + path, {
        responseType: "text",
        transformResponse: (body) => body,
        validateStatus: () => true,
      });
      if (response.status === 404) return { type: "NOT_FOUND" };
      if (response.status >= 400) {
        return {
          type: "UNAVAILABLE",
          reason: `Order Service responded with status ${response.status}`,
        };
      }
      const order = getOrderDetailsResponseFromJson(String(response.data)).result;
      return {
        type: "FOUND",
        order: {
          customerId: order.customerId,
          payed: order.payed,
          totalCents: order.totalCents,
        },
      };
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      return {
        type: "UNAVAILABLE",
        reason: "Order Service request failed: " + message,
      };
    }
  }

  async markOrderAsPaid(orderId: string): Promise<OrderPaymentNotificationResult> {
    const baseUrl = process.env.ORDER_SERVICE_URL;
    if (!baseUrl) {
      return {
        success: false,
        errorMessage: "Missing Order Service URL",
      };
    }

    const path = (
      OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.PAY_ORDER_PATH
    ).replace("{id}", orderId);

    try {
      // The JSON Content-Type is required: for a string body axios defaults to
      // application/x-www-form-urlencoded, which Micronaut rejects with 415.
      const response = await axios.patch(baseUrl + path, "", {
        headers: { "Content-Type": "application/json" },
        validateStatus: () => true,
      });

      if (response.status >= 400) {
        return {
          success: false,
          errorMessage: `Order Service responded with status ${response.status}`,
        };
      }

      return { success: true };
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      return {
        success: false,
        errorMessage: "Order Service request failed: " + message,
      };
    }
  }
}
