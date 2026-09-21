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
    vi.useRealTimers();
    mockAdmin.connect.mockResolvedValue(undefined);
    mockAdmin.createTopics.mockResolvedValue(undefined);
    mockAdmin.disconnect.mockResolvedValue(undefined);
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
        waitForLeaders: false,
      })
    );
    expect(mockAdmin.disconnect).toHaveBeenCalled();
  });

  it("retries createTopics on a retriable failure and eventually succeeds", async () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    vi.useFakeTimers();

    mockAdmin.createTopics
      .mockRejectedValueOnce(new Error("UNKNOWN_TOPIC_OR_PARTITION"))
      .mockResolvedValueOnce(undefined);

    const getKafka: (topic: string) => Promise<unknown> = (
      await import("@main/infrastructure/adapter/inbound/kafka/KafkaClient")
    ).default;

    const resultPromise = getKafka("test-topic");
    await vi.advanceTimersByTimeAsync(2000);
    const result = await resultPromise;

    expect(result).toBeDefined();
    expect(mockAdmin.createTopics).toHaveBeenCalledTimes(2);
    expect(mockAdmin.disconnect).toHaveBeenCalled();
  });

  it("gives up after exhausting every retry, without disconnecting", async () => {
    vi.stubEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    vi.useFakeTimers();

    mockAdmin.createTopics.mockRejectedValue(new Error("UNKNOWN_TOPIC_OR_PARTITION"));

    const getKafka: (topic: string) => Promise<unknown> = (
      await import("@main/infrastructure/adapter/inbound/kafka/KafkaClient")
    ).default;

    const resultPromise = getKafka("test-topic");
    resultPromise.catch(() => {});
    await vi.advanceTimersByTimeAsync(10000);

    await expect(resultPromise).rejects.toThrow("UNKNOWN_TOPIC_OR_PARTITION");
    expect(mockAdmin.createTopics).toHaveBeenCalledTimes(5);
    expect(mockAdmin.disconnect).not.toHaveBeenCalled();
  });
});
