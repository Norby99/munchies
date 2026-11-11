# scheduler-analysis (ScalaJS)

The offline capacity-planning tool for `scheduler-service`: Scala 3, compiled to a
CommonJS module via ScalaJS, and built on the actual course code from "Advanced Software
Modeling and Design" (`../../../asmd-public-models`, commit `3297344`), vendored
in rather than re-derived.

It answers the question *"given fitted arrival/service rates, how often would a
candidate `CapacityPolicy.maxCapacityUnits` reject an order?"* and is deliberately kept
**offline**, outside `scheduler-service`'s HTTP request path. It is the only
implementation: an earlier TypeScript copy under `../src/main/ts/analysis/` was removed
in favour of consuming this module's compiled output.

## Using it from scheduler-service

This directory is also a local npm package (`package.json`, `index.d.ts`), linked into
`scheduler-service` as the `scheduler-analysis` dev dependency (`file:./analysis-scala`):

```ts
import { SchedulerAnalysis } from "scheduler-analysis";

const rates = SchedulerAnalysis.fitRates(history);
```

- `package.json`'s `main` points at the `fastLinkJS` output under `target/`, which is
  not committed: run `sbt fastLinkJS` before importing the package.
- `index.d.ts` is written by hand (ScalaJS emits no TypeScript declarations) and must be
  kept in sync with `kitchen/JsApi.scala`.
- It is a dev dependency on purpose: the production Docker image prunes it, so the
  analysis must not be imported from the service's runtime code without also adding an
  sbt build stage to the `Dockerfile`.

## Layout

```
src/main/scala/
  u06/modelling/{System,PetriNet,SystemAnalysis}.scala   vendored, unmodified
  u06/utils/MSet.scala                                   vendored, unmodified
  u07/modelling/CTMC.scala           vendored, unmodified — CTMC[S]: rated transition systems
  u07/modelling/CTMCSimulation.scala vendored, ONE deviation (see file header) — Gillespie's SSA
  u07/modelling/SPN.scala            vendored, unmodified — Stochastic Petri Net = Trn + a rate
  u07/utils/{MSet,Stochastics,Time}.scala   vendored, unmodified
  u08/modelling/CTMCExperiment.scala vendored, import path adjusted (see file header) —
                                      PRISM-style statistical model checking
  kitchen/SpnDsl.scala               `cond ~ rate ~> eff ^ inh` builder for SPN.Trn, parallel to
                                      u06.modeling.PetriNet's `~~>`/`^^^` (see below) — ours
  kitchen/KitchenQueue.scala         the M/M/c/c kitchen-capacity model, as an SPN (see below) — ours
  kitchen/RateEstimation.scala       fits lambda/mu from historical order timestamps — ours
  kitchen/CapacityAdvisor.scala      ties it together: simulated + closed-form (Erlang B) blocking probability — ours
  kitchen/Statistics.scala           PRISM-style confidence bound (requiredRuns/epsilon) — ours, see below
  kitchen/KitchenPipeline.scala      tandem prep+cook pipeline for one product/route: orders of
                                      several items, an oven of independent places with Erlang
                                      bakes, a per-span (not concurrency) admission quota — ours
  kitchen/SpnSimulator.scala         Gillespie's direct method for an SPN, building only the
                                      successor that fires (see below) — ours
  kitchen/ServiceSimulation.scala    runs a whole multi-span service, measures per-span delay — ours
  kitchen/Planning.scala             the parameter objects the agents share: Slots, KitchenModel,
                                      ProfiledKitchen, Goal, DecisionRule, Assessment — ours
  kitchen/CapacityAgent.scala        model-based, utility-based agent: searches admissionQuota
                                      for the largest value keeping delay under cap — ours
  kitchen/AutonomousCapacityAgent.scala  CapacityAgent inside a perceive -> decide -> act loop:
                                      refits its rates every period, applies (Auto) or only
                                      recommends (Advise) the quota, falls back + alerts — ours
  kitchen/Calibration.scala          how much slower than declared a service ran, from its
                                      clearing time (deployment perception) — ours
  kitchen/StaffingAdvisor.scala      staffing advice: makers needed for the quota to cover demand — ours
  kitchen/SimulatedKitchen.scala     the agent's environment for demos: hidden, changing true
                                      conditions; measures each period vs a static quota — ours
  kitchen/PeakProfile.scala          proof of concept: per-slot demand and per-slot quotas — ours
  kitchen/util/FastMSet.scala        map-backed MSet, same trait as the vendored one; the
                                      simulation's markings use it (about 9x faster) — ours
  kitchen/JsApi.scala                the only file that imports scala.scalajs.js — @JSExportTopLevel("SchedulerAnalysis")
```

`u06`/`u07`/`u08` are vendored from the course; `kitchen/*` is this project's own
application code built on top of it. Two vendored files needed a small, explicitly
documented deviation to work here — see each file's header comment, and "Deviations
from the vendored source" below.

Note that `u07.modeling.SPN` imports `u07.utils.MSet`, not `u06.utils.MSet` —
that's how the course itself wrote it (each lesson folder is self-contained rather
than reusing the previous lesson's copy), so `kitchen/KitchenQueue.scala` uses u07's
MSet too, even though u06 is also vendored here for context/lineage.

## The kitchen model, as a Petri Net

Rather than a raw integer-state CTMC, the kitchen's capacity
is modelled the way the course models bounded resources: a `Free` place starts with
`capacity` tokens and feeds an `InPrep` place.

```
Free --arrival (lambda)--> InPrep       (Trn(cond=MSet(Free), eff=MSet(InPrep)))
InPrep --completion (n*mu)--> Free      (Trn(cond=MSet(InPrep), eff=MSet(Free)))
```

The arrival transition simply **cannot fire** once `Free` is empty — ordinary PN token
conservation (`m extract cond` returning `None`) enforces the capacity cap on its own,
no inhibitor arc needed. This is an M/M/c/c ("Erlang loss") queue, matching what
`ScheduleOrder` does today when a slot is full (409 Conflict) — just as a continuous-time
process with rates instead of a fixed per-slot headcount. The completion rate is
`m(InPrep) * mu` — "the rate could be a formula of the number of tokens in the incoming
place" (07-StochasticModelling, slide 29) — because each in-flight order finishes
independently (memoryless).

`CapacityAdvisor.erlangBlockingProbability` is the closed-form steady-state blocking
probability for the same queue (plain queueing theory, not vendored), used only to
cross-check the simulator (`KitchenQueueSpec`) — it can't answer the practically
relevant *transient* question ("blocking probability in the first 30 minutes after
opening, starting empty"), which is exactly where Gillespie simulation earns its keep.

## SpnDsl: `cond ~ rate ~> eff ^ inh`

`u06.modeling.PetriNet` has its own fancy syntax for building a `Trn` (`~~>`/`^^^`, via
an `AsMarking` typeclass that lets a single place, a tuple of places, or a full marking
stand on either side). `SpnDsl` is the same idea for `SPN.Trn`, which has one more slot
(the rate) between precondition and effect: `cond ~ rate ~> eff ^ inh`. `KitchenQueue`'s
two transitions are written with it as the worked example:

```scala
Free ~ lambdaPerMinute ~> InPrep
InPrep ~ ((m: MSet[Place]) => m(InPrep) * muPerMinute) ~> Free
```

Can't literally reuse u06's `AsMarking`/`~~>`/`^^^`: those are typed over
`u06.utils.MSet`, and `u07.modeling.SPN` (what every `kitchen/*` model actually builds
on) imports the separate `u07.utils.MSet` — a different type, and one that dropped
`ofTuple` entirely. So `SpnDsl` rebuilds an `AsMarking` of its own against `u07`'s
`MSet` (same tuple-flattening trick u06's `ofTuple` uses), and adds an `AsRate`
typeclass alongside it so the rate slot accepts either a constant `Double` or a full
`MSet[P] => Double`.

Precedence is what makes the one-line chain work with no parentheses: `~` and `~>` both
start with `~` (Scala's highest-precedence operator-character class, grouping
left-to-right), while `^` starts with `^` (a lower class) — so `cond ~ rate ~> eff ^
inh` parses as `((cond ~ rate) ~> eff) ^ inh`, the exact left-to-right pipeline
intended, for the same structural reason `~~>`/`^^^` already compose that way in u06.

## KitchenPipeline, ServiceSimulation, CapacityAgent: tandem stages and a tuning agent

A single product's full journey through the kitchen is two stages in series, not one:
**prep** (serial, one item per worker) and **cook**, with a short **loading** step in
between (a worker placing the item in the oven, holding that station's one loading slot).
Orders arrive in batches: an order admits `s` items at once (`s` drawn from
`Config.orderSizeProbabilities`), and an order that doesn't fit the slot's remaining quota
is not admitted at all -- unless `Config.deferral` is set. Then, as `scheduler-service`
does ("that slot is full, pick another"), the customer is offered the first later slot
with room for the whole order: one slot later, accepted with `acceptOneSlotLater`
probability (0.9 in the demos), else two slots later, accepted with `acceptTwoSlotsLater`
(0.5), else the order is lost. Each outcome is its own transition, at the order size's
arrival rate times the outcome's probability, with a rate of zero unless the order doesn't
fit (a marking-dependent rate, since "fewer than s tokens" is not an inhibitor arc). A
moved order takes its room in the later slot at once (`RoomNext`/`RoomAfter`), ahead of
that slot's own orders, and waits in `PendingNext`/`PendingAfter` until the driver opens
that slot (`advanceSlot`), when it is admitted and counts as the slot's own. Ledgers
(`MovedOne`, `MovedTwo`, `Lost`) let each service report what was moved and lost; an
`Open` token, removed at closing, stops orders from arriving while the kitchen drains.

The cook stage is a station of independent places (`Config.cookStationCapacities`, e.g. 7
for a pizza oven): an item goes in as soon as it's loaded and comes out after its own
bake, so a station of capacity c is c parallel servers, not a tray baked as one batch. A
bake is Erlang-distributed with `Config.bakePhases` phases (`Baking(station, phase)`), each
exponential at `bakePhases * cookRatePerMinute`: the mean stays `1 / cookRatePerMinute`
and the coefficient of variation is `1 / sqrt(bakePhases)`. This is the standard
phase-type approximation of a near-deterministic duration within a CTMC: with a single
exponential phase, 5% of 5-minute bakes would last over 15 minutes, which no real oven
does. Items need no colouring for it, since every item in a phase completes it at the
same rate independently of the others.

An earlier version modelled the oven as batch lanes (fill a tray, bake it as one, with a
second lane allowed to top up the leftover room), a way to express batch service without
coloured tokens. It was removed: a deck oven doesn't batch, and the lanes left most of
its places idle whenever a lane committed to a small batch, which made the whole model
far too pessimistic for a pizzeria.

Which stage comes first is fixed when the `Config` is built (`Route.PrepThenCook` for a
pizza; `Route.CookThenPrep` for something griddled, then assembled) -- one net models
one product/route, not a mix.

Admission here is deliberately *not* a concurrency cap the way `KitchenQueue`'s capacity
is. `Admission` is a per-time-span quota: consumed when an order is admitted, never
refunded by a completion, and reset only by `resetAdmission` -- called by whatever drives
the simulation across span boundaries on its own fixed clock, never by a transition
inside the SPN. That split exists because a deterministic "every 15 minutes" event has
no honest representation among exponential, memoryless transitions; rather than fake it
(the way the loading session fakes an instantaneous decision to stop), the one genuinely
deterministic part of this model is kept out of the CTMC entirely and handled as
ordinary control flow in `ServiceSimulation.simulateService`.

`simulateService` runs a whole service as a sequence of fixed-length spans (e.g. 12
back-to-back 15-minute windows = a 3-hour turn), re-opening the quota at each boundary,
*not* draining the backlog between spans -- a late span is expected to eat into the next
one's throughput, same as a real kitchen. Delay is measured per span, not per item: "did
span k's whole admitted cohort clear by its own clock-fixed deadline" (`spanLengthMinutes
* k`, never shifted by how late earlier spans ran). That's an order-dependent question
("did *this* cohort make it"), unlike a plain sum, so it can't reuse the
matching-doesn't-matter trick a pure total would get for free -- it needs to know which
`Exit` events belong to which admission cohort. Since every item is fungible (no token
coloring anywhere in this package), the only sensible attribution is FIFO at the
whole-pipeline level: the k-th unit to exit, chronologically, belongs to whichever
cohort the running admitted count had reached when it was admitted. `perSpanDelays`
walks the cumulative `Exit`-count history for the first point crossing each cohort's
threshold, entirely from `(time, Exit count)` pairs -- no per-item timestamps needed.

`CapacityAgent` is the model-based, utility-based agent this whole exercise was built
towards: given the model, it searches `admissionQuota` for the largest value keeping a
service's average and worst-span delay under caps -- "maximize throughput subject to a
delay cap", not "minimize delay" (trivially solved by admitting nobody). It reduces each
candidate to a Bernoulli-type "did this service breach its cap" indicator before reusing
`Statistics.requiredRuns`/`epsilonFor`, since those are Hoeffding-based and only valid
for a quantity bounded in `[0,1]` -- raw delay minutes aren't, without an arbitrary
rescaling. The search itself is a single top-down linear scan, relying on (not proving)
that delay is stochastically non-decreasing in `admissionQuota` for a fixed kitchen; see
`CapacityAgent`'s header for why a full scan was chosen over a binary search despite that
monotonicity (each evaluation is a noisy Monte Carlo estimate, and binary-searching a
noisy monotonic signal can take a wrong turn on one unlucky sample).

`AutonomousCapacityAgent` closes the loop around it, so the agent covers all four
capabilities of an autonomous system rather than only decision-making:

- **Sensing/perception** -- before each period's service it perceives the *previous*
  service's order log (a `StageSample` per order: arrival time plus prep, loading and bake
  durations) and the *coming* service's staffing and `OvenReport` (the oven's state and,
  outside its normal state, the fornaio's estimate of the bake time). It never sees the
  service it decides for, so a change only the logs reveal reaches it one period late.
  `RateEstimation.fitStagesAcross` refits arrivals, prep and loading from the last
  `memoryPeriods` logs (2 in the demo); stage durations are pooled, and the arrival rate is
  total gaps over total observed time, since each log has its own clock and flattening them
  would multiply it by the number of logs. Each log is tagged with the oven state reported
  for its service, and the bake rate comes from the last `memoryPeriods` logs with the oven
  in the state now reported, else from the fornaio's estimate, else (with an alert) from
  every recent log. After a repair, the agent goes straight back to the working oven's logs.
  That is `Perception.StageLogs`, which only a simulated kitchen can feed: nobody times each
  pizza at each stage. `Perception.ArrivalsOnly` is the deployment version: the agent fits
  only the arrival rate (`RateEstimation.arrivalRateAcross`, reading nothing but arrival
  times) and takes prep, loading and bake times from the kitchen profile the staff declare
  (`Staffing.declaredTimes`). After each service, `Calibration` finds the factor by which the
  kitchen ran slower than declared: the one at which the model, under the quota that was in
  force and the demand the service's own log shows, reproduces the service's mean clearing
  time (how long after each slot's end its last order was ready, measurable from READY
  timestamps). From 5% slower up, the agent plans with the declared times stretched by that
  factor and advises the manager to check them; a new declaration supersedes the factor.
  Total times cannot tell which stage is slow, so the advice says only "slower than declared".
- **Decision-making** -- `CapacityAgent.recommendCapacity` on the refitted model. With
  `safetyMargin`, a quota is accepted only if each breach-probability estimate plus epsilon
  stays within the tolerance: within tolerance with probability `confidence`. With
  `sequentialTest`, each candidate is judged by Wald's sequential probability ratio test
  (`SequentialTest`, as in PRISM/UPPAAL statistical model checking), weighing a breach
  probability of `tolerance - 2 * epsilon` against one of `tolerance` with error `1 -
  confidence` each way: it stops as soon as the evidence is decisive, so candidates far
  from the tolerance take a few dozen services instead of up to `requiredRuns`, which
  stays the cap.
- **Actuation** -- in `Mode.Auto` it applies the quota through the environment's actuator,
  only ever within the manager's `[minQuota, maxQuota]`; `Mode.Advise` only recommends. With
  `staffingAdvice`, `StaffingAdvisor` also advises on the staff schedule: the quota that
  would serve the whole demand in 4 slots of 5 (a quantile of the per-slot demand), and the
  fewest pizza makers with which it meets the goal, or none, when the oven is what limits
  the service.
  The staff schedule is the manager's to change, so this is always advice.
- **Adaptability** -- a missing worker or a reported oven fault moves the decision before
  the service it affects; a change in demand, one service later. No feasible quota -> it
  applies `minQuota` and raises an alert; a log too small to refit -> it keeps its previous
  beliefs and raises an alert; an oven state with no log and no estimate -> it uses every
  recent log and raises an alert.

`SimulatedKitchen` plays the environment in `demo-node.js`: its true conditions change
between periods and are never shown to the agent, and each period is also measured under
the static initial quota, so the output compares acting on the agent's decisions with not
having an agent at all. Besides mean delays, each period reports how many of its simulated
services breached each cap, the frequency the risk tolerance is about. The pizzeria scenario
(12 slots of 15 minutes, quotas 8 to 24 pizzas, 9 Fridays) judges candidate quotas on 600
simulated services each (epsilon 0.05 at 90% confidence) with the safety margin, rejects a
candidate as soon as its breaches already exceed what the margin allows, so only the
accepted quota needs all of them, and measures each week over 100 services. The whole demo
takes 14 minutes on our machine with the sequential test, run side by side with the
comparison below (7 minutes) and `demo-peak.js` (3 minutes); with a fixed 600 services per
accepted candidate it took an hour. By default it runs the deployment version (`Perception.ArrivalsOnly`
with calibration and staffing advice); `node demo-node.js logs` runs the same weeks with
`Perception.StageLogs`.

`demo-peak.js` is a proof of concept of a Friday whose demand peaks around 20:00
(`PeakProfile`): the agent fits one arrival rate per slot from past Fridays (smoothed with
the neighbouring slots) and plans one quota on that profile, or one quota per slot (greedy
ascent, then a check of the final plan on fresh runs, since a plan built from many tests
near the limit is optimistic). `ServiceSimulation.simulateProfiledService` simulates it exactly, since each
slot is simulated on its own from the marking at its start.

`ServiceSimulation` runs on `SpnSimulator` rather than the vendored `SPN.toCTMC` +
`CTMCSimulation.newSimulationTrace`. Both are Gillespie's direct method, but the vendored pair
builds the successor marking of every enabled transition at every step, hashes each one into
a `Set[Action]`, and does it twice (once for the absorbing-state test, once to draw), although
only one successor is used. `SpnSimulator` evaluates each transition's enabling and rate,
draws one, and builds that successor alone: on the pizzeria scenario, a simulated service got
about 20x faster. It also keeps the transitions in a fixed order (by their places) instead of
a `Set`'s, and sums the rates of transitions that reach the same marking, where the vendored
`Set[Action]` would merge them into one; `SpnSimulatorSpec` checks it against the vendored
simulator. The vendored code is unchanged and still runs `KitchenQueue`/`CapacityAdvisor`.

Markings are `FastMSet`s (map-backed) rather than the vendored `MSet`, which rebuilds a token
list on every `union`/`diff`; `KitchenPipeline.initialMarking` builds one and every later
marking derives from it. The vendored code itself is unchanged.

`KitchenPipeline.Place` hashes by content rather than by identity, and `SpnSimulator` orders
transitions by their places, so a seed gives the same run in any process. With identity hash
codes, the vendored simulator's `Set` order, and with it the run, depended on what the process
had hashed before, e.g. the scenario alone versus after the other demos.

## Deviations from the vendored source

Kept to the minimum needed to actually run; each is explained in the file it's in.

1. **`u07/modelling/CTMCSimulation.scala`** — the original re-emits `Event(t, s)` (the
   same `t`) forever once a state is absorbing. Fine for `.take(n)`-style consumption,
   but an infinite loop (and an OOM — `LazyList` memoizes every element it forces) the
   moment a caller does `.takeWhile(_.time <= timeBound)`, because `t` never exceeds
   the bound. Fixed by tagging the post-absorption event `Double.PositiveInfinity`
   instead. `CTMCSimulationSpec` has a regression test for this.
2. **`u08/modelling/CTMCExperiment.scala`** — the course keeps each lesson folder
   self-contained, so u08's own copy re-imports its own pasted `CTMC`/`CTMCSimulation`/
   `Stochastics`/`MSet`, and in u08 specifically those are declared under a typo'd
   package (`scala.u08.utils`, present in the fetched source, not "fixed" here since
   we don't vendor that copy at all). Rather than duplicate u07's classes a second
   time, this file imports `u07.modelling.{CTMC, CTMCSimulation.*}` directly.
3. **Not a code change, but worth knowing**: `CTMCExperiment.experiment`'s `given rnd:
   Random` is defined inside that same object and resolved lexically — callers cannot
   supply their own seed. `kitchen.CapacityAdvisor` therefore doesn't call
   `CTMCExperiment.experiment`; it calls `CTMCSimulation.newSimulationTrace` directly
   with a caller-supplied `java.util.Random`, and `kitchen.Statistics` (not vendored)
   provides the epsilon/confidence math `CTMCExperiment.experiment` doesn't expose.
   `CTMCExperiment` is still genuinely exercised, unseeded, by `CTMCExperimentSpec`.

## Building

Requires a JDK (21 was used to develop this) and network access to Maven Central on
first run — everything else (Scala compiler, ScalaJS compiler plugin and linker,
scalatest) is fetched by sbt/coursier. No system-wide sbt install is required; use
[sbt-extras](https://github.com/paulp/sbt-extras) if you don't have `sbt` on your PATH.

```
sbt compile        # type-check everything
sbt test           # scalatest specs, run under Node via scalajs-env-nodejs
sbt fastLinkJS      # produces target/scala-3.4.0/scheduler-analysis-fastopt/main.js
sbt fullLinkJS       # optimized/production build, same output shape under -fullopt/
```

`fastLinkJS`/`fullLinkJS` emit a CommonJS module exporting `SchedulerAnalysis`
(`adviseCapacity`, `fitRates`, `erlangBlockingProbability`, and `recommendCapacity`,
`runAdaptationScenario`, `runPeakProfilePoC`, which each take one options object grouped
like the Scala parameter objects; the shapes are in `index.d.ts`) — see `demo-node.js` for
a plain `require()` against it, or `npm run analyze:scala` in `../package.json`.

**Known gap in this environment:** `sbt test` runs scalatest specs inside a real Node
process via `scalajs-env-nodejs`, which needs sbt's own JVM and a spawned Node process
resident at once. On a memory-constrained machine this can get killed by an OOM
guard (`earlyoom` et al.) right as the Node process starts, before any test output —
that's a resource ceiling, not a test failure. `sbt compile`/`Test/compile` (type
checking both main and test sources) and `fastLinkJS` + `node demo-node.js` (the
actual runtime path scheduler-service uses) don't need that second process and are
the more reliable way to sanity-check changes here under memory pressure; re-run
`sbt test` once more headroom is available.

`adviseCapacity` is not free — it runs `requiredRuns(epsilon, confidence)` full
Gillespie simulations (e.g. ~18k at the default epsilon=0.01/confidence=0.95). That's
fine for its actual use (an offline/periodic job), but `demo-node.js` deliberately
asks for a looser epsilon=0.03/confidence=0.9 (~1.7k runs, seconds not minutes) purely
so the demo itself stays quick.

## Reuse note

`u06`–`u08` are verbatim copies of `asmd-public-models` at commit `3297344` (except the
two documented deviations above), not a live source dependency — sbt has no
straightforward way to pull unpublished sources from a sibling git repository without
either publishing an artifact or a fragile filesystem-relative-path
`unmanagedSourceDirectories` link (which would break for anyone who doesn't have both
repos cloned side by side). If `asmd-public-models` changes again, re-sync by hand.
