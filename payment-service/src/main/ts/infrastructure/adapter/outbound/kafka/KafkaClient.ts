import { Kafka, logLevel } from "kafkajs";

/**
 * Builds a connected Kafka client and ensures the given topic exists.
 *
 * Mirrors the helper used by notification-service so both sides of the
 * payment-success event agree on how the topic is provisioned.
 */
async function getKafka(topic: string): Promise<Kafka> {
  const uri = process.env.KAFKA_BOOTSTRAP_SERVERS;

  if (!uri) throw new Error("Kafka is not online");

  const kafka = new Kafka({
    clientId: topic,
    brokers: [uri],
    logLevel: logLevel.INFO,
  });

  const admin = kafka.admin();

  await admin.connect();

  await admin.createTopics({
    topics: [
      {
        topic: topic,
      },
    ],
  });

  await admin.disconnect();

  return kafka;
}

export default getKafka;
