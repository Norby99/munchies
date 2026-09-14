import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// vi.mock() is hoisted to the top of the file by Vitest, so any variables
// referenced inside the factory must be initialised with vi.hoisted().
const { connectMock, disconnectMock, onMock } = vi.hoisted(() => ({
  connectMock: vi.fn(),
  disconnectMock: vi.fn(),
  onMock: vi.fn(),
}));

vi.mock("mongoose", () => ({
  default: {
    connect: connectMock,
    disconnect: disconnectMock,
    connection: {
      on: onMock,
    },
  },
}));

import { connectDB, disconnectDB } from "@main/infrastructure/adapter/outbound/mongo/config/db";

describe("db", () => {
  const ORIGINAL_MONGODB_URI = process.env.MONGODB_URI;

  beforeEach(() => {
    vi.clearAllMocks();
    connectMock.mockResolvedValue(undefined);
    disconnectMock.mockResolvedValue(undefined);
  });

  afterEach(() => {
    // Restore environment variable to original state
    if (ORIGINAL_MONGODB_URI === undefined) {
      delete process.env.MONGODB_URI;
    } else {
      process.env.MONGODB_URI = ORIGINAL_MONGODB_URI;
    }
  });

  // ── connectDB ─────────────────────────────────────────────────────────────

  describe("connectDB", () => {
    it("connects to MongoDB using the MONGODB_URI env variable", async () => {
      process.env.MONGODB_URI = "mongodb://localhost:27017/test";

      await connectDB();

      expect(connectMock).toHaveBeenCalledWith("mongodb://localhost:27017/test");
    });

    it("registers the connected, error and disconnected event listeners", async () => {
      process.env.MONGODB_URI = "mongodb://localhost:27017/test";

      await connectDB();

      const registeredEvents = onMock.mock.calls.map(([event]: [string]) => event);
      expect(registeredEvents).toContain("connected");
      expect(registeredEvents).toContain("error");
      expect(registeredEvents).toContain("disconnected");
    });

    it("throws when MONGODB_URI is not defined", async () => {
      delete process.env.MONGODB_URI;

      await expect(connectDB()).rejects.toThrow("MONGODB_URI is not defined in .env");
      expect(connectMock).not.toHaveBeenCalled();
    });
  });

  // ── disconnectDB ──────────────────────────────────────────────────────────

  describe("disconnectDB", () => {
    it("calls mongoose.disconnect", async () => {
      await disconnectDB();

      expect(disconnectMock).toHaveBeenCalledTimes(1);
    });
  });
});
