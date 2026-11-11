package kitchen.agent

/** Hoeffding's bound, as in PRISM: N = ceil(ln(2/delta) / (2 epsilon^2)), delta = 1 - confidence. */
object Statistics:

  def requiredRuns(epsilon: Double, confidence: Double): Int =
    val delta = 1 - confidence
    math.ceil(math.log(2 / delta) / (2 * epsilon * epsilon)).toInt

  def epsilonFor(runs: Int, confidence: Double): Double =
    val delta = 1 - confidence
    math.sqrt(math.log(2 / delta) / (2 * runs))

/**
 * Wald's sequential probability ratio test, as used by statistical model checkers: "p <=
 * goodP" against "p >= badP", with error probabilities `alpha` and `beta`. It may take
 * many observations near the bounds, so callers cap the runs.
 */
object SequentialTest:

  case class Hypotheses(goodP: Double, badP: Double, alpha: Double, beta: Double):
    require(goodP > 0 && goodP < badP && badP < 1, "need 0 < goodP < badP < 1")
    require(alpha > 0 && alpha < 0.5 && beta > 0 && beta < 0.5, "alpha and beta must be in (0, 0.5)")
    private[SequentialTest] val acceptGood = math.log((1 - beta) / alpha)
    private[SequentialTest] val acceptBad = math.log(beta / (1 - alpha))
    private[SequentialTest] val onFailure = math.log(goodP / badP)
    private[SequentialTest] val onSuccess = math.log((1 - goodP) / (1 - badP))

  /** `observe(true)` for a failure, e.g. a breach. */
  final class Test(h: Hypotheses):
    private var logRatio = 0.0
    def observe(failed: Boolean): Unit = logRatio += (if failed then h.onFailure else h.onSuccess)
    def verdict: Option[Boolean] =
      if logRatio >= h.acceptGood then Some(true)
      else if logRatio <= h.acceptBad then Some(false)
      else None

  /** One test per cap: good once both are good, bad as soon as either is bad. */
  final class CapPair(h: Hypotheses):
    private val average = Test(h)
    private val worst = Test(h)
    def observe(averageBreach: Boolean, worstBreach: Boolean): Unit =
      average.observe(averageBreach)
      worst.observe(worstBreach)
    def verdict: Option[Boolean] =
      (average.verdict, worst.verdict) match
        case (Some(false), _) | (_, Some(false)) => Some(false)
        case (Some(true), Some(true))            => Some(true)
        case _                                    => None
