package kitchen.agent

import java.util.Random

/**
 * Finds the largest admission quota whose risk of breaching the delay caps is within
 * tolerance: "maximize throughput subject to a delay cap", since "minimize delay" is met
 * by admitting nobody. Each service is reduced to a breach indicator, because the
 * Hoeffding bounds in `Statistics` need a quantity in [0, 1] and delay minutes are not.
 */
object CapacityAgent:

  /**
   * A slot's orders are due `leadTimeMinutes` after the slot ends; a service breaches a
   * cap when its average or worst slot lateness exceeds it.
   */
  case class DelayCaps(averageDelayCapMinutes: Double, worstDelayCapMinutes: Double, leadTimeMinutes: Double = 0.0)

  /** `verdict`: the sequential test's decision, if it reached one. */
  case class CandidateEvaluation(
      admissionQuota: Int,
      averageDelayExceedsCapProbability: Double,
      worstDelayExceedsCapProbability: Double,
      runs: Int,
      verdict: Option[Boolean] = None,
  ):
    def meets(acceptUpTo: Double): Boolean =
      verdict.getOrElse(
        averageDelayExceedsCapProbability <= acceptUpTo && worstDelayExceedsCapProbability <= acceptUpTo,
      )

  /**
   * A quota breaching with probability `riskTolerance` is accepted with probability at most
   * `1 - confidence`; one breaching at `riskTolerance - 2 * epsilon` is rejected with at
   * most that probability.
   */
  def sequentialHypotheses(riskTolerance: Double, epsilon: Double, confidence: Double): SequentialTest.Hypotheses =
    SequentialTest.Hypotheses(riskTolerance - 2 * epsilon, riskTolerance, 1 - confidence, 1 - confidence)

  /**
   * Simulates up to `rule.runs` services and estimates how often each cap is breached.
   * Stops early when the sequential test decides, or, with `earlyRejection`, once the
   * breaches already exceed what the tolerance allows over all runs.
   */
  def evaluate(kitchen: KitchenModel, admissionQuota: Int, assessment: Assessment, rnd: Random): CandidateEvaluation =
    val Assessment(slots, goal, rule) = assessment
    val caps = goal.caps
    val runs = rule.runs
    val tests = rule.hypotheses(goal.riskTolerance).map(SequentialTest.CapPair(_))
    def verdict = tests.flatMap(_.verdict)
    val breachLimit = rule.rejectAbove(goal.riskTolerance).map(_ * runs)
    def rejected(breaches: Int) = breachLimit.exists(breaches > _)
    var averageBreaches = 0
    var worstBreaches = 0
    var done = 0
    while done < runs && !rejected(averageBreaches) && !rejected(worstBreaches) && verdict.isEmpty do
      val result = ServiceSimulation
        .simulateService(kitchen, admissionQuota, slots, rnd)
        .withLeadTime(caps.leadTimeMinutes)
      val averageBreach = result.averageDelayMinutes > caps.averageDelayCapMinutes
      val worstBreach = result.worstDelayMinutes > caps.worstDelayCapMinutes
      if averageBreach then averageBreaches += 1
      if worstBreach then worstBreaches += 1
      tests.foreach(_.observe(averageBreach, worstBreach))
      done += 1
    CandidateEvaluation(admissionQuota, averageBreaches.toDouble / done, worstBreaches.toDouble / done, done, verdict)

  /**
   * The largest quota in `quotas` meeting the goal. Scans from the top rather than
   * binary-searching: each evaluation is noisy, and one unlucky estimate would send a
   * binary search the wrong way. See `DecisionRule` for the options.
   */
  def recommendCapacity(
      kitchen: KitchenModel,
      quotas: Range,
      assessment: Assessment,
      rnd: Random,
  ): Option[CandidateEvaluation] =
    require(quotas.nonEmpty, "need at least one candidate quota")
    val acceptUpTo = assessment.acceptUpTo
    require(acceptUpTo > 0, "with safetyMargin, epsilon must be smaller than riskTolerance")
    quotas.reverseIterator
      .map(q => evaluate(kitchen, q, assessment, rnd))
      .find(_.meets(acceptUpTo))
