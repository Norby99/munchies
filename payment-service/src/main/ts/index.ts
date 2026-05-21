import {
  Currency,
  getIdFromEntityId,
  newId,
  PaymentStatus,
  PaymentRequest,
  PaymentMethod,
  PaymentDetails,
  newUUIDEntityId,
  PaymentResponse,
} from "@main/domain/external-modules";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentId } from "@main/domain/model/PaymentId";
import express from "express";

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
  /*
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
        null,
      ),
    );

    const found = await repository.findById(new PaymentId(id));
    console.log("found: " + found);
  } catch (error) {
    console.error("Mongo query check failed:", error);
    process.exitCode = 1;
  } finally {
    await disconnectDB();
  }
  */

  const app = express();
  app.use(express.json());

  const controller = new PaymentController();

  app.post("/payments", (req, res) => {
    //console.log(req);
    const request = new PaymentRequest(
      "",
      new PaymentDetails(100, PaymentMethod.CARD, Currency.AUD)
    );

    const response: PaymentResponse = controller.processPayment(request);

    const json_response = JSON.parse(JSON.stringify(response));

    json_response["status"] = json_response["status"]["a_1"];
    json_response["currency"] = json_response["currency"]["a_1"];

    console.log(json_response);

    res.status(200).send(json_response);
  });

  const PORT = process.env.PORT ?? 8080;
  app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
  });
}

void main();
