import { Kafka, logLevel } from "kafkajs";

const MAX_CREATE_TOPIC_ATTEMPTS = 5;
const CREATE_TOPIC_RETRY_DELAY_MS = 2000;

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Creates the given topic, retrying on failure.
 *
 * Right after the broker starts (or restarts, reloading previously created
 * topics), a metadata refresh can race leader election for some topic and
 * come back as UNKNOWN_TOPIC_OR_PARTITION. kafkajs marks that error
 * `retriable: true`, but admin.createTopics()'s own internal retrier bails
 * immediately on it instead of retrying (it only retries NOT_CONTROLLER and
 * TOPIC_ALREADY_EXISTS), so without this loop that failure would propagate
 * straight up and crash the whole agent process on startup.
 */
async function createTopicWithRetry(
  admin: ReturnType<Kafka["admin"]>,
  topic: string
): Promise<void> {
  for (let attempt = 1; attempt <= MAX_CREATE_TOPIC_ATTEMPTS; attempt++) {
    try {
      await admin.createTopics({
        topics: [{ topic }],
        waitForLeaders: true,
      });
      return;
    } catch (err) {
      if (attempt === MAX_CREATE_TOPIC_ATTEMPTS) throw err;
      console.warn(
        `[notification-service] createTopics(${topic}) failed on attempt ${attempt}/${MAX_CREATE_TOPIC_ATTEMPTS}, retrying...`,
        err
      );
      await delay(CREATE_TOPIC_RETRY_DELAY_MS);
    }
  }
}

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

  await createTopicWithRetry(admin, topic);

  await admin.disconnect();

  return kafka;
}
export default getKafka;
