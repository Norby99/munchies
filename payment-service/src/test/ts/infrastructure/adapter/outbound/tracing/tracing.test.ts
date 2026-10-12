import { describe, expect, it } from "vitest";
import { trace } from "@opentelemetry/api";
import {
  isUntraced,
  startTracing,
} from "@main/infrastructure/adapter/outbound/tracing/tracing";

describe("tracing", () => {
  it("should stay off when no OTLP endpoint is configured", () => {
    expect(startTracing({})).toBe(false);

    // No provider registered: spans are no-ops.
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
    expect(isUntraced("/payments")).toBe(false);
    expect(isUntraced(undefined)).toBe(false);
  });
});
