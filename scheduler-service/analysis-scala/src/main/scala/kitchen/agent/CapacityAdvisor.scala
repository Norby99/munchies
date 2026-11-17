package kitchen.agent

import java.util.Random
import u07.utils.MSet
import u07.modeling.{CTMC, SPN}
import u07.modeling.CTMCSimulation.*
import kitchen.model.KitchenQueue

case class CapacityAdvice(
    candidateCapacity: Int,
    lambdaPerMinute: Double,
    muPerMinute: Double,
    /** Simulated, from an empty kitchen over the horizon. */
    transientBlockingProbability: Double,
    epsilon: Double,
    confidence: Double,
    runs: Int,
    /** Erlang B, as a cross-check. */
    steadyStateBlockingProbability: Double,
)

/** How often a candidate capacity of the `KitchenQueue` model would reject an order. */
object CapacityAdvisor:

  /** Erlang B by the stable recursion B(k, a) = a B(k-1, a) / (k + a B(k-1, a)). */
  def erlangBlockingProbability(offeredLoad: Double, capacity: Int): Double =
    (1 to capacity).foldLeft(1.0)((b, k) => (offeredLoad * b) / (k + offeredLoad * b))

  def adviseCapacity(
      lambdaPerMinute: Double,
      muPerMinute: Double,
      candidateCapacity: Int,
      timeHorizonMinutes: Double,
      rnd: Random,
      epsilon: Double = 0.01,
      confidence: Double = 0.95,
  ): CapacityAdvice =
    val ctmc: CTMC[MSet[KitchenQueue.Place]] = SPN.toCTMC(KitchenQueue.spn(lambdaPerMinute, muPerMinute))
    val initial = KitchenQueue.initialMarking(candidateCapacity)
    val runs = Statistics.requiredRuns(epsilon, confidence)

    val sumFractionBlocked = (0 until runs).foldLeft(0.0): (acc, _) =>
      // Vector: fractionOfTimeBlocked indexes into it.
      val trace = ctmc.newSimulationTrace(initial, rnd).takeWhile(_.time <= timeHorizonMinutes).toVector
      acc + fractionOfTimeBlocked(trace, candidateCapacity, timeHorizonMinutes)

    CapacityAdvice(
      candidateCapacity,
      lambdaPerMinute,
      muPerMinute,
      transientBlockingProbability = sumFractionBlocked / runs,
      epsilon = Statistics.epsilonFor(runs, confidence),
      confidence = confidence,
      runs = runs,
      steadyStateBlockingProbability = erlangBlockingProbability(lambdaPerMinute / muPerMinute, candidateCapacity),
    )

  private def fractionOfTimeBlocked(
      trace: Vector[Event[MSet[KitchenQueue.Place]]],
      capacity: Int,
      timeHorizonMinutes: Double,
  ): Double =
    if trace.isEmpty then 0.0
    else
      var timeBlocked = 0.0
      for i <- trace.indices do
        val start = trace(i).time
        val end = if i + 1 < trace.length then trace(i + 1).time else timeHorizonMinutes
        if KitchenQueue.occupancy(trace(i).state) >= capacity then
          timeBlocked += math.max(0.0, math.min(end, timeHorizonMinutes) - start)
      timeBlocked / timeHorizonMinutes
