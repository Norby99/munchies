// Calls the Scala.js output (exported as SchedulerAnalysis) as scheduler-service would.
// Run: node demo-node.js [logs] (after `sbt fastLinkJS`).
const { SchedulerAnalysis } = require("./target/scala-3.4.0/scheduler-analysis-fastopt/main.js");

const history = [];
{
  let t = 0;
  const meanInterArrival = 4; // minutes
  const meanPrep = 18; // minutes
  let seed = 42;
  const rnd = () => {
    seed = (seed * 1103515245 + 12345) & 0x7fffffff;
    return seed / 0x7fffffff;
  };
  for (let i = 0; i < 400; i++) {
    t += -Math.log(rnd()) * meanInterArrival;
    const prep = -Math.log(rnd()) * meanPrep;
    const startedPrepAtEpochMs = t * 60_000;
    history.push({ startedPrepAtEpochMs, finishedPrepAtEpochMs: startedPrepAtEpochMs + prep * 60_000 });
  }
}

const fitted = SchedulerAnalysis.fitRates(history);
console.log("Fitted rates from historical orders (via ScalaJS)");
console.log(`  sample size : ${fitted.sampleSize}`);
console.log(`  lambda      : ${fitted.lambdaPerMinute.toFixed(4)} orders/min`);
console.log(`  mu          : ${fitted.muPerMinute.toFixed(4)} completions/min (mean prep ${(1 / fitted.muPerMinute).toFixed(1)} min)`);
console.log();

const horizon = 120;
// Loose precision, to keep the demo fast.
const epsilon = 0.03;
const confidence = 0.9;
console.log(`Candidate capacities over a ${horizon}-minute service window (starting empty):`);
console.log("cap  transient P(block)  +/- eps  steady-state P(block)  runs");
for (const capacity of [2, 3, 4, 5, 6, 8]) {
  const advice = SchedulerAnalysis.adviseCapacity(
    fitted.lambdaPerMinute,
    fitted.muPerMinute,
    capacity,
    horizon,
    42, // seed
    epsilon,
    confidence,
  );
  console.log(
    `${String(capacity).padStart(3)}  ${advice.transientBlockingProbability.toFixed(4).padStart(17)}` +
      `  ${advice.epsilon.toFixed(4)}` +
      `  ${advice.steadyStateBlockingProbability.toFixed(4).padStart(21)}` +
      `  ${advice.runs}`,
  );
}

// The quota search on a tandem pipeline, with illustrative rates and a short service.
console.log();
console.log("CapacityAgent: largest admissionQuota keeping delay under cap");
const recommendation = SchedulerAnalysis.recommendCapacity({
  kitchen: {
    route: "PrepThenCook",
    prepWorkers: 2,
    cookStationCapacities: [3], // one station, 3 places
    orderSizeProbabilities: { 1: 1.0 },
  },
  rates: {
    lambdaPerMinute: 1.0,
    prepRatePerMinute: 1.0,
    loadingRatePerMinute: 1.0, // same worker pool as prep -- loading a station isn't free
    cookRatePerMinute: 0.5, // mean 2 min per bake, up to 3 items at once
  },
  quotas: { min: 5, max: 20 },
  slots: { lengthMinutes: 15, count: 4 },
  goal: { averageDelayCapMinutes: 2.0, worstDelayCapMinutes: 5.0, riskTolerance: 0.2 },
  rule: { epsilon: 0.2, confidence: 0.7 },
  seed: 2026,
});
if (recommendation.found) {
  console.log(`  recommended admissionQuota : ${recommendation.admissionQuota} per 15-minute span`);
  console.log(`  P(avg delay > cap)         : ${recommendation.averageDelayExceedsCapProbability.toFixed(3)}`);
  console.log(`  P(worst-span delay > cap)  : ${recommendation.worstDelayExceedsCapProbability.toFixed(3)}`);
  console.log(`  runs per candidate         : ${recommendation.runs}`);
} else {
  console.log("  no quota in [5, 20] keeps delay under cap for this kitchen");
}

// The closed-loop agent on a pizzeria's Friday evenings, against the manager's static
// quota of 20. The scenario and its parameters are described in the SAAP report.
// Perception: ArrivalsOnly by default (what a real deployment can measure), or
// StageLogs with `node demo-node.js logs`. "?" marks staffing advice given under a
// slowdown factor, which cannot tell slow makers from a slow oven.
const normal = {
  name: "normal Friday",
  lambdaPerMinute: 5.5 / 15,
  prepRatePerMinute: 60 / 45,
  loadingRatePerMinute: 60 / 6,
  cookRatePerMinute: 1 / 5,
  prepWorkers: 2,
  cookStationCapacities: [7],
  declaredMinutes: { prep: 0.75, loading: 0.1, bake: 5 },
};
const ovenFault = {
  ...normal,
  name: "oven fault",
  cookRatePerMinute: 1 / 7,
  ovenStatus: "fault",
  estimatedBakeMinutes: 6,
  declaredMinutes: { ...normal.declaredMinutes, bake: 6 },
};
const newMaker = { ...normal, name: "new pizza maker", prepRatePerMinute: 60 / 65 };
const busierSeason = { ...normal, name: "busier season", lambdaPerMinute: 7 / 15 };
const phases = [
  normal,
  { ...normal, name: "topper absent", prepWorkers: 1 },
  normal,
  ovenFault,
  ovenFault,
  ovenFault,
  { ...normal, name: "oven repaired" },
  newMaker,
  newMaker,
  busierSeason,
  busierSeason,
];
const pizzasPerOrder = { 1: 0.28, 2: 0.3, 3: 0.16, 4: 0.14, 5: 0.06, 6: 0.04, 8: 0.01, 10: 0.01 };
const staticQuota = 20;
const outcomeRuns = 100;
// Full slots: 90% accept 15 minutes later, 50% accept 30 minutes later, the rest are lost.
const deferral = { acceptOneSlotLater: 0.9, acceptTwoSlotsLater: 0.5 };
const perception = process.argv[2] === "logs" ? "StageLogs" : "ArrivalsOnly";
const staffingAdvice = perception === "ArrivalsOnly";
console.log();
console.log(`AutonomousCapacityAgent (${perception}), Auto mode, pizzeria Friday 18:30-21:30: quota in pizzas per slot, bounds [8, 24]`);
console.log(
  perception === "StageLogs"
    ? "before each Friday: last Friday's log, the coming staff schedule and oven report; rates from the last 2 Fridays"
    : "before each Friday: last Friday's arrivals (orders/min over the last 2 Fridays), the coming staff schedule and declared stage times",
);
console.log("goal: lateness against slot end + 15 min, avg <= 3 min, worst slot <= 8 min, risk 20% with 90% confidence");
console.log(`breaches counted over ${outcomeRuns} simulated services per week; orders that do not fit are offered a later slot`);
console.log("mv: pizzas moved to a later slot per service (min: mean delay), lost: pizzas lost per service");
console.log("                         believed                      |  agent                                       | static quota                                 | staffing advice");
console.log("week    conditions        orders/min bake from  slow wk | quota avg  worst brA brW   mv  min  lost | quota avg  worst brA brW   mv  min  lost | target makers | alert");
const rows = SchedulerAnalysis.runAdaptationScenario({
  phases,
  kitchen: {
    route: "PrepThenCook",
    orderSizeProbabilities: pizzasPerOrder,
    bakePhases: 10, // Erlang-10 bakes, coefficient of variation 0.32
    deferral,
  },
  slots: { lengthMinutes: 15, count: 12 }, // 18:30-21:30
  goal: {
    averageDelayCapMinutes: 3.0,
    worstDelayCapMinutes: 8.0,
    leadTimeMinutes: 15.0, // a slot's orders are due 15 minutes after it ends
    riskTolerance: 0.2,
  },
  rule: {
    epsilon: 0.05,
    confidence: 0.9,
    safetyMargin: true, // accept only quotas within the tolerance with 90% confidence
    sequentialTest: true, // Wald's SPRT, settling each candidate as soon as the evidence is decisive
  },
  agent: {
    minQuota: 8,
    maxQuota: 24, // demand rarely exceeds 24 pizzas per slot
    mode: "Auto",
    perception,
    memoryPeriods: 2, // the agent fits its rates on the last 2 Fridays
    staffingAdvice,
  },
  world: {
    initialQuota: staticQuota, // also the baseline every week is compared against
    ordersPerPeriod: 66, // one Friday evening's log (12 slots x 5.5 orders)
    outcomeRuns, // simulated services each week's measured outcome is computed from
  },
  seed: 2026,
});
const bakeSource = { SameOvenLogs: "log", ReportedEstimate: "est", AnyLogs: "any", DeclaredTimes: "decl" };
const f = (x, w) => x.toFixed(2).padStart(w);
for (const r of rows) {
  console.log(
    `${String(r.period).padStart(6)}  ${r.conditions.padEnd(17)}` +
      `  ${f(r.believedLambdaPerMinute, 9)} ${f(1 / r.believedCookRatePerMinute, 4)} ${bakeSource[r.bakeRateSource].padEnd(4)}` +
      ` ${f(r.believedSlowdown, 4)} ${String(r.believedPrepWorkers).padStart(2)}` +
      ` | ${String(r.quotaInForce).padStart(5)} ${f(r.averageDelayMinutes, 4)} ${f(r.worstDelayMinutes, 5)}` +
      ` ${String(r.averageCapBreaches).padStart(3)} ${String(r.worstCapBreaches).padStart(3)}` +
      ` ${r.movedItems.toFixed(1).padStart(4)} ${r.moveMinutes.toFixed(0).padStart(4)} ${r.lostItems.toFixed(1).padStart(5)}` +
      ` | ${String(r.baselineQuota).padStart(5)} ${f(r.baselineAverageDelayMinutes, 4)} ${f(r.baselineWorstDelayMinutes, 5)}` +
      ` ${String(r.baselineAverageCapBreaches).padStart(3)} ${String(r.baselineWorstCapBreaches).padStart(3)}` +
      ` ${r.baselineMovedItems.toFixed(1).padStart(4)} ${r.baselineMoveMinutes.toFixed(0).padStart(4)} ${r.baselineLostItems.toFixed(1).padStart(5)}` +
      ` | ${staffingAdvice ? `${String(r.targetQuota).padStart(6)} ${((r.recommendedWorkers ? String(r.recommendedWorkers) : "none") + (r.staffingAdviceReliable ? " " : "?")).padStart(6)}` : "     -      -"}` +
      ` | ${r.alert}`,
  );
}
