// Proof of concept: a Friday whose demand peaks around 20:00 (see the SAAP report).
// Run: node demo-peak.js (after `sbt fastLinkJS`).
const { SchedulerAnalysis } = require("./target/scala-3.4.0/scheduler-analysis-fastopt/main.js");

const ordersPerSlot = [3, 3, 4, 5, 6, 7, 8, 8, 7, 6, 5, 4]; // 18:30 ... 21:15, 66 in all
const slotLabels = ordersPerSlot.map((_, k) => {
  const minutes = 18 * 60 + 30 + 15 * k;
  return `${Math.floor(minutes / 60)}:${String(minutes % 60).padStart(2, "0")}`;
});
const outcomeRuns = 300;
const result = SchedulerAnalysis.runPeakProfilePoC({
  ordersPerSlot,
  pastServices: 8, // Fridays of logs the profile is fitted from
  kitchen: {
    route: "PrepThenCook",
    prepWorkers: 2,
    cookStationCapacities: [7], // one oven of 7 places
    orderSizeProbabilities: { 1: 0.28, 2: 0.3, 3: 0.16, 4: 0.14, 5: 0.06, 6: 0.04, 8: 0.01, 10: 0.01 },
    bakePhases: 10,
    // Full slots: 90% accept 15 minutes later, 50% accept 30 minutes later.
    deferral: { acceptOneSlotLater: 0.9, acceptTwoSlotsLater: 0.5 },
  },
  rates: { prepRatePerMinute: 60 / 45, loadingRatePerMinute: 60 / 6, cookRatePerMinute: 1 / 5 },
  slotLengthMinutes: 15,
  quotas: { min: 8, max: 24, step: 2 }, // step: of the per-slot search
  goal: { averageDelayCapMinutes: 3.0, worstDelayCapMinutes: 8.0, leadTimeMinutes: 15.0, riskTolerance: 0.2 },
  rule: {
    epsilon: 0.07, // 306 services per candidate; accepted at breach probability <= 0.13
    confidence: 0.9,
    sequentialTest: true, // Wald's SPRT, as in demo-node.js
  },
  outcomeRuns,
  seed: 2026,
});

const pad = (x, w) => String(x).padStart(w);
console.log("Peak-profile proof of concept: a normal Friday, 66 orders peaking at 20:00-20:30");
console.log(`slot        ${slotLabels.map((l) => pad(l, 5)).join(" ")}`);
console.log(`true orders ${result.trueOrdersPerSlot.map((x) => pad(x, 5)).join(" ")}`);
console.log(`fitted      ${result.fittedOrdersPerSlot.map((x) => pad(x.toFixed(1), 5)).join(" ")}`);
console.log(`(${result.runsPerCandidate} simulated services per candidate plan; outcomes over ${outcomeRuns} services;` +
  ` demand ${result.demandItems.toFixed(0)} pizzas per evening)`);
console.log();
console.log(
  `per-slot search reached ${result.searchedQuotas.join(" ")}` +
    (result.searchPassedCheck ? "" : ", which failed the check on fresh runs: kept the single quota"),
);
console.log();
for (const p of result.plans) {
  console.log(`${p.name}`);
  console.log(`  quotas      ${p.quotas.map((q) => pad(q, 5)).join(" ")}`);
  console.log(
    `  believed    breach avg ${p.believedAverageBreach.toFixed(2)} worst ${p.believedWorstBreach.toFixed(2)},` +
      ` ${p.believedAdmittedItems.toFixed(1)} pizzas admitted`,
  );
  console.log(
    `  true        breaches avg ${p.averageCapBreaches} worst ${p.worstCapBreaches} of ${outcomeRuns},` +
      ` lateness avg ${p.averageDelayMinutes.toFixed(2)} worst ${p.worstDelayMinutes.toFixed(2)} min,` +
      ` ${p.admittedItems.toFixed(1)} pizzas admitted (${p.movedItems.toFixed(1)} of them moved later), ${p.lostItems.toFixed(1)} lost`,
  );
}
