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
    /** Simulated blocking probability over the given horizon, starting from an empty
      * kitchen — the transient behavior right after opening. No closed form; this is
      * exactly where Gillespie simulation earns its keep over Erlang B. */
    transientBlockingProbability: Double,
    epsilon: Double,
    confidence: Double,
    runs: Int,
    /** Closed-form steady-state blocking probability for the same lambda/mu/capacity,
      * included only as a cross-check (see analysis-scala's tests) — it assumes the
      * queue has run "forever", so it is always >= the transient value. */
    steadyStateBlockingProbability: Double,
)

/**
 * Offline capacity-tuning helper: estimates how often a candidate kitchen capacity
 * would reject an arriving order (scheduler-service's 409 Conflict path) during a
 * service window, given fitted arrival/service rates. Not on the request path — meant
 * to run against order-service history to sanity-check or suggest
 * CapacityPolicy.maxOrdersPerSlot. The only implementation of this (see README.md) --
 * there is no longer a parallel TypeScript copy.
 *
 * Runs the vendored u07.modelling.CTMCSimulation.newSimulationTrace directly (not
 * u08.modelling.CTMCExperiment.experiment — see that file's header for why: its rnd
 * isn't caller-suppliable, so results can't be seeded/reproduced).
 */
object CapacityAdvisor:

  /** Erlang B, numerically-stable recursion B(0,a)=1, B(k,a)=a*B(k-1,a)/(k+a*B(k-1,a)).
   * Not vendored: plain queueing theory, used only to cross-check the simulator. */
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
      // Vector, not List: fractionOfTimeBlocked below does indexed lookups, which are
      // O(1) on a Vector and O(n) (so O(n^2) over the whole loop) on a List.
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
