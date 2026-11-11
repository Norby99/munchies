# Agent-Based Architecture: the Capacity Agent

> Consider the specification and design of the autonomous software system controlling a drone in
> the "Shipping on the Air" case study, applying an agent-oriented approach: identify a possible
> model/architecture, implement a simple prototype to show your idea.

**Scope.** "Shipping on the Air" is not part of our project. We therefore applied the same agent-oriented approach to
an autonomous system from our own domain: an agent that sets how many orders a restaurant accepts per time slot. It
is built on the kitchen-capacity model we developed for ASMD (`scheduler-service/analysis-scala`, see the
[ASMD report](../../asmd/index.md)), so the effort goes into the agent rather than into a second case study. The end
of [Architecture](#architecture) relates the result back to the drone.

## Specification

**The problem.** A restaurant accepts orders in fixed time slots (15 minutes here) and caps how many items each slot
may take on (`CapacityPolicy.maxCapacityUnits`). The orders of a slot are due 15 minutes after that slot ends. If the
restaurant admits too much, the kitchen falls behind and orders are late. If it admits too little, it turns away
customers it could have served. The right quota is not a constant: it depends on demand, on who is on shift and on
the state of the equipment. The agent's job is to keep choosing the quota as these conditions change.

**A pizzeria as the reference kitchen.** The model is not tied to one kind of restaurant: an order is a number of
items, and each item is prepared by a worker and cooked in a station with a fixed number of places (`KitchenPipeline`
also covers the reverse order, cooked then assembled, as for a piadina or a burger). To keep the prototype concrete,
the rest of this chapter assumes a pizzeria: an item is a pizza, the workers are the pizza makers, who also load the
oven, and the cooking station is an oven with 7 places.

The course defines a rational agent by four elements:

| Element | For the capacity agent |
|---|---|
| Performance measure | Items admitted per slot, as long as the probability that a service breaches the manager's delay caps (average lateness, worst slot's lateness) stays within the manager's risk tolerance |
| Prior knowledge | The structure of the kitchen as a Stochastic Petri Net (`KitchenPipeline`: prep, loading, oven), the order-size distribution, and the bounds `[minQuota, maxQuota]` of its authority |
| Actions | Set the quota to an integer in `[minQuota, maxQuota]`, once per period |
| Percepts | From the last service: the order log (arrival times) and how late each slot finished.<br>For the coming service: the rota (the staff schedule), the oven report and the stage times the staff declare. |

**The environment** is *inaccessible* (the agent never sees the kitchen's true rates, only logs and reports),
*nondeterministic* (arrivals and every stage take random time), *dynamic* (staff, equipment and demand change; the
prototype changes them only between periods), with *discrete actions* and *continuous percepts*. There is a single
agent.

## Architecture

Following the course's taxonomy, the agent is **model-based and utility-based**:

- **Internal state and model.** The agent keeps recent order logs, the last reported staffing, and the beliefs formed
  from them (arrival rate, stage times). Its model of how the world works is the `KitchenPipeline` SPN with these
  beliefs as parameters.
- **Utility, not just a goal.** "Minimize delay" would be met by admitting nobody. The utility is therefore the
  throughput (the quota), and the delay goal becomes a constraint: the risk of breaching a cap.
- **Decision procedure.** For each candidate quota, from the largest down, the agent simulates services on its model
  (Gillespie's algorithm) and estimates the probability of each breach. It picks the largest quota whose risk is
  within tolerance. This is a constrained form of the rational utility-based agent: the manager states the trade-off
  as a risk tolerance rather than as an exchange rate between minutes of delay and orders.
- **Execution cycle.** Perceive, revise beliefs, decide, act: once per period (here, before each Friday).

**Relation to the other architectures of the course.**

- *BDI.* The agent has beliefs (rates, staffing), one desire (the delay goal) and an intention (the quota for the
  period). It lacks what makes BDI worthwhile: several conflicting goals and a plan library to choose from. Its
  deliberation is an optimization over one integer.
- *Agents and Artifacts.* The `KitchenEnvironment` interface is a boundary artifact representing the kitchen: one
  observable state (`perceive`) and one operation (`applyQuota`). It lets the same agent run against a simulated
  kitchen today and against `restaurant-service` later.
- *Reactive and hybrid.* Re-tuning a quota has no real-time urgency, so we chose neither. A drone controller would
  need a hybrid, layered design (e.g. InteRRaP): a reactive layer for collision avoidance and a deliberative layer for
  route planning. Our agent corresponds to that deliberative layer only.

## Capabilities of an autonomous system

Each of the course's four capabilities maps onto one part of the agent's cycle (`AutonomousCapacityAgent.Agent.step()`):

| Capability | How the agent realizes it | Where |
|---|---|---|
| **Sensing and perception** | Fits the arrival rate from the last 2 order logs; takes the stage times from the staff's declaration, stretched if the last service ran slower than declared | `RateEstimation`, `Calibration` |
| **Decision-making** | Simulates candidate quotas on the model built from its beliefs and picks the largest one within the risk tolerance | `CapacityAgent` |
| **Actuation** | In `Auto` mode applies the quota within the manager's bounds; in `Advise` mode only recommends it. Also advises on the rota, which only the manager can change | `KitchenEnvironment.applyQuota`, `StaffingAdvisor` |
| **Adaptability** | Perception runs every period, so a change moves the decision. When no quota meets the goal, or the data is insufficient, it falls back to a safe choice and raises an alert | `Agent.revise`, `Agent.decide` |

**Bounded autonomy.** The manager sets the goal (delay caps, risk tolerance) and the bounds of the agent's authority
(minimum and maximum quota). Within them, `Auto` mode acts alone; `Advise` mode keeps a human in the loop. The
decision procedure is the same in both modes.

## The prototype

The agent lives in `scheduler-service/analysis-scala`, on top of the ASMD model:

- `AutonomousCapacityAgent.scala`: the perceive, decide, act loop, the internal state, the two modes and the fallbacks.
- `CapacityAgent.scala`: the decision function.
- `RateEstimation.scala` and `Calibration.scala`: perception.
- `StaffingAdvisor.scala`: the rota advice.
- `KitchenPipeline.scala`, `ServiceSimulation.scala`, `SpnSimulator.scala`: the model and its simulation.
- `SimulatedKitchen.scala`: the environment used for the demo.

It is plain Scala compiled to JavaScript with Scala.js. `demo-node.js` runs the scenario below end to end
(`sbt fastLinkJS && node demo-node.js`), and `AutonomousCapacityAgentSpec` covers both modes, calibration, the rota
advice, adaptation and the fallbacks. The results depend only on the seed, so the demo reproduces the table below
exactly.

### Perception: what a real kitchen can measure

A simulated kitchen could log how long each order spent in every stage; a real one cannot, since nobody stamps each
pizza in and out of the oven during a busy service. What Munchies does record is each order's **arrival** and its
**lifecycle** (`order-service` stores `PENDING -> PREPARING -> READY -> COMPLETED`), so it knows when each slot's last
order was ready. The stage times are better known by the staff. The agent is built on that division. Before each
Friday it:

1. **Perceives.** It receives last Friday's order log, with the arrival time of every order (including those a full
   slot turned away, otherwise the demand could never appear larger than the quota), and how late each slot finished.
   From the staff it receives the coming Friday's rota (how many pizza makers), the oven report (places and state)
   and the declared stage times (prep, loading and bake minutes per pizza).
2. **Fits the demand.** It estimates the arrival rate from the last 2 Fridays, as the number of gaps between
   arrivals over the minutes they span (the maximum-likelihood estimate for Poisson arrivals). Two Fridays smooth the
   noise of one without hiding a change of season for long.
3. **Checks the declared times.** It re-simulates last Friday with that Friday's actual demand, rota, declared times
   and quota, and searches for the single factor on all stage times that reproduces how late the slots really
   finished. If the times must be stretched by 5% or more, it plans the coming Friday with the stretched times and
   advises the manager to check them. It acts only on slowdowns, since a quota raised by mistake costs late orders
   while one lowered by mistake costs only pizzas, and it drops the stretch as soon as the staff declare new times.
4. **Decides and acts.** It builds the kitchen model from its beliefs (the rota's makers, the oven's places, the
   declared or stretched times, the fitted demand) and tries the quotas from 24 down to 8. For each it simulates
   Fridays and checks whether the delay caps are breached in at most 20% of them. The first quota that passes is
   applied to order intake; if none does, the agent applies 8 and raises an alert.
5. **Advises on the rota.** From the fitted demand it computes the quota that would serve the whole demand in 80% of
   slots, and finds the fewest pizza makers (1 to 4) with which that quota meets the goal. "None" means that even 4
   makers are not enough, so the limit is the oven. The advice is marked unreliable while a slowdown is in force,
   since a slow oven and a slow maker look the same in the clearing times. Only the manager changes the rota.

Each candidate quota is judged with Wald's sequential test, as statistical model checkers such as PRISM do. Asking
whether the breach rate is below or above exactly 20% would never settle for a quota near the limit, so the test
decides between "at most 10%" (accept) and "at least 20%" (reject), with at most 10% error either way. A quota that
breaks the goal is thus rarely accepted, while one just within it may be turned down, which costs a few pizzas rather
than late orders. The test simulates one service at a time and stops as soon as the evidence is decisive: a clearly
safe quota passes after about 20 services, a clearly unsafe one fails after 4 breaches. Each cap has its own test, and
the quota passes only when both accept. Undecided quotas stop at 600 services (Hoeffding's bound for ±5% at 90%
confidence) and pass only if their estimated breach rate is at most 15%.

### Scenario: a pizzeria's Friday evenings

The parameters come from the experience of one of us, who worked in a pizzeria, and from public order data:

| Parameter | Value |
|---|---|
| Service | 18:30-21:30, 12 slots of 15 minutes |
| Demand | 5.5 orders per slot, 2.64 pizzas per order on average (sizes shaped on the [Maven Analytics "Pizza Place Sales"](https://mavenanalytics.io/project/2498) dataset) |
| Kitchen | 2 pizza makers (about 45 s per pizza), one oven with 7 places (about 5 minutes per bake) |
| Goal | average lateness at most 3 minutes, worst slot at most 8 minutes, each breached in at most 20% of services |
| Full slot | the order is offered a later slot, as `scheduler-service` does: 90% of customers accept 15 minutes later, 50% accept 30 minutes later, the rest are lost |
| Authority | quota between 8 and 24 pizzas per slot; the manager's static setting is 20 |

`SimulatedKitchen` holds the kitchen's true conditions, which change from week to week and are never shown to the
agent. Each week is also run under the static quota with the same random numbers, so the two differ only by the
quota:

- Week 0: a normal Friday.
- Week 1: the topper is absent (announced on the rota).
- Week 2: a normal Friday.
- Weeks 3-5: an oven fault makes bakes last 7 minutes; it is reported, but the staff declare 6.
- Week 6: the oven is repaired (reported).
- Weeks 7-8: a slower new pizza maker; nobody updates the declared prep time.
- Weeks 9-10: a busier season (7 orders per slot), which no report announces.

### Results

Breaches are of the worst-slot cap, out of 100 simulated services; pizzas lost are per evening, out of about 174
asked for (222 in the busier season).

| Week | True conditions | Slowdown measured | Agent quota | Agent breaches | Agent pizzas lost | Static breaches | Static pizzas lost | Rota advice (makers) |
|---|---|---|---|---|---|---|---|---|
| 0 | Normal Friday | - | 20 | 6 | 4 | 6 | 4 | 2 |
| 1 | Topper absent | - | 11 | 8 | 50 | 98 | 5 | 3 |
| 2 | Normal Friday | - | 17 | 2 | 12 | 11 | 4 | 3 |
| 3 | Oven fault | - | 16 | 53 | 16 | 76 | 4 | none |
| 4 | Oven fault | x1.10 | 14 | 12 | 22 | 78 | 5 | none |
| 5 | Oven fault | x1.19 | 13 | 0 | 31 | 75 | 5 | none |
| 6 | Oven repaired | - | 24 | 29 | 2 | 18 | 5 | 2 |
| 7 | New pizza maker | - | 19 | 52 | 6 | 61 | 5 | 3 |
| 8 | New pizza maker | x1.17 | 14 | 8 | 24 | 58 | 4 | none |
| 9 | Busier season | x1.15 | 15 | 0 | 51 | 54 | 16 | none |
| 10 | Busier season | - | 17 | 10 | 34 | 56 | 15 | 4 |

"none" means that no number of pizza makers up to 4 reaches the target: something else, the oven, limits the service.

- **On a normal Friday** the agent chooses the static 20, which holds.
- **Announced changes are handled before they hurt.** With the topper absent the static quota fails in 98 services of
  100; the agent reads the rota, cuts the quota to 11 and stays on time. The rota advice gives the better fix: a third
  pizza maker.
- **Unannounced changes are corrected one week later.** The first Friday of the oven fault and of the new pizza maker
  breaches the cap in about half the services, since nothing tells the agent. On the next Friday calibration measures
  the kitchen 10-17% slower than declared, the agent lowers the quota and returns within the tolerance.
- **Moved orders come back as load.** In the busier season the static 20 fails in more than half the services,
  because orders moved out of full slots crowd the later ones. The agent stays on time.
- **The goal protects lateness, not customers.** Staying on time costs about 50 pizzas per evening (weeks 1 and 9).
  That is what a goal that limits only lateness asks for.
- **Estimates are noisy.** In week 6, by chance, the two Fridays of logs underestimate demand (0.31 orders per minute
  instead of 0.37); the agent chooses 24 and breaches the cap in 29 services, above the tolerance. Nothing in its
  percepts can reveal such an error before the service.

### Two variants

Each variant changes one choice of the agent above and keeps everything else:

- **Stage-log agent:** learns every stage time from logs of how long each pizza spent in each stage, instead of
  using the declared times corrected by calibration. Only a simulated kitchen can produce such logs.
- **Peak-profile agent:** fits one arrival rate per slot from 8 past Fridays, instead of one rate for the whole
  evening from 2.

#### Results
**Stage-log agent.** `node demo-node.js logs` runs it on the same 11 weeks. It does not do better: it
matches calibration on the oven fault, and does worse with the new pizza maker (52 breaches in week 8 instead of 8),
because its fit still mixes in the Friday before the change, while calibration looks only at the last one. Learning
only from what a real system can measure costs nothing here.

**Peak-profile agent.** `demo-peak.js` is a proof of concept, not part of the weekly loop: it plans a Friday
with the same 66 orders, peaking around 20:00. On the evening's average rate the agent chooses 22 pizzas per slot,
which on the peaked Friday breach the cap in 46% of services. With per-slot rates it chooses 18 and stays within the
tolerance (13%), losing 8 more pizzas per evening. A different quota for each slot gained nothing on top of that.

## Limitations

- **Simulated environment.** The demo runs against `SimulatedKitchen`. A real kitchen has one service per Friday, a
  noisier signal than the demo's 100 simulated services, so a deployed agent would pool several Fridays and react
  more slowly.
- **Simplified scenario.** Orders arrive at a constant rate (see the peak variant), the delivery, takeaway and dine-in
  channels share one due time, and the order sizes are not re-estimated.
- **Parameter uncertainty.** The agent plans as if its estimated rates were exact (week 6 shows the cost). Planning
  against a pessimistic bound would protect the goal at the cost of lower quotas.
- **Lost customers are not in the goal.** A goal that also weighs lost demand would let the manager state that
  trade-off.
- **A minimal learning element.** Calibration compares the service with the model and corrects one factor, measured
  afresh each week. It does not remember, and it cannot tell which stage is slow: if the slow pizza maker is not on
  the next rota, the agent still plans with the stretched times for one week (a quota slightly too low).
- **No write-back and no reactive layer.** Nothing writes the quota to `restaurant-service` yet (that is an adapter
  behind `KitchenEnvironment`, not a change to the agent), and the prototype covers only the deliberative half of
  what a drone would need.
