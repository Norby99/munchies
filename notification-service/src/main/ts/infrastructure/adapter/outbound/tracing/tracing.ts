import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { KafkaJsInstrumentation } from "@opentelemetry/instrumentation-kafkajs";
import { PinoInstrumentation } from "@opentelemetry/instrumentation-pino";
import { resourceFromAttributes } from "@opentelemetry/resources";
import { BatchSpanProcessor } from "@opentelemetry/sdk-trace-base";
import { NodeTracerProvider } from "@opentelemetry/sdk-trace-node";

/**
 * Distributed Tracing side of the Observability pattern.
 *
 * See gateway's one for further understanding.
 */

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
    resource: resourceFromAttributes({ "service.name": "notification-service" }),
    spanProcessors: [new BatchSpanProcessor(new OTLPTraceExporter())],
  });
  provider.register();

  registerInstrumentations({
    tracerProvider: provider,
    instrumentations: [
      new KafkaJsInstrumentation(),
      new PinoInstrumentation(),
    ],
  });

  return true;
}

startTracing();
