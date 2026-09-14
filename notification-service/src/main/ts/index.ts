import "@main/domain/external-modules";
import getKafka from "./infrastructure/adapter/inbound/kafka/KafkaClient";
import {
  UserEmailConfirmationGroupId,
  UserEmailConfirmationTopic,
  PaymentSuccessNotificationInfo,
} from "@main/domain/external-modules";
import { KafkaUserEmailConfirmationNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";
import { KafkaPaymentSuccessNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer";
import { NotificationController } from "./infrastructure/adapter/inbound/web/controller/controller";

async function main() {
  // Every consumer forwards the events it receives to the same controller,
  // which is the single place currently printing notifications instead of
  // dispatching them (see NotificationController's docs).
  const controller = new NotificationController();

  const userEmailConfirmationKafka = await getKafka(UserEmailConfirmationTopic);
  const userEmailConfirmationConsumer =
    new KafkaUserEmailConfirmationNotificationConsumer(
      userEmailConfirmationKafka,
      UserEmailConfirmationTopic,
      UserEmailConfirmationGroupId,
      controller
    );
  await userEmailConfirmationConsumer.connect();
  userEmailConfirmationConsumer.run();

  const paymentSuccessTopic = PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_TOPIC;
  const paymentSuccessGroupId =
    PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_GROUP_ID;
  const paymentSuccessKafka = await getKafka(paymentSuccessTopic);
  const paymentSuccessConsumer = new KafkaPaymentSuccessNotificationConsumer(
    paymentSuccessKafka,
    paymentSuccessTopic,
    paymentSuccessGroupId,
    controller
  );
  await paymentSuccessConsumer.connect();
  paymentSuccessConsumer.run();
}

main();
