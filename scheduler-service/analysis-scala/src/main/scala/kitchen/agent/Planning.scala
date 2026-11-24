package kitchen.agent

import kitchen.model.{KitchenPipeline, SpnSimulator}
import CapacityAgent.DelayCaps

// Parameter objects shared by the simulation and the agents.

/** `count` back-to-back slots of `lengthMinutes` each. */
case class Slots(lengthMinutes: Double, count: Int):
  require(lengthMinutes > 0, "lengthMinutes must be positive")
  require(count > 0, "count must be positive")

case class KitchenModel(config: KitchenPipeline.Config, rates: KitchenPipeline.Rates):
  def spn: SpnSimulator[KitchenPipeline.Place] = SpnSimulator(KitchenPipeline.spn(config, rates))

/** A kitchen whose arrival rate changes from slot to slot; `simulators(k)` runs slot k. */
case class ProfiledKitchen(
    config: KitchenPipeline.Config,
    simulators: Vector[SpnSimulator[KitchenPipeline.Place]],
    slotLengthMinutes: Double,
):
  require(simulators.nonEmpty, "need at least one slot")
  def slots: Int = simulators.size

object ProfiledKitchen:

  def apply(kitchen: KitchenModel, lambdasPerMinute: Vector[Double], slotLengthMinutes: Double): ProfiledKitchen =
    ProfiledKitchen(
      kitchen.config,
      lambdasPerMinute.map(l => kitchen.copy(rates = kitchen.rates.withArrivals(l)).spn),
      slotLengthMinutes,
    )

  def flat(kitchen: KitchenModel, slots: Slots): ProfiledKitchen =
    ProfiledKitchen(kitchen.config, Vector.fill(slots.count)(kitchen.spn), slots.lengthMinutes)

/** A quota meets the goal when the probability of breaching either cap is at most `riskTolerance`. */
case class Goal(caps: DelayCaps, riskTolerance: Double):
  require(riskTolerance > 0 && riskTolerance < 1, "riskTolerance must be in (0, 1)")

/**
 * How a candidate quota is judged:
 *  - `epsilon`, `confidence`: the precision, which fixes the maximum `runs`.
 *  - `earlyRejection`: stop once the breaches already exceed the tolerance.
 *  - `safetyMargin`: accept only estimates at least `epsilon` below the tolerance.
 *  - `sequentialTest`: stop as soon as Wald's test is decisive.
 */
case class DecisionRule(
    epsilon: Double = 0.05,
    confidence: Double = 0.9,
    earlyRejection: Boolean = false,
    safetyMargin: Boolean = false,
    sequentialTest: Boolean = false,
):
  def runs: Int = Statistics.requiredRuns(epsilon, confidence)

  def acceptUpTo(riskTolerance: Double): Double =
    if safetyMargin then riskTolerance - epsilon else riskTolerance

  def rejectAbove(riskTolerance: Double): Option[Double] =
    Option.when(earlyRejection)(acceptUpTo(riskTolerance))

  def hypotheses(riskTolerance: Double): Option[SequentialTest.Hypotheses] =
    Option.when(sequentialTest)(CapacityAgent.sequentialHypotheses(riskTolerance, epsilon, confidence))

case class Assessment(slots: Slots, goal: Goal, rule: DecisionRule):
  def caps: DelayCaps = goal.caps
  def acceptUpTo: Double = rule.acceptUpTo(goal.riskTolerance)
