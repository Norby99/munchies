import { Kafka, logLevel } from "kafkajs";

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
