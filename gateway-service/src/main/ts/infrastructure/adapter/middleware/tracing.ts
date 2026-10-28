import { IncomingMessage } from "node:http";
import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import {
  ExpressInstrumentation,
  ExpressLayerType,
} from "@opentelemetry/instrumentation-express";
import { HttpInstrumentation } from "@opentelemetry/instrumentation-http";
import { PinoInstrumentation } from "@opentelemetry/instrumentation-pino";
import { resourceFromAttributes } from "@opentelemetry/resources";
import { BatchSpanProcessor } from "@opentelemetry/sdk-trace-base";
import { NodeTracerProvider } from "@opentelemetry/sdk-trace-node";

/**
 * Distributed Tracing side of the Observability pattern.
 *
 * The gateway is where a request enters the system, so this is where its trace starts: a
 * trace id is assigned to the incoming request and sent along in the `traceparent` header of
 * every call to a downstream service, which continues the same trace. Spans are exported
 * over OTLP to the endpoint in `OTEL_EXPORTER_OTLP_ENDPOINT` (Tempo in the cluster, see
 * `observability/`).
 *
 * This module must be the first import of the entry point: the instrumentations work by
 * patching `http`, `express` and `pino` as they are loaded, so nothing may load them earlier.
 */

/** Operational endpoints, polled every few seconds by Kubernetes and Prometheus. */
const UNTRACED_PATHS = new Set(["/health", "/metrics"]);

/** Whether a request targets an operational endpoint, which would only add noise to traces. */
export function isUntraced(url: string | undefined): boolean {
  return UNTRACED_PATHS.has((url ?? "").split("?")[0]);
}

/**
 * Starts tracing when an OTLP endpoint is configured, and does nothing otherwise, so local
 * runs and tests need no collector.
 *
 * @returns whether tracing was started.
 */
export function startTracing(env: NodeJS.ProcessEnv = process.env): boolean {
  if (!env.OTEL_EXPORTER_OTLP_ENDPOINT) {
    return false;
  }

  const provider = new NodeTracerProvider({
    resource: resourceFromAttributes({ "service.name": "gateway-service" }),
    // The exporter reads the endpoint from OTEL_EXPORTER_OTLP_ENDPOINT on its own.
    spanProcessors: [new BatchSpanProcessor(new OTLPTraceExporter())],
  });
  // Also installs the W3C `traceparent` propagator and the async context manager.
  provider.register();

  registerInstrumentations({
    tracerProvider: provider,
    instrumentations: [
      new HttpInstrumentation({
        ignoreIncomingRequestHook: (req: IncomingMessage) => isUntraced(req.url),
      }),
      // One span per matched route is enough: a span for every middleware (body parser,
      // cookie parser, ...) would bury it.
      new ExpressInstrumentation({
        ignoreLayersType: [ExpressLayerType.MIDDLEWARE],
      }),
      // Adds `trace_id` and `span_id` to every log line written while a span is active,
      // which is what links a log line in Loki to its trace in Tempo.
      new PinoInstrumentation(),
    ],
  });

  return true;
}

startTracing();
