/**
 * Hand-written types for the ScalaJS output of `kitchen.JsApi` (ScalaJS does not emit
 * TypeScript declarations). Keep in sync with `src/main/scala/kitchen/JsApi.scala`.
 */

/** One historical order's kitchen-occupancy window, as reconstructed from order-service's status timestamps. */
export interface HistoricalOrder {
  /** epoch ms the order entered PREPARING (kitchen occupancy start). */
  startedPrepAtEpochMs: number;
  /** epoch ms the order left PREPARING (became READY or later). */
  finishedPrepAtEpochMs: number;
}

export interface FittedRates {
  /** Orders entering preparation per minute. */
  lambdaPerMinute: number;
  /** 1 / mean preparation duration, in minutes^-1. */
  muPerMinute: number;
  sampleSize: number;
}

export interface CapacityAdvice {
  candidateCapacity: number;
  lambdaPerMinute: number;
  muPerMinute: number;
  /** Simulated blocking probability over the bounded time horizon, starting from an empty kitchen. */
  transientBlockingProbability: number;
  /** Half-width of the confidence interval around transientBlockingProbability. */
  epsilon: number;
  confidence: number;
  runs: number;
  /** Closed-form steady-state blocking probability (Erlang B) for the same lambda/mu/capacity. */
  steadyStateBlockingProbability: number;
}

export declare const SchedulerAnalysis: {
  /**
   * Estimates how often `candidateCapacity` would reject an arriving order during a
   * service window of `timeHorizonMinutes`. Runs thousands of simulations: offline use only.
   * All arguments are required; `seed` must be an integer.
   */
  adviseCapacity(
    lambdaPerMinute: number,
    muPerMinute: number,
    candidateCapacity: number,
    timeHorizonMinutes: number,
    seed: number,
    epsilon: number,
    confidence: number,
  ): CapacityAdvice;

  erlangBlockingProbability(offeredLoad: number, capacity: number): number;

  /** Throws if fewer than 2 orders are given, or if any timestamps are inconsistent. */
  fitRates(history: readonly HistoricalOrder[]): FittedRates;
};
