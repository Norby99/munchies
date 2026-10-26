package kitchen.agent

/** One historical order's kitchen-occupancy window (epoch milliseconds, as Double for ScalaJS-safe interop). */
case class HistoricalOrder(startedPrepAtEpochMs: Double, finishedPrepAtEpochMs: Double)

case class FittedRates(lambdaPerMinute: Double, muPerMinute: Double, sampleSize: Int)

/**
 * Fits KitchenQueue's lambda (arrival rate into InPrep) and mu (service rate) from a
 * sample of completed orders, using the maximum-likelihood estimator for an
 * exponential distribution: rate = 1 / sample mean (from 07-StochasticModelling, slide 10:
 * "rates are typically obtained by counting past occurrences").
 */
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
