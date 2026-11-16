import { vi } from "vitest";
import type { LiveNotifier } from "@main/domain/port/LiveNotifier";

/** A LiveNotifier that records the pushes instead of sending them. */
export function fakeNotifier(): LiveNotifier & {
  toUser: ReturnType<typeof vi.fn>;
  toRestaurant: ReturnType<typeof vi.fn>;
} {
  return {
    toUser: vi.fn().mockResolvedValue(undefined),
    toRestaurant: vi.fn().mockResolvedValue(undefined),
  };
}
