import { Kafka, logLevel } from "kafkajs";

/**
 * Builds the Kafka client used by table-reservation-service's producer.
 *
 * Topic provisioning is deliberately not done here: table-reservation-service only publishes,
 * and notification-service (the only Kafka consumer in the system) is responsible for making
 * sure every topic it subscribes to exists, with retries and leader election awaited. Producers
 * across services behave the same way: they just publish on the topic named in the event's
 * `-shared` module.
 *
 * @param clientId The Kafka client id, used by the broker for logging and quotas.
 * @throws Error if `KAFKA_BOOTSTRAP_SERVERS` is not set.
 */
function getKafka(clientId: string): Kafka {
  const uri = process.env.KAFKA_BOOTSTRAP_SERVERS;

  if (!uri) throw new Error("Kafka is not online");

  return new Kafka({
    clientId,
    brokers: [uri],
    logLevel: logLevel.INFO,
  });
}

export default getKafka;
