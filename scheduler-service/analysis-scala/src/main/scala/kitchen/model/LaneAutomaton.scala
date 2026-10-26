package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

/**
 * The batch-service lane automaton shared by KitchenStation's stations and
 * KitchenPipeline's cook stage: grab one item at a time from `input`, stop only when a
 * presence test actually applies (no `input` left, or no `room` left), then complete a
 * committed batch at `ratePerMinute` into `room` + `output`.
 *
 * Policy: always prefer a full load; accept a partial load only when demand runs out
 * first. A lane may also require a sibling lane to have already committed to baking
 * (`siblingBake`, one variant per size it could be baking, checked as a read-arc) before
 * it can accept its first item -- the mechanism behind "a second lane only opens once the
 * first has committed, capped at the leftover room."
 *
 * `SPN.Trn.inh` is a presence test, not a magnitude test (`u07.utils.MSet.disjoined`), so
 * "load up to N" can't be one transition (`eff` is a fixed multiset); grabbing is a
 * per-filled-count family of transitions instead, one per level, stopping only once a
 * presence test actually applies. Grab/stop run at `LoadRatePerMinute`, far faster than
 * any real arrival/service rate, approximating a GSPN "immediate" transition on a
 * framework that only has timed ones.
 *
 * Generic over the place type `P` and over how a caller builds `room`/`load`/`bake`
 * places -- it knows nothing about stations or lanes as concepts, only the places a
 * caller hands it, so KitchenStation and KitchenPipeline can each keep their own `Place`
 * enum and vocabulary while sharing this one automaton.
 */
object LaneAutomaton:

  private val LoadRatePerMinute = 1e6

  def transitions[P](
      input: P,
      output: P,
      room: P,
      load: Int => P,
      bake: Int => P,
      capacity: Int,
      ratePerMinute: Double,
      siblingBake: Option[Int => P],
  ): Set[SPN.Trn[P]] =
    def grab(filled: Int): Set[SPN.Trn[P]] =
      val next = if filled + 1 == capacity then bake(capacity) else load(filled + 1)
      (filled, siblingBake) match
        case (0, Some(sibling)) =>
          (1 until capacity).map { siblingSize =>
            val siblingAt = sibling(siblingSize)
            (load(0), input, room, siblingAt) ~ LoadRatePerMinute ~> (next, siblingAt)
          }.toSet
        case _ =>
          Set((load(filled), input, room) ~ LoadRatePerMinute ~> next)

    def stop(filled: Int): Set[SPN.Trn[P]] =
      Set(
        load(filled) ~ LoadRatePerMinute ~> bake(filled) ^^^ input,
        load(filled) ~ LoadRatePerMinute ~> bake(filled) ^^^ room,
      )

    def complete(size: Int): SPN.Trn[P] =
      bake(size) ~ ratePerMinute ~> MSet.ofList(load(0) :: List.fill(size)(room) ++ List.fill(size)(output))

    val grabs = (0 until capacity).flatMap(grab).toSet
    val stops = (1 until capacity).flatMap(stop).toSet
    val completions = (1 to capacity).map(complete).toSet

    grabs ++ stops ++ completions
