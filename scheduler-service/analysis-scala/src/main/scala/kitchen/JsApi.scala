package kitchen

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import java.util.Random
import u07.utils.Time
import kitchen.model.KitchenPipeline
import kitchen.agent.{CapacityAdvisor, CapacityAgent, HistoricalOrder, RateEstimation}

/**
 * The boundary scheduler-service (Node) actually calls. Kept deliberately thin:
 * everything above this file is plain, platform-agnostic Scala reusing the vendored
 * u06/u07/u08 course code — this is the only file that knows it is compiled to JS.
 */
@JSExportTopLevel("SchedulerAnalysis")
object JsApi:

  @JSExport
  def adviseCapacity(
      lambdaPerMinute: Double,
      muPerMinute: Double,
      candidateCapacity: Int,
      timeHorizonMinutes: Double,
      seed: Double,
      epsilon: Double,
      confidence: Double,
  ): js.Object =
    val advice = Time.timed:
      CapacityAdvisor.adviseCapacity(
        lambdaPerMinute,
        muPerMinute,
        candidateCapacity,
        timeHorizonMinutes,
        new Random(seed.toLong),
        epsilon,
        confidence,
      )
    js.Dynamic.literal(
      candidateCapacity = advice.candidateCapacity,
      lambdaPerMinute = advice.lambdaPerMinute,
      muPerMinute = advice.muPerMinute,
      transientBlockingProbability = advice.transientBlockingProbability,
      epsilon = advice.epsilon,
      confidence = advice.confidence,
      runs = advice.runs,
      steadyStateBlockingProbability = advice.steadyStateBlockingProbability,
    )

  @JSExport
  def erlangBlockingProbability(offeredLoad: Double, capacity: Int): Double =
    CapacityAdvisor.erlangBlockingProbability(offeredLoad, capacity)

  @JSExport
  def recommendCapacity(
      route: String,
      prepWorkers: Int,
      cookStationCapacities: js.Array[Int],
      orderSizeProbabilities: js.Dictionary[Double],
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
      minQuota: Int,
      maxQuota: Int,
      spanLengthMinutes: Double,
      spans: Int,
      averageDelayCapMinutes: Double,
      worstDelayCapMinutes: Double,
      riskTolerance: Double,
      seed: Double,
      epsilon: Double,
      confidence: Double,
  ): js.Object =
    val parsedRoute = route match
      case "PrepThenCook" => KitchenPipeline.Route.PrepThenCook
      case "CookThenPrep" => KitchenPipeline.Route.CookThenPrep
      case other          => throw IllegalArgumentException(s"unknown route: $other (expected PrepThenCook or CookThenPrep)")
    val config = KitchenPipeline.Config(
      parsedRoute,
      prepWorkers,
      cookStationCapacities.toList,
      orderSizeProbabilities.toMap.map { case (size, probability) => size.toInt -> probability },
    )
    val caps = CapacityAgent.DelayCaps(averageDelayCapMinutes, worstDelayCapMinutes)
    val recommendation = Time.timed:
      CapacityAgent.recommendCapacity(
        config,
        lambdaPerMinute,
        prepRatePerMinute,
        loadingRatePerMinute,
        cookRatePerMinute,
        minQuota,
        maxQuota,
        spanLengthMinutes,
        spans,
        caps,
        riskTolerance,
        new Random(seed.toLong),
        epsilon,
        confidence,
      )
    recommendation match
      case Some(evaluation) =>
        js.Dynamic.literal(
          found = true,
          admissionQuota = evaluation.admissionQuota,
          averageDelayExceedsCapProbability = evaluation.averageDelayExceedsCapProbability,
          worstDelayExceedsCapProbability = evaluation.worstDelayExceedsCapProbability,
          runs = evaluation.runs,
        )
      case None =>
        js.Dynamic.literal(found = false)

  @JSExport
  def fitRates(history: js.Array[js.Dynamic]): js.Object =
    val parsed = history.toSeq.map { h =>
      HistoricalOrder(
        h.startedPrepAtEpochMs.asInstanceOf[Double],
        h.finishedPrepAtEpochMs.asInstanceOf[Double],
      )
    }
    val fitted = RateEstimation.fitRates(parsed)
    js.Dynamic.literal(
      lambdaPerMinute = fitted.lambdaPerMinute,
      muPerMinute = fitted.muPerMinute,
      sampleSize = fitted.sampleSize,
    )
