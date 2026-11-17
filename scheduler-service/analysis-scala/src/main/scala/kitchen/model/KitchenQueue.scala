package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

/** The M/M/c/c (Erlang loss) queue as an SPN: `capacity` `Free` tokens feed `InPrep`. */
object KitchenQueue:

  enum Place:
    case Free, InPrep

  export Place.*

  /** `muPerMinute` is per order in prep. */
  def spn(lambdaPerMinute: Double, muPerMinute: Double): SPN.SPN[Place] =
    SPN(
      Free ~ lambdaPerMinute ~> InPrep,
      InPrep ~ (m => m(InPrep) * muPerMinute) ~> Free,
    )

  def initialMarking(capacity: Int): MSet[Place] = MSet.ofList(List.fill(capacity)(Free))

  def occupancy(m: MSet[Place]): Int = m(InPrep)
