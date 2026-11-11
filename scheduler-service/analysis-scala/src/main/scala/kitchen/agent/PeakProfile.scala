package kitchen.agent

import java.util.Random
import CapacityAgent.DelayCaps

/**
 * Proof of concept for a service whose demand peaks: fit one arrival rate per slot from
 * past services, then plan one quota on that profile (`bestFlatQuota`) or one quota per
 * slot (`shapeQuotas`).
 */
object PeakProfile:

  case class PlanEvaluation(
      quotas: Vector[Int],
      averageDelayExceedsCapProbability: Double,
      worstDelayExceedsCapProbability: Double,
      meanAverageDelayMinutes: Double,
      meanWorstDelayMinutes: Double,
      meanAdmittedItems: Double,
      runs: Int,
      meanMovedItems: Double = 0.0,
      meanLostItems: Double = 0.0,
      verdict: Option[Boolean] = None,
  ):
    def meets(acceptUpTo: Double): Boolean =
      verdict.getOrElse(
        averageDelayExceedsCapProbability <= acceptUpTo && worstDelayExceedsCapProbability <= acceptUpTo,
      )

  /** One service's arrival times, at a constant rate within each slot. */
  def sampleArrivals(lambdasPerMinute: Vector[Double], spanLengthMinutes: Double, rnd: Random): Vector[Double] =
    lambdasPerMinute.zipWithIndex.flatMap { case (lambda, slot) =>
      val start = slot * spanLengthMinutes
      Iterator
        .iterate(-math.log(1.0 - rnd.nextDouble()) / lambda)(_ - math.log(1.0 - rnd.nextDouble()) / lambda)
        .takeWhile(_ <= spanLengthMinutes)
        .map(start + _)
        .toVector
    }

  /** Weights 1/4, 1/2, 1/4 with the neighbours, so that a slot busy by chance does not look like the peak. */
  def smooth(rates: Vector[Double]): Vector[Double] =
    rates.indices.toVector.map { k =>
      val weighted = Seq((k - 1, 0.25), (k, 0.5), (k + 1, 0.25)).filter((i, _) => rates.indices.contains(i))
      weighted.map((i, w) => rates(i) * w).sum / weighted.map(_._2).sum
    }

  /** Each slot's arrival rate over all `services`. */
  def fitProfile(services: Seq[Seq[Double]], spanLengthMinutes: Double, spans: Int): Vector[Double] =
    require(services.nonEmpty, "need at least one service")
    val counts = services.flatten
      .map(t => math.min(spans - 1, (t / spanLengthMinutes).toInt))
      .groupMapReduce(identity)(_ => 1)(_ + _)
    Vector.tabulate(spans)(k => counts.getOrElse(k, 0) / (services.size * spanLengthMinutes))

  /** As `CapacityAgent.evaluate`, for a plan with one quota per slot. */
  def evaluatePlan(
      kitchen: ProfiledKitchen,
      quotas: Vector[Int],
      goal: Goal,
      rule: DecisionRule,
      rnd: Random,
  ): PlanEvaluation =
    runPlan(
      kitchen,
      quotas,
      goal.caps,
      rule.runs,
      rnd,
      rule.rejectAbove(goal.riskTolerance),
      rule.hypotheses(goal.riskTolerance),
    )

  /** Exactly `runs` services, without judging. */
  def measurePlan(kitchen: ProfiledKitchen, quotas: Vector[Int], caps: DelayCaps, runs: Int, rnd: Random): PlanEvaluation =
    runPlan(kitchen, quotas, caps, runs, rnd, rejectAbove = None, sequential = None)

  private def runPlan(
      kitchen: ProfiledKitchen,
      quotas: Vector[Int],
      caps: DelayCaps,
      runs: Int,
      rnd: Random,
      rejectAbove: Option[Double],
      sequential: Option[SequentialTest.Hypotheses],
  ): PlanEvaluation =
    val breachLimit = rejectAbove.map(_ * runs)
    val tests = sequential.map(SequentialTest.CapPair(_))
    def verdict = tests.flatMap(_.verdict)
    var averageBreaches, worstBreaches, done = 0
    var averageSum, worstSum, admittedSum, movedSum, lostSum = 0.0
    while done < runs && !breachLimit.exists(l => averageBreaches > l || worstBreaches > l) && verdict.isEmpty do
      val r = ServiceSimulation
        .simulateProfiledService(kitchen, quotas, rnd)
        .withLeadTime(caps.leadTimeMinutes)
      val averageBreach = r.averageDelayMinutes > caps.averageDelayCapMinutes
      val worstBreach = r.worstDelayMinutes > caps.worstDelayCapMinutes
      if averageBreach then averageBreaches += 1
      if worstBreach then worstBreaches += 1
      tests.foreach(_.observe(averageBreach, worstBreach))
      averageSum += r.averageDelayMinutes
      worstSum += r.worstDelayMinutes
      admittedSum += r.admittedItems
      movedSum += r.movedOneSlotItems + r.movedTwoSlotsItems
      lostSum += r.lostItems
      done += 1
    PlanEvaluation(
      quotas,
      averageBreaches.toDouble / done,
      worstBreaches.toDouble / done,
      averageSum / done,
      worstSum / done,
      admittedSum / done,
      done,
      movedSum / done,
      lostSum / done,
      verdict,
    )

  /** The largest single quota meeting the goal. */
  def bestFlatQuota(
      kitchen: ProfiledKitchen,
      quotas: Range,
      goal: Goal,
      rule: DecisionRule,
      rnd: Random,
  ): Option[PlanEvaluation] =
    val acceptUpTo = rule.acceptUpTo(goal.riskTolerance)
    quotas.reverseIterator
      .map(q => evaluatePlan(kitchen, Vector.fill(kitchen.slots)(q), goal, rule, rnd))
      .find(_.meets(acceptUpTo))

  /** `searched`: the plan the ascent reached, kept only if `passedCheck`. */
  case class Shaped(plan: PlanEvaluation, searched: Vector[Int], passedCheck: Boolean)

  /**
   * Greedy ascent from `start`: raise one slot at a time by `step`, keeping raises that
   * still meet the goal. Some raises pass by chance, so the result is checked again on
   * fresh runs and dropped for `start` if it fails.
   */
  def shapeQuotas(
      kitchen: ProfiledKitchen,
      start: PlanEvaluation,
      maxQuota: Int,
      step: Int,
      goal: Goal,
      rule: DecisionRule,
      rnd: Random,
      passes: Int = 3,
  ): Shaped =
    val acceptUpTo = rule.acceptUpTo(goal.riskTolerance)
    var best = start
    var pass = 0
    var improved = true
    while improved && pass < passes do
      improved = false
      for slot <- best.quotas.indices if best.quotas(slot) < maxQuota do
        val candidate = best.quotas.updated(slot, math.min(maxQuota, best.quotas(slot) + step))
        val e = evaluatePlan(kitchen, candidate, goal, rule, rnd)
        if e.meets(acceptUpTo) then
          best = e
          improved = true
      pass += 1
    if best eq start then Shaped(start, start.quotas, passedCheck = true)
    else
      val check = evaluatePlan(kitchen, best.quotas, goal, rule.copy(earlyRejection = false), rnd)
      if check.meets(acceptUpTo) then Shaped(check, best.quotas, passedCheck = true)
      else Shaped(start, best.quotas, passedCheck = false)
