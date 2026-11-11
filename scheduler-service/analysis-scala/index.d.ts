/** Hand-written types for `kitchen.JsApi`. Keep in sync with `JsApi.scala`. */

export interface HistoricalOrder {
  /** Epoch ms the order entered PREPARING. */
  startedPrepAtEpochMs: number;
  /** Epoch ms the order left PREPARING. */
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

// Option groups, each mirroring a Scala parameter object.

/** An order that does not fit its slot is offered one, then two slots later; otherwise it is lost. */
export interface Deferral {
  acceptOneSlotLater: number;
  acceptTwoSlotsLater: number;
}

/** The static facts about the kitchen. `orderSizeProbabilities` maps pizzas per order to its probability. */
export interface KitchenSetupOptions {
  route: "PrepThenCook" | "CookThenPrep";
  orderSizeProbabilities: Record<number, number>;
  /** Erlang phases of a bake (default 1). */
  bakePhases?: number;
  /** Omit for no moving. */
  deferral?: Deferral;
}

/** The kitchen setup plus one service's staffing. */
export interface KitchenOptions extends KitchenSetupOptions {
  prepWorkers: number;
  cookStationCapacities: number[];
}

export interface StageRates {
  prepRatePerMinute: number;
  loadingRatePerMinute: number;
  cookRatePerMinute: number;
}

export interface Rates extends StageRates {
  lambdaPerMinute: number;
}

export interface SlotsOptions {
  lengthMinutes: number;
  count: number;
}

export interface GoalOptions {
  averageDelayCapMinutes: number;
  worstDelayCapMinutes: number;
  /** Orders are due this long after their slot ends (default 0). */
  leadTimeMinutes?: number;
  riskTolerance: number;
}

/** How a candidate quota is judged; each function documents its own defaults. */
export interface RuleOptions {
  epsilon?: number;
  confidence?: number;
  earlyRejection?: boolean;
  safetyMargin?: boolean;
  sequentialTest?: boolean;
}

export interface RecommendCapacityOptions {
  kitchen: KitchenOptions;
  rates: Rates;
  quotas: { min: number; max: number };
  slots: SlotsOptions;
  goal: GoalOptions;
  /** Defaults: epsilon 0.05, confidence 0.9, every option off. */
  rule?: RuleOptions;
  /** An integer. */
  seed: number;
}

export type CapacityRecommendation =
  | {
      found: true;
      admissionQuota: number;
      averageDelayExceedsCapProbability: number;
      worstDelayExceedsCapProbability: number;
      runs: number;
    }
  | { found: false };

/** One period's true conditions, hidden from the agent except for the staffing, the oven report and the declared times. */
export interface ScenarioPhase extends Rates {
  name: string;
  prepWorkers: number;
  cookStationCapacities: number[];
  ovenStatus?: string;
  estimatedBakeMinutes?: number;
  declaredMinutes?: { prep: number; loading: number; bake: number };
}

export interface AdaptationScenarioOptions {
  phases: ScenarioPhase[];
  kitchen: KitchenSetupOptions;
  slots: SlotsOptions;
  goal: GoalOptions;
  /** Defaults: epsilon 0.05, confidence 0.9, early rejection on, the other options off. */
  rule?: RuleOptions;
  agent: {
    minQuota: number;
    maxQuota: number;
    mode: "Auto" | "Advise";
    perception?: "StageLogs" | "ArrivalsOnly";
    memoryPeriods?: number;
    staffingAdvice?: boolean;
  };
  world: { initialQuota: number; ordersPerPeriod: number; outcomeRuns: number };
  seed: number;
}

export interface PeakProfileOptions {
  ordersPerSlot: number[];
  pastServices: number;
  kitchen: KitchenOptions;
  rates: StageRates;
  slotLengthMinutes: number;
  quotas: { min: number; max: number; step: number };
  goal: GoalOptions;
  /** Defaults: epsilon 0.05, confidence 0.9, early rejection and safety margin on. */
  rule?: RuleOptions;
  outcomeRuns: number;
  seed: number;
}

export declare const SchedulerAnalysis: {
  /** How often `candidateCapacity` would reject an order. Offline use only; `seed` must be an integer. */
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

  /** The largest quota meeting the goal; offline use only. Throws on a missing required option. */
  recommendCapacity(options: RecommendCapacityOptions): CapacityRecommendation;

  /** Demo: the closed-loop agent against a simulated kitchen, one row per phase (see `demo-node.js`). */
  runAdaptationScenario(options: AdaptationScenarioOptions): Record<string, unknown>[];

  /** Demo: the peak-profile proof of concept (see `demo-peak.js`). */
  runPeakProfilePoC(options: PeakProfileOptions): Record<string, unknown>;
};
