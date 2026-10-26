// Proves the ScalaJS -> Node boundary: requires the fastLinkJS CommonJS output and
// calls into kitchen.JsApi (exported as SchedulerAnalysis) exactly the way
// scheduler-service would. Run: node demo-node.js (after `sbt fastLinkJS`).
const { SchedulerAnalysis } = require("./target/scala-3.4.0/scheduler-analysis-fastopt/main.js");

const history = [];
{
  let t = 0;
  const meanInterArrival = 4; // minutes
  const meanPrep = 18; // minutes
  let seed = 42;
  const rnd = () => {
    // tiny deterministic LCG, good enough for a demo fixture
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
// A demo-friendly epsilon/confidence (~1-2k runs instead of ~18k at 0.01/0.95) — this
// is an offline tool, so a real capacity-planning run can afford the tighter, slower
// bound; this script is just proving the ScalaJS -> Node wiring end to end.
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

// The model-based, utility-based agent: given a kitchen's shape and rates, search for
// the largest per-span admission quota that keeps a whole prep->cook service's delay
// under cap. Illustrative rates below, not the fitted ones above -- those were fit for
// the single-stage demo at a 120-minute horizon (mean prep ~19 min), which would swamp
// any 15-minute span regardless of quota; this is a different model (a tandem pipeline)
// with its own, deliberately faster, illustrative prep/cook rates. Demo-friendly
// epsilon/confidence and a short 4-span service, same reasoning as the horizon/epsilon
// choice above -- a real planning run would tighten both.
console.log();
console.log("CapacityAgent: largest admissionQuota keeping delay under cap");
const recommendation = SchedulerAnalysis.recommendCapacity(
  "PrepThenCook",
  2, // prepWorkers
  [3], // cookStationCapacities (one station, 3 slots)
  { 1: 1.0 }, // orderSizeProbabilities
  1.0, // lambdaPerMinute
  1.0, // prepRatePerMinute
  1.0, // loadingRatePerMinute (same worker pool as prep -- loading a station isn't free)
  0.5, // cookRatePerMinute (mean 2 min per bake, up to 3 items at once)
  5, // minQuota
  20, // maxQuota
  15, // spanLengthMinutes
  4, // spans
  2.0, // averageDelayCapMinutes
  5.0, // worstDelayCapMinutes
  0.2, // riskTolerance
  2026, // seed
  0.2, // epsilon
  0.7, // confidence
);
if (recommendation.found) {
  console.log(`  recommended admissionQuota : ${recommendation.admissionQuota} per 15-minute span`);
  console.log(`  P(avg delay > cap)         : ${recommendation.averageDelayExceedsCapProbability.toFixed(3)}`);
  console.log(`  P(worst-span delay > cap)  : ${recommendation.worstDelayExceedsCapProbability.toFixed(3)}`);
  console.log(`  runs per candidate         : ${recommendation.runs}`);
} else {
  console.log("  no quota in [2, 10] keeps delay under cap for this kitchen");
}
