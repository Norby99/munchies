package kitchen.agent

import kitchen.model.KitchenPipeline

/** Epoch milliseconds, as Double for JS interop. */
case class HistoricalOrder(startedPrepAtEpochMs: Double, finishedPrepAtEpochMs: Double)

case class FittedRates(lambdaPerMinute: Double, muPerMinute: Double, sampleSize: Int)

/** Maximum-likelihood rates from past orders: rate = 1 / sample mean. */
object RateEstimation:

  def fitRates(history: Seq[HistoricalOrder]): FittedRates =
    require(history.size >= 2, "Need at least 2 historical orders to fit rates")

    val starts = history.map(_.startedPrepAtEpochMs).sorted
    val spanMinutes = (starts.last - starts.head) / 60_000.0
    require(spanMinutes > 0, "Historical orders must span a positive amount of time")
    val lambdaPerMinute = (history.size - 1) / spanMinutes

    val durationsMinutes = history.map(o => (o.finishedPrepAtEpochMs - o.startedPrepAtEpochMs) / 60_000.0)
    require(
      durationsMinutes.forall(_ > 0),
      "finishedPrepAtEpochMs must be after startedPrepAtEpochMs for every order",
    )
    val meanDurationMinutes = durationsMinutes.sum / durationsMinutes.size

    FittedRates(lambdaPerMinute, 1 / meanDurationMinutes, history.size)

  def fitStages(samples: Seq[StageSample]): FittedStageRates =
    require(samples.size >= 2, "Need at least 2 samples to fit rates")
    val arrivals = samples.map(_.arrivedAtMinutes).sorted
    val spanMinutes = arrivals.last - arrivals.head
    require(spanMinutes > 0, "Samples must span a positive amount of time")

    def rateOf(stage: String, durations: Seq[Double]): Double =
      require(durations.forall(_ > 0), s"every $stage duration must be positive")
      durations.size / durations.sum

    FittedStageRates(
      lambdaPerMinute = (samples.size - 1) / spanMinutes,
      prepRatePerMinute = rateOf("prep", samples.map(_.prepMinutes)),
      loadingRatePerMinute = rateOf("loading", samples.map(_.loadingMinutes)),
      cookRatePerMinute = rateOf("cook", samples.map(_.cookMinutes)),
      sampleSize = samples.size,
    )

  /** `fitStages` over several logs, each with its own clock. */
  def fitStagesAcross(logs: Seq[Seq[StageSample]]): FittedStageRates =
    val (lambda, _) = arrivalRateAcross(logs)
    fitStages(logs.flatten).copy(lambdaPerMinute = lambda)

  /** Arrival rate (intervals over observed time, summed over the logs) and sample size. */
  def arrivalRateAcross(logs: Seq[Seq[StageSample]]): (Double, Int) =
    val usable = logs.filter(_.size >= 2)
    require(usable.nonEmpty, "Need at least one log with 2 samples to fit rates")
    val observedMinutes = usable.map { log =>
      val arrivals = log.map(_.arrivedAtMinutes)
      arrivals.max - arrivals.min
    }.sum
    require(observedMinutes > 0, "Logs must span a positive amount of time")
    val intervals = usable.map(_.size - 1).sum
    (intervals / observedMinutes, usable.map(_.size).sum)

/**
 * One logged order with its time in each stage. The log includes orders a full quota
 * turned away, otherwise the fitted demand would be capped by the quota.
 */
case class StageSample(arrivedAtMinutes: Double, prepMinutes: Double, loadingMinutes: Double, cookMinutes: Double)

case class FittedStageRates(
    lambdaPerMinute: Double,
    prepRatePerMinute: Double,
    loadingRatePerMinute: Double,
    cookRatePerMinute: Double,
    sampleSize: Int,
):
  def toRates: KitchenPipeline.Rates =
    KitchenPipeline.Rates(lambdaPerMinute, prepRatePerMinute, loadingRatePerMinute, cookRatePerMinute)

object FittedStageRates:
  def apply(rates: KitchenPipeline.Rates, sampleSize: Int): FittedStageRates =
    FittedStageRates(
      rates.lambdaPerMinute,
      rates.prepRatePerMinute,
      rates.loadingRatePerMinute,
      rates.cookRatePerMinute,
      sampleSize,
    )
