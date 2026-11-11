package kitchen.agent

import java.util.Random
import kitchen.model.{KitchenPipeline, SpnSimulator}
import KitchenPipeline.{Admitted, Exit, Lost, MovedOne, MovedTwo}

import scala.collection.mutable.ArrayBuffer as EmptyArray

/**
 * Runs a `KitchenPipeline` over a service of consecutive slots, with backlog carried
 * from slot to slot, and measures how late each slot's orders cleared against the slot's
 * end. Items are not coloured, so exits are attributed to slots first in, first out.
 */
object ServiceSimulation:

  case class ServiceResult(
      perSpanDelayMinutes: Vector[Double],
      totalDelayMinutes: Double,
      averageDelayMinutes: Double,
      worstDelayMinutes: Double,
      admittedItems: Int = 0,
      movedOneSlotItems: Int = 0,
      movedTwoSlotsItems: Int = 0,
      lostItems: Int = 0,
  ):
    /** The same service with each slot due `leadTimeMinutes` after its end. */
    def withLeadTime(leadTimeMinutes: Double): ServiceResult =
      require(leadTimeMinutes >= 0, "leadTimeMinutes must not be negative")
      if leadTimeMinutes == 0 then this
      else
        val delays = perSpanDelayMinutes.map(d => math.max(0.0, d - leadTimeMinutes))
        copy(perSpanDelayMinutes = delays, totalDelayMinutes = delays.sum, averageDelayMinutes = delays.sum / delays.size, worstDelayMinutes = delays.max)

  /** `exitHistory`: (time, cumulative exits), in time order. */
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

  /** One service of `slots.count` slots on `kitchen`, with the same quota in every slot. */
  def simulateService(kitchen: KitchenModel, admissionQuota: Int, slots: Slots, rnd: Random): ServiceResult =
    val simulator = kitchen.spn
    simulateSpans(
      kitchen.config,
      Vector.fill(slots.count)(simulator),
      Vector.fill(slots.count)(admissionQuota),
      slots.lengthMinutes,
      rnd,
    )

  /** A service whose arrival rate and quota change from slot to slot. */
  def simulateProfiledService(kitchen: ProfiledKitchen, quotas: Vector[Int], rnd: Random): ServiceResult =
    require(kitchen.slots == quotas.size, "need one quota per slot")
    simulateSpans(kitchen.config, kitchen.simulators, quotas, kitchen.slotLengthMinutes, rnd)

  private def simulateSpans(
      config: KitchenPipeline.Config,
      simulators: Vector[SpnSimulator[KitchenPipeline.Place]],
      quotas: Vector[Int],
      spanLengthMinutes: Double,
      rnd: Random,
  ): ServiceResult =
    val spans = simulators.size
    val exitHistory = EmptyArray[(Double, Int)]()
    val admittedPerSpan = EmptyArray[Int]()

    var marking = KitchenPipeline.serviceStart(config, quotas)
    var elapsed = 0.0
    exitHistory += (0.0 -> marking(Exit))
    var admittedBefore = 0

    for span <- 0 until spans do
      val simulator = simulators(span)
      // The first event past the slot's end is discarded: by memorylessness, nothing is lost.
      var t = 0.0
      var inSpan = true
      while inSpan do
        simulator.step(marking, rnd) match
          case Some((dt, next)) if t + dt <= spanLengthMinutes =>
            t += dt
            if next(Exit) != marking(Exit) then exitHistory += ((elapsed + t) -> next(Exit))
            marking = next
          case _ => inSpan = false
      val boundary = marking
      admittedPerSpan += (boundary(Admitted) - admittedBefore)
      admittedBefore = boundary(Admitted)
      elapsed += spanLengthMinutes
      marking = KitchenPipeline.advanceSlot(config, boundary, quotas, span + 1)

    // Admission is closed: drain until every admitted item has exited.
    val drain = simulators.last
    val finalAdmitted = marking(Admitted)
    var t = 0.0
    var draining = marking(Exit) < finalAdmitted
    while draining do
      drain.step(marking, rnd) match
        case Some((dt, next)) =>
          t += dt
          if next(Exit) != marking(Exit) then exitHistory += ((elapsed + t) -> next(Exit))
          marking = next
          if marking(Exit) >= finalAdmitted then draining = false
        case None =>
          throw IllegalStateException(s"the pipeline deadlocked with items still inside: $marking")

    val delays = perSpanDelays(admittedPerSpan.toVector, exitHistory.toVector, spanLengthMinutes)
    ServiceResult(
      delays,
      delays.sum,
      delays.sum / delays.size,
      delays.max,
      admittedPerSpan.sum,
      marking(MovedOne),
      marking(MovedTwo),
      marking(Lost),
    )
