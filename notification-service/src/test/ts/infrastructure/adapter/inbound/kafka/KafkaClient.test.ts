import { describe, expect, it, vi, afterEach } from "vitest";

const mockAdmin = {
  connect: vi.fn().mockResolvedValue(undefined),
  createTopics: vi.fn().mockResolvedValue(undefined),
  disconnect: vi.fn().mockResolvedValue(undefined),
};

vi.mock("kafkajs", () => ({
  // Use a regular function so it can be called with `new`
  Kafka: vi.fn(function () {
    return {
      admin: vi.fn().mockReturnValue(mockAdmin),
    };
  }),
  logLevel: { INFO: 2 },
}));

describe("getKafka", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.clearAllMocks();
  });


  it("throws an error when KAFKA_BOOTSTRAP_SERVERS is not set", async () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "");

    // Use require to bypass module caching from prior test
    const getKafka: (topic: string) => Promise<unknown> = (
      await import("@main/infrastructure/adapter/inbound/kafka/KafkaClient")
    ).default;

    await expect(getKafka("some-topic")).rejects.toThrow("Kafka is not online");
  });

  it("returns a Kafka instance, creates the topic, and disconnects admin when KAFKA_BOOTSTRAP_SERVERS is set", async () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    const getKafka: (topic: string) => Promise<unknown> = (
      await import("@main/infrastructure/adapter/inbound/kafka/KafkaClient")
    ).default;

    const result = await getKafka("test-topic");

    expect(result).toBeDefined();
    expect(mockAdmin.connect).toHaveBeenCalled();
    expect(mockAdmin.createTopics).toHaveBeenCalledWith(
      expect.objectContaining({
        topics: [{ topic: "test-topic" }],
      })
    );
    expect(mockAdmin.disconnect).toHaveBeenCalled();
  });
});
