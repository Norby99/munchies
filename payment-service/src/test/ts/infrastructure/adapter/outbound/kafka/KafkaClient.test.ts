import { afterEach, describe, expect, it, vi } from "vitest";
import { Kafka } from "kafkajs";
import getKafka from "@main/infrastructure/adapter/outbound/kafka/KafkaClient";

const admin = vi.fn();

vi.mock("kafkajs", () => ({
  // Regular function so it can be called with `new`.
  Kafka: vi.fn(function () {
    return { admin };
  }),
  logLevel: { INFO: 2 },
}));

describe("getKafka", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.clearAllMocks();
  });

  it("throws an error when KAFKA_BOOTSTRAP_SERVERS is not set", () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "");

    expect(() => getKafka("payment-client")).toThrow("Kafka is not online");
  });

  it("builds a client pointing to the configured broker", () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    const kafka = getKafka("payment-client");

    expect(kafka).toBeDefined();
    expect(Kafka).toHaveBeenCalledWith(
      expect.objectContaining({
        clientId: "payment-client",
        brokers: ["localhost:9092"],
      })
    );
  });

  it("does not provision topics, which is notification-service's responsibility", () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    getKafka("payment-client");

    expect(admin).not.toHaveBeenCalled();
  });
});
