import {
  Currency,
  getIdFromEntityId,
  newId,
  PaymentStatus,
} from "@main/domain/external-modules";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentId } from "@main/domain/model/PaymentId";

new PaymentController();

console.log(getIdFromEntityId(new PaymentId(null)._id));

import "dotenv/config";
import {
  connectDB,
  disconnectDB,
} from "@main/infrastructure/adapter/outbound/mongo/config/db";
import { PaymentModel } from "@main/infrastructure/adapter/outbound/mongo/document/payment-document";
import { PaymentMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/payment-mongo-repository";
import { Payment } from "@main/domain/model/Payment";

async function main(): Promise<void> {
  try {
    console.log("await connection");

    await connectDB();

    console.log("connected");

    const repository = new PaymentMongoRepository();

    const id = newId();

    console.log("new id :" + id);

    repository.save(
      new Payment(
        new PaymentId(id),
        PaymentStatus.PENDING,
        10,
        newId(),
        Currency.AUD,
        null
      )
    );

    console.log("found: " + repository.findById(new PaymentId(id)));
  } catch (error) {
    console.error("Mongo query check failed:", error);
    process.exitCode = 1;
  } finally {
    await disconnectDB();
  }
}

void main();
