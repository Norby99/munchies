package kitchen.agent

import java.util.Random
import kitchen.model.KitchenPipeline

/**
 * A model-based, utility-based agent: searches `admissionQuota` for the largest value
 * keeping a whole service's delay within caps -- "maximize throughput subject to a
 * delay cap", not "minimize delay" (trivially achieved by admitting nobody).
 *
 * Reports exceedance *probabilities*, not a confidence interval on raw delay minutes:
 * `Statistics.requiredRuns`/`epsilonFor` are Hoeffding-based, valid only for a quantity
 * bounded in [0,1] -- delay in minutes isn't, without an arbitrary rescaling. "Did this
 * service breach its cap" is a genuine Bernoulli indicator, so `evaluate` reduces each
 * candidate to that before reusing the existing confidence-bound machinery.
 */
object CapacityAgent:

  case class DelayCaps(averageDelayCapMinutes: Double, worstDelayCapMinutes: Double)

  case class CandidateEvaluation(
      admissionQuota: Int,
      averageDelayExceedsCapProbability: Double,
      worstDelayExceedsCapProbability: Double,
      runs: Int,
  )

  def evaluate(
      config: KitchenPipeline.Config,
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
      admissionQuota: Int,
      spanLengthMinutes: Double,
      spans: Int,
      caps: DelayCaps,
      rnd: Random,
      epsilon: Double = 0.05,
      confidence: Double = 0.9,
  ): CandidateEvaluation =
    val runs = Statistics.requiredRuns(epsilon, confidence)
    var averageBreaches = 0
    var worstBreaches = 0
    for _ <- 0 until runs do
      val result = ServiceSimulation.simulateService(
        config,
        lambdaPerMinute,
        prepRatePerMinute,
        loadingRatePerMinute,
        cookRatePerMinute,
        admissionQuota,
        spanLengthMinutes,
        spans,
        rnd,
      )
      if result.averageDelayMinutes > caps.averageDelayCapMinutes then averageBreaches += 1
      if result.worstDelayMinutes > caps.worstDelayCapMinutes then worstBreaches += 1
    CandidateEvaluation(admissionQuota, averageBreaches.toDouble / runs, worstBreaches.toDouble / runs, runs)

  /**
   * The largest `admissionQuota` in `[minQuota, maxQuota]` keeping both exceedance
   * probabilities at or below `riskTolerance`; scans top-down, stops at the first hit.
   *
   * Relies on one assumption: for a fixed kitchen, admitting more work per span can
   * only add contention, so delay is stochastically non-decreasing in `admissionQuota`.
   * A full scan over a binary search, despite that monotonicity, because each
   * evaluation is a noisy Monte Carlo estimate -- binary-searching a noisy signal can
   * take a wrong turn on one unlucky sample; a full scan judges every candidate
   * independently and doesn't have that failure mode.
   */
  def recommendCapacity(
      config: KitchenPipeline.Config,
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
      minQuota: Int,
      maxQuota: Int,
      spanLengthMinutes: Double,
      spans: Int,
      caps: DelayCaps,
      riskTolerance: Double,
      rnd: Random,
      epsilon: Double = 0.05,
      confidence: Double = 0.9,
  ): Option[CandidateEvaluation] =
    require(minQuota <= maxQuota, "minQuota must be <= maxQuota")
    (maxQuota to minQuota by -1).iterator
      .map(q =>
        evaluate(
          config,
          lambdaPerMinute,
          prepRatePerMinute,
          loadingRatePerMinute,
          cookRatePerMinute,
          q,
          spanLengthMinutes,
          spans,
          caps,
          rnd,
          epsilon,
          confidence,
        ),
      )
      .find(e => e.averageDelayExceedsCapProbability <= riskTolerance && e.worstDelayExceedsCapProbability <= riskTolerance)
