import axios, { AxiosInstance, AxiosResponse } from "axios";
import { com } from "munchies-order-service-shared";
import {
  OrderClient,
  OrderServiceError,
  PlaceOrderInput,
  PlacedOrder,
} from "../../domain/ports/orderClient";

import OrderServiceConfig = com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig;
import AdvanceOrderStatusRequest = com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest;
import placeOrderRequestFromJson = com.munchies.order.infrastructure.adapter.inbound.request.placeOrderRequestFromJson;
import placeOrderResponseFromJson = com.munchies.order.infrastructure.adapter.outbound.response.placeOrderResponseFromJson;
import getOrderDetailsResponseFromJson = com.munchies.order.infrastructure.adapter.outbound.response.getOrderDetailsResponseFromJson;

/**
 * Raw-text request options: the order-shared parsers do the JSON mapping, so axios must hand
 * the body over untouched. Set per request so an injected AxiosInstance behaves the same way.
 */
const RAW_TEXT = {
  responseType: "text" as const,
  transformResponse: (body: unknown) => body,
};

/**
 * REST adapter towards order-service.
 *
 * Paths come from `OrderServiceConfig` and bodies are built and read with the order-shared
 * classes (`PlaceOrderRequest`, `AdvanceOrderStatusRequest`, `PlaceOrderResponse`,
 * `GetOrderDetailsResponse`), so this adapter cannot drift from order-service's contract:
 *   POST {SERVICE_PATH}{PLACE_ORDER_PATH}           -> place
 *   POST {SERVICE_PATH}{ADVANCE_ORDER_STATUS_PATH}  -> advance
 *   GET  {SERVICE_PATH}{GET_ORDER_PATH}             -> getStatus
 * order-service wraps every response in `{ result, code }` and encodes timestamps as
 * stringified epoch millis.
 */
export class HttpOrderClient implements OrderClient {
  private readonly http: AxiosInstance;

  constructor(baseUrl: string, http?: AxiosInstance) {
    this.http =
      http ??
      axios.create({
        baseURL: baseUrl,
        timeout: 10_000,
        headers: { "Content-Type": "application/json" },
        validateStatus: () => true,
      });
  }

  async place(input: PlaceOrderInput): Promise<PlacedOrder> {
    const res = await this.http.post<string>(
      OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.PLACE_ORDER_PATH,
      this.toPlaceOrderRequestJson(input),
      RAW_TEXT,
    );
    if (!this.isSuccess(res)) throw this.failure(res);
    const order = this.parse(() => placeOrderResponseFromJson(String(res.data)).result);
    if (!order.orderId) {
      throw new OrderServiceError("order-service response had no orderId", 502);
    }
    return { orderId: order.orderId, status: order.status };
  }

  async advance(orderId: string): Promise<void> {
    const res = await this.http.post<string>(
      OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH,
      new AdvanceOrderStatusRequest(orderId).toJson(),
      RAW_TEXT,
    );
    if (!this.isSuccess(res)) throw this.failure(res);
  }

  async getStatus(orderId: string): Promise<string> {
    // No customerId query parameter: the scheduler reads the order on its own behalf, not for
    // a customer (see OrderServiceConfig.CUSTOMER_ID_QUERY).
    const path = (OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.GET_ORDER_PATH).replace(
      "{id}",
      encodeURIComponent(orderId),
    );
    const res = await this.http.get<string>(path, RAW_TEXT);
    if (!this.isSuccess(res)) throw this.failure(res);
    return this.parse(() => getOrderDetailsResponseFromJson(String(res.data)).result.status);
  }

  /**
   * Builds the wire body through order-shared's PlaceOrderRequest. The Kotlin/JS constructor
   * cannot take a JS array for `items` (it expects a Kotlin List), so the request is decoded
   * from a plain object and re-encoded: decoding is strict, hence a field the contract does not
   * know, a missing required field or an unknown order type is rejected here, before the call.
   * Only the known fields are copied, so nothing extra on the input can leak onto the wire.
   */
  private toPlaceOrderRequestJson(input: PlaceOrderInput): string {
    const plain = {
      restaurantId: input.restaurantId,
      customerId: input.customerId,
      items: input.items.map((item) => ({ menuItemId: item.menuItemId, quantity: item.quantity })),
      orderType: input.orderType,
      estimatedDeliveryTime: input.estimatedDeliveryTime,
      deliveryAddress: input.deliveryAddress,
      bellName: input.bellName,
      customerPhone: input.customerPhone,
      pickupTime: input.pickupTime,
      customerName: input.customerName,
      tableNumber: input.tableNumber,
      numberOfGuests: input.numberOfGuests,
    };
    try {
      return placeOrderRequestFromJson(JSON.stringify(plain)).toJson();
    } catch (err) {
      const reason = err instanceof Error ? err.message : String(err);
      throw new OrderServiceError(`order does not match order-service's PlaceOrderRequest: ${reason}`, 400);
    }
  }

  /** Runs a shared parser; a body that does not match the contract is a bad gateway. */
  private parse<T>(parser: () => T): T {
    try {
      return parser();
    } catch (err) {
      const reason = err instanceof Error ? err.message : String(err);
      throw new OrderServiceError(`unexpected order-service response: ${reason}`, 502);
    }
  }

  private isSuccess(res: AxiosResponse): boolean {
    return res.status >= 200 && res.status < 300;
  }

  private failure(res: AxiosResponse): OrderServiceError {
    return new OrderServiceError(this.message(res.data) ?? `HTTP ${res.status}`, res.status);
  }

  /**
   * Error bodies are not parsed with a shared class on purpose: they come from order-service's
   * exception handlers or from Micronaut itself, and not all of them are an `ErrorResponse`.
   * The `{ result: "<message>" }` form is used when present, a non-JSON body as it is, and
   * null (the caller then reports the HTTP status) for any other JSON.
   */
  private message(data: unknown): string | null {
    if (typeof data !== "string" || data.length === 0) return null;
    let parsed: unknown;
    try {
      parsed = JSON.parse(data);
    } catch {
      return data;
    }
    if (parsed && typeof parsed === "object" && "result" in parsed) {
      const result = (parsed as { result: unknown }).result;
      if (typeof result === "string") return result;
    }
    return null;
  }
}
