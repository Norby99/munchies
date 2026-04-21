import { getIdFromEntityId } from "./domain/external-modules";
import { PaymentController } from "./infrastructure/adapter/inbound/web/controller/controller";
import { PaymentId } from "./domain/model/PaymentId";

new PaymentController();

console.log(getIdFromEntityId(new PaymentId(null)._id));
