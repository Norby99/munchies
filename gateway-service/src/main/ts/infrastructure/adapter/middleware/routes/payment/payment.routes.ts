import { SimpleRoute } from "../simple-route";
import { ProcessPaymentRoute } from "./process-payment.route";

export const paymentRoutes: SimpleRoute<any>[] = [new ProcessPaymentRoute()];
