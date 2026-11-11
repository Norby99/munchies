package kitchen.agent

import java.util.Random
import kitchen.model.KitchenPipeline
import AutonomousCapacityAgent.StageTimes

/**
 * One factor on all declared stage times, chosen so that the model reproduces a past
 * service's mean clearing time. Clearing times cannot tell which stage was slow.
 */
object Calibration:

  case class ObservedService(lambdaPerMinute: Double, admissionQuota: Int, clearingMinutes: Double)

  /** Bisection in `[lower, upper]`, each factor judged on `runs` services. */
  case class Search(runs: Int = 60, lower: Double = 0.5, upper: Double = 3.0, iterations: Int = 7)

  /** Every factor is simulated with the same `seed`, so noise does not blur the comparison. */
  def slowdownFactor(
      config: KitchenPipeline.Config,
      declared: StageTimes,
      observed: ObservedService,
      slots: Slots,
      seed: Long,
      search: Search = Search(),
  ): Double =
    import search.*
    def clearing(s: Double): Double =
      val rnd = new Random(seed)
      val kitchen = KitchenModel(config, declared.rates(observed.lambdaPerMinute, slowdown = s))
      (1 to runs).map { _ =>
        ServiceSimulation.simulateService(kitchen, observed.admissionQuota, slots, rnd).averageDelayMinutes
      }.sum / runs

    val target = observed.clearingMinutes
    if clearing(lower) >= target then lower
    else if clearing(upper) <= target then upper
    else
      var (lo, hi) = (lower, upper)
      for _ <- 1 to iterations do
        val mid = (lo + hi) / 2
        if clearing(mid) < target then lo = mid else hi = mid
      (lo + hi) / 2
