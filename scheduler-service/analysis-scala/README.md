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
  kitchen/KitchenStation.scala       generalizes KitchenQueue to batch arrivals and batch-service
                                      stations (an oven tray, not one item per server) — ours
  kitchen/RateEstimation.scala       fits lambda/mu from historical order timestamps — ours
  kitchen/CapacityAdvisor.scala      ties it together: simulated + closed-form (Erlang B) blocking probability — ours
  kitchen/Statistics.scala           PRISM-style confidence bound (requiredRuns/epsilon) — ours, see below
  kitchen/KitchenPipeline.scala      tandem prep+cook pipeline for one product/route, with a
                                      per-span (not concurrency) admission quota — ours
  kitchen/ServiceSimulation.scala    runs a whole multi-span service, measures per-span delay — ours
  kitchen/CapacityAgent.scala        model-based, utility-based agent: searches admissionQuota
                                      for the largest value keeping delay under cap — ours
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

## KitchenStation: batch arrivals and batch-service stations

`KitchenQueue` treats every order and every "server" as a single unit — realistic for
a grill or a fry basket, not for an oven that bakes several items together in roughly
the same time regardless of how full the tray is. `KitchenStation` generalizes it along
both axes that `KitchenQueue` collapses to 1:

- **Batch arrivals** — an order admits `s` items at once (`s` drawn from
  `Config.orderSizeProbabilities`), not always exactly 1. An order that doesn't fully
  fit the kitchen's remaining `Capacity` is lost entirely, never partially admitted.
- **Batch service** — each station holds up to `stationCapacity` items, always
  preferring a full load, and only accepting a partial load when there genuinely isn't
  enough waiting to fill it. `KitchenQueue` is exactly `KitchenStation`'s
  `stationCapacity = 1` case (`KitchenStationSpec` checks this against the same Erlang B
  closed form `KitchenQueueSpec` cross-checks against).
- **One, bounded, top-up** — if a station commits to a partial bake, a second
  independent lane on the *same* physical station may open to take a *later* arrival
  into the leftover room (capped at exactly what's left), but never a third lane: the
  second lane only opens once the first has already committed to baking, and once
  committed neither lane re-opens until it completes and goes idle again.

Mechanically, this needed one structural trick the vendored SPN doesn't support
directly: `Trn.inh` is a *presence* test (`u07.utils.MSet.disjoined` — "is this place
non-empty"), not a magnitude test ("does it hold at least N"), so a transition can't
say "load however many are waiting, capped at K" in one step (`eff` is a fixed
multiset, it can't depend on the current marking). `KitchenStation` instead runs a small
per-lane automaton that grabs one waiting item at a time, stopping only when a presence
test actually applies (no `Waiting` left, or no `Room` left on that station) — at a
rate (`LoadRatePerMinute`) many orders of magnitude faster than any realistic
lambda/mu, so the whole load resolves in a negligible sliver of simulated time. That's
the standard way to approximate a GSPN "immediate" transition on top of a framework
that only has continuous-time, exponential ones. The real cost: each admitted item now
costs an extra discrete simulation event, so `KitchenStationSpec`'s Erlang B cross-check
uses a shorter horizon and fewer runs than `KitchenQueueSpec`'s — otherwise it takes
minutes instead of seconds.

The second lane's *start* transition reads (without consuming — present in both `cond`
and `eff`, netting to no change) the first lane's current `Bake` size as a
precondition. That read-arc, combined with the shared per-station `Room` counter, is
what makes "only open once the sibling has committed, and only for whatever room it
left" fall out of the structure, rather than needing a separate parameter for every
possible fill level the sibling could be sitting at.

Not yet done: wiring `KitchenStation` into `CapacityAdvisor`/`RateEstimation` (both still
operate on `KitchenQueue`); `KitchenStation` currently stands alone, proven by its own
`KitchenStationSpec`.

## KitchenPipeline, ServiceSimulation, CapacityAgent: tandem stages and a tuning agent

A single product's full journey through the kitchen is two stages in series, not one:
**prep** (serial, one item per worker -- a person flattening dough has no batching
benefit) and **cook** (the same batch-station automaton as `KitchenStation`, reused here
targeting pipeline-specific places instead of a hardcoded `Waiting`/`Capacity` pair).
Which stage comes first is fixed when the `Config` is built (`Route.PrepThenCook` for a
pizza; `Route.CookThenPrep` for something griddled, then assembled) -- one net models
one product/route, not a mix.

Admission here is deliberately *not* a concurrency cap the way `KitchenStation.Capacity`
is. `Admission` is a per-time-span quota: consumed when an order is admitted, never
refunded by a completion, and reset only by `resetAdmission` -- called by whatever drives
the simulation across span boundaries on its own fixed clock, never by a transition
inside the SPN. That split exists because a deterministic "every 15 minutes" event has
no honest representation among exponential, memoryless transitions; rather than fake it
(the way the loading automaton fakes an instantaneous decision), the one genuinely
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
(`adviseCapacity`, `fitRates`, `erlangBlockingProbability`) — see `demo-node.js` for a
plain `require()` against it, or `npm run analyze:scala` in `../package.json`.

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
