import { afterEach, describe, expect, it } from "vitest";
import getKafka from "@main/infrastructure/adapter/outbound/kafka/KafkaClient";

describe("getKafka", () => {
  const original = process.env.KAFKA_BOOTSTRAP_SERVERS;

  afterEach(() => {
    if (original === undefined) delete process.env.KAFKA_BOOTSTRAP_SERVERS;
    else process.env.KAFKA_BOOTSTRAP_SERVERS = original;
  });

  it("builds a client for the configured broker", () => {
    process.env.KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    expect(getKafka("table-reservation-service")).toBeDefined();
  });

  it("fails without a configured broker", () => {
    delete process.env.KAFKA_BOOTSTRAP_SERVERS;
    expect(() => getKafka("table-reservation-service")).toThrow("Kafka is not online");
  });
});
