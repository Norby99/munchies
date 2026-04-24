import {
  Currency,
  getIdFromEntityId,
  newId,
  PaymentStatus,
  newUUIDEntityId,
} from "@main/domain/external-modules";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentId } from "@main/domain/model/PaymentId";

new PaymentController();

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

    await repository.save(
      new Payment(
        new PaymentId(id),
        PaymentStatus.PENDING,
        10,
        newUUIDEntityId(newId()),
        Currency.AUD,
        null
      )
    );

    const found = await repository.findById(new PaymentId(id));
    console.log("found: " + found);
  } catch (error) {
    console.error("Mongo query check failed:", error);
    process.exitCode = 1;
  } finally {
    await disconnectDB();
  }
}

void main();
