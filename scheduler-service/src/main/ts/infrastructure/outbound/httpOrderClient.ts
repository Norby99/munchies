import axios, { AxiosInstance } from "axios";
import {
  OrderClient,
  OrderServiceError,
  PlaceOrderInput,
  PlacedOrder,
} from "../../domain/ports/orderClient";

/**
 * Talks to order-service over HTTP. order-service wraps every response in
 * `{ result, code }` and encodes timestamps as stringified epoch millis.
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
    const res = await this.http.post("/orders/place", input);
    if (res.status >= 200 && res.status < 300) {
      const order = res.data?.result ?? {};
      if (!order.orderId) {
        throw new OrderServiceError("order-service response had no orderId", 502);
      }
      return { orderId: order.orderId, status: order.status ?? "PENDING" };
    }
    throw new OrderServiceError(this.message(res.data) ?? `HTTP ${res.status}`, res.status);
  }

  async advance(orderId: string): Promise<void> {
    const res = await this.http.post("/orders/advance", { orderId });
    if (res.status >= 200 && res.status < 300) return;
    throw new OrderServiceError(this.message(res.data) ?? `HTTP ${res.status}`, res.status);
  }

  private message(data: unknown): string | null {
    if (data && typeof data === "object" && "result" in data) {
      const result = (data as { result: unknown }).result;
      if (typeof result === "string") return result;
    }
    if (typeof data === "string" && data.length > 0) return data;
    return null;
  }
}
