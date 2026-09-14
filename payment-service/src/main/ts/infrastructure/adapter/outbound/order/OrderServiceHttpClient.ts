import axios from "axios";
import { com } from "munchies-order-service-shared";
import {
  OrderPaymentNotificationResult,
  OrderServiceClient,
} from "@main/domain/port/order-service-client";

import OrderServiceConfig = com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig;

/**
 * REST adapter towards order-service.
 *
 * order-service exposes the "mark as paid" operation as
 * `PATCH {OrderServiceConfig.SERVICE_PATH}{OrderServiceConfig.PAY_ORDER_PATH}`
 * (see MicronautOrderController.payOrder). The path is reused from
 * order-shared so this adapter cannot drift from the real backend contract.
 */
export class OrderServiceHttpClient implements OrderServiceClient {
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
      const response = await axios.patch(baseUrl + path, "", {
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
