package kitchen.agent

import java.util.Random
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import kitchen.model.KitchenPipeline
import KitchenPipeline.{Admission, Admitted, Exit}

/**
 * Drives a KitchenPipeline SPN across a whole service of fixed-length admission spans
 * (e.g. 12 back-to-back 15-minute windows = a 3-hour turn), reopening the quota on the
 * nominal clock at each boundary, then measures how late each span's cohort cleared
 * relative to its own fixed deadline (`spanLengthMinutes * k`, never shifted by how
 * late earlier spans ran -- backlog is never artificially drained between spans).
 *
 * Delay is per span ("did cohort k clear by its deadline"), not per item -- an
 * order-dependent question a pure sum can't answer. Since every item is fungible (no
 * token coloring in KitchenPipeline), the only sensible attribution is FIFO at the
 * whole-pipeline level: the k-th unit to exit, chronologically, belongs to whichever
 * cohort the running admitted count had reached when it was admitted. `perSpanDelays`
 * walks the cumulative Exit-count history for the first point crossing each cohort's
 * admitted threshold.
 */
object ServiceSimulation:

  case class ServiceResult(
      perSpanDelayMinutes: Vector[Double],
      totalDelayMinutes: Double,
      averageDelayMinutes: Double,
      worstDelayMinutes: Double,
  )

  /** `exitHistory`: every (absoluteTimeMinutes, cumulativeExitCount) observed during the
   * service, in non-decreasing time order. `admittedPerSpan(k)`: how many items were
   * admitted during span k. Both endpoints of a FIFO attribution, per the header above --
   * never a per-item timestamp. */
  def perSpanDelays(
      admittedPerSpan: Seq[Int],
      exitHistory: Seq[(Double, Int)],
      spanLengthMinutes: Double,
  ): Vector[Double] =
    val cumulativeAdmitted = admittedPerSpan.scanLeft(0)(_ + _).drop(1)
    cumulativeAdmitted.zipWithIndex.map { case (threshold, index) =>
      val deliveryTime = exitHistory
        .find(_._2 >= threshold)
        .map(_._1)
        .getOrElse(throw IllegalStateException(s"exit history never reaches $threshold admitted items"))
      val deadline = spanLengthMinutes * (index + 1)
      math.max(0.0, deliveryTime - deadline)
    }.toVector

  def simulateService(
      config: KitchenPipeline.Config,
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
      admissionQuota: Int,
      spanLengthMinutes: Double,
      spans: Int,
      rnd: Random,
  ): ServiceResult =
    require(spans > 0, "spans must be positive")
    val ctmc = SPN.toCTMC(
      KitchenPipeline.spn(config, lambdaPerMinute, prepRatePerMinute, loadingRatePerMinute, cookRatePerMinute),
    )

    val exitHistory = scala.collection.mutable.ArrayBuffer[(Double, Int)]()
    val admittedPerSpan = scala.collection.mutable.ArrayBuffer[Int]()

    var marking = KitchenPipeline.initialMarking(config, admissionQuota)
    var elapsed = 0.0
    exitHistory += (0.0 -> marking(Exit))

    for span <- 0 until spans do
      val admittedBefore = marking(Admitted)
      val segment = ctmc.newSimulationTrace(marking, rnd).takeWhile(_.time <= spanLengthMinutes).toVector
      for event <- segment.view.drop(1) do exitHistory += ((elapsed + event.time) -> event.state(Exit))
      val boundary = segment.last.state
      admittedPerSpan += (boundary(Admitted) - admittedBefore)
      elapsed += spanLengthMinutes
      val nextQuota = if span == spans - 1 then 0 else admissionQuota
      marking = KitchenPipeline.resetAdmission(boundary, nextQuota)

    // Admission is closed for good (quota 0 from here on): drain whatever's still mid-
    // pipeline until every admitted item has exited, however long that takes.
    val finalAdmitted = marking(Admitted)
    val drainEvents = ctmc.newSimulationTrace(marking, rnd).iterator
    var draining = marking(Exit) < finalAdmitted
    while draining do
      val event = drainEvents.next()
      exitHistory += ((elapsed + event.time) -> event.state(Exit))
      if event.state(Exit) >= finalAdmitted then draining = false

    val delays = perSpanDelays(admittedPerSpan.toVector, exitHistory.toVector, spanLengthMinutes)
    ServiceResult(delays, delays.sum, delays.sum / delays.size, delays.max)
