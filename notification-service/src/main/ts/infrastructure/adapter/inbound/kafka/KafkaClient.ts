import { Kafka, logLevel } from "kafkajs";

function getKafka(topic: string): Kafka {
  const uri = process.env.KAFKA_BOOTSTRAP_SERVERS;

  if (!uri) throw new Error("Kafka is not online");

  return new Kafka({
    clientId: topic,
    brokers: [uri],
    logLevel: logLevel.INFO,
  });
}
export default getKafka;
