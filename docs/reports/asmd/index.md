# Advanced Software Modeling and Development

The assignment for this course is `scheduler-service/analysis-scala`: an offline capacity-planning
tool for `scheduler-service`, built on the course's own Scala implementation of Continuous-Time
Markov Chains (CTMC) and Stochastic Petri Nets (SPN), vendored in from `asmd-public-models`
rather than re-derived.

## The problem

`scheduler-service` needs to answer a planning question nobody wants to guess at: given how fast
orders actually arrive and how fast a kitchen actually works, how many orders can a time slot
safely accept without customers waiting too long? That number is `CapacityPolicy.maxCapacityUnits`
on the restaurant side. This module is the offline tool that answers it against historical order
data, rather than a value picked by hand.

## Approach: stochastic simulation over a closed-form formula

Rather than deriving a bespoke queueing formula for every kitchen shape, the kitchen is modeled as
a **Stochastic Petri Net**: a set of places (counters — how many orders are waiting, how many
ovens are busy) and transitions (events — an order arrives, a bake finishes), each transition
firing at an exponentially distributed rate. Running that forward with **Gillespie's algorithm**
(`u07.modeling.CTMCSimulation.newSimulationTrace`) produces one possible timeline of a service;
running it many times and averaging gives real statistics — how often a given capacity would
reject an order, how much customers would wait — without needing a closed-form solution for every
model shape, which stops existing the moment the model gets as rich as a tandem prep/cook
pipeline with orders of several items.

## The models, from simplest to richest

- **`KitchenQueue`** — the textbook `M/M/c/c` ("Erlang loss") queue: a bounded counter of free
  slots feeding a busy counter, exactly what happens today when `scheduler-service` rejects an
  order because a slot is full, expressed as rates instead of a fixed headcount.
- **`KitchenPipeline`** — generalizes it to two stages in series (prep: serial, one item per
  worker; cook: a station of independent places, such as the 7 places of a pizza oven), with a
  short loading step in between, chained in whichever order a product needs. Orders carry
  several items at once, and a bake is Erlang-distributed (several exponential phases, so the
  same mean with a much smaller spread), the standard way to approximate a near-fixed duration
  within a continuous-time Markov chain. Admission changes from a concurrency cap to a
  **per-time-span quota** that resets on a fixed clock rather than being refunded on
  completion — the rule that actually matches "accept at most N items per 15-minute slot."
  Optionally, an order that does not fit is offered a later slot instead of being lost, as the
  scheduler does: each outcome (moved one slot, moved two, lost) is a transition whose rate
  depends on the remaining quota, since "fewer than n tokens" is not expressible as an
  inhibitor arc.

## Supporting modules

- **`SpnDsl`** — `cond ~ rate ~> eff ^ inh`, a small builder syntax for the SPN transitions above,
  parallel to the vendored course code's own Petri Net DSL (`~~>`/`^^^`) but with a rate slot.
- **`SpnSimulator`** — Gillespie's direct method for an SPN, like the vendored `SPN.toCTMC` +
  `CTMCSimulation`, but building only the successor marking of the transition that fires instead
  of every enabled one at every step: about 20 times faster on a full service, and checked against
  the vendored simulator.
- **`ServiceSimulation`** — runs a `KitchenPipeline` across a whole service made of many
  consecutive time slots (a late slot's backlog is allowed to spill into the next one, as in a
  real kitchen) and measures, per slot, how late its orders cleared relative to that slot's own
  fixed deadline.
- **`CapacityAgent`** — searches for the largest per-slot admission quota that keeps both average
  and worst-slot delay under a chosen cap: "maximize throughput subject to a delay cap," not
  "minimize delay" (trivially solved by admitting nobody). Framed explicitly against an agent
  architecture taxonomy for the SAAP course's own agent-oriented-architecture assignment — see the
  [SAAP report](../saap/index.md).
- **`RateEstimation`**, **`Statistics`**, **`CapacityAdvisor`** — fitting real arrival/service
  rates from historical order timestamps; the PRISM-style confidence-bound math (`requiredRuns`,
  `epsilonFor`) for how many simulation runs are enough; and a closed-form Erlang-B cross-check
  used only to verify the simulator agrees with known queueing theory on the simplest model.
- **`JsApi`** — the one file that knows this is compiled to JavaScript. It exports a handful of
  plain functions (`adviseCapacity`, `recommendCapacity`, `erlangBlockingProbability`,
  `fitRates`) that are the entire surface `scheduler-service` ever touches.

## Integration with `scheduler-service`

The module compiles via ScalaJS to a CommonJS module and is consumed as an ordinary local npm
dependency (`"scheduler-analysis": "file:analysis-scala"`), with no JVM at runtime. `demo-node.js`
proves the whole chain end to end from plain Node: a synthetic order history, rates fitted from
it, and every exported function called and printed.

## Testing

Every model is checked against something external to itself: `KitchenQueue` against the
closed-form Erlang-B formula, `KitchenPipeline`'s mechanics against hand-constructed markings and
its Erlang bake against the mean and spread it should have, `SpnSimulator` against the vendored
simulator, `CapacityAgent` against both a deliberately comfortable and a deliberately overloaded
scenario. 113 tests across the suite.
