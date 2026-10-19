import { describe, expect, it } from "vitest";
import { trace } from "@opentelemetry/api";
import {
  isUntraced,
  startTracing,
} from "../../main/ts/infrastructure/adapter/middleware/tracing";

describe("tracing", () => {
  it("should stay off when no OTLP endpoint is configured", () => {
    expect(startTracing({})).toBe(false);

    // No provider registered: spans are no-ops and carry an invalid context.
    const span = trace.getTracer("test").startSpan("noop");
    expect(span.isRecording()).toBe(false);
    span.end();
  });

  it("should leave operational endpoints out of traces", () => {
    expect(isUntraced("/health")).toBe(true);
    expect(isUntraced("/metrics")).toBe(true);
    expect(isUntraced("/metrics?format=text")).toBe(true);
  });

  it("should trace business endpoints", () => {
    expect(isUntraced("/orders/42")).toBe(false);
    expect(isUntraced("/users/health")).toBe(false);
    expect(isUntraced(undefined)).toBe(false);
  });
});
