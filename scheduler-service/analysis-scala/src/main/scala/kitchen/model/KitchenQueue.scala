package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

/**
 * Kitchen capacity as a Stochastic Petri Net (M/M/c/c, "Erlang loss"): a bounded
 * `Free` place (starts with `capacity` tokens) feeds an `InPrep` place. Arrival
 * consumes one `Free` token, so it simply cannot fire once `Free` is empty: ordinary
 * PN token conservation enforces the capacity cap, no inhibitor arc needed. Completion
 * returns one token per finished order, at a rate proportional to how many are
 * currently in flight, as seen in lesson slides.
 *
 * This is exactly what ScheduleOrder (scheduler-service's TS code) does when a slot is
 * full just modeled as a continuous-time process with rates instead of a fixed per-slot
 * headcount.
 */
object KitchenQueue:

  enum Place:
    case Free, InPrep

  export Place.*

  /**
   * @param lambdaPerMinute arrival rate (lambda): average number of new orders arriving per minute. Constant, independent of how many orders are already in prep.
   * @param muPerMinute service rate (mu): average number of orders *one* concurrent prep
   *   slot finishes per minute. The completion transition's actual rate is
   *   `mu * occupancy`, since each in-prep order completes independently at rate `mu`.
   */
  def spn(lambdaPerMinute: Double, muPerMinute: Double): SPN.SPN[Place] =
    SPN(
      Free ~ lambdaPerMinute ~> InPrep,
      InPrep ~ (m => m(InPrep) * muPerMinute) ~> Free,
    )

  def initialMarking(capacity: Int): MSet[Place] = MSet.ofList(List.fill(capacity)(Free))

  def occupancy(m: MSet[Place]): Int = m(InPrep)
