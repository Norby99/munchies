import "@main/domain/external-modules";
import getKafka from "./infrastructure/adapter/inbound/kafka/KafkaClient";
import {
  UserEmailConfirmationGroupId,
  UserEmailConfirmationTopic,
} from "@main/domain/external-modules";
import { KafkaUserEmailConfirmationNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";

console.log("hello world");

async function main() {
  const kafka = await getKafka(UserEmailConfirmationTopic);

  const consumer = new KafkaUserEmailConfirmationNotificationConsumer(
    kafka,
    UserEmailConfirmationTopic,
    UserEmailConfirmationGroupId
  );

  await consumer.connect();
  consumer.run();
}

main();
