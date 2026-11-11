package kitchen.model

import java.util.Random
import u07.utils.MSet
import u07.modeling.SPN
import kitchen.util.FastMSet

/**
 * Gillespie's direct method for an SPN. Unlike the vendored `SPN.toCTMC` +
 * `CTMCSimulation`, it builds only the successor of the transition that fires, which is
 * about 20 times faster. Transitions are kept in a fixed order so that a seed gives the
 * same run in any process.
 */
final class SpnSimulator[P](spn: SPN.SPN[P]):

  private final class Compiled(
      val cond: Vector[(P, Int)],
      val inh: Vector[P],
      val delta: Vector[(P, Int)],
      val rate: MSet[P] => Double,
  )

  private def orderKey(t: SPN.Trn[P]): String =
    def places(m: MSet[P]) = m.asList.map(_.toString).sorted.mkString(",")
    s"${places(t.cond)}|${places(t.eff)}|${places(t.inh)}"

  private val transitions: Array[Compiled] =
    val ordered = spn.toVector.sortBy(orderKey)
    require(
      ordered.map(orderKey).distinct.size == ordered.size,
      "transitions must differ in their places, so that their order is well defined",
    )
    ordered.map { t =>
      val delta = (t.eff.asMap.toSeq.map((p, n) => p -> n) ++ t.cond.asMap.toSeq.map((p, n) => p -> -n))
        .groupMapReduce(_._1)(_._2)(_ + _)
        .filter(_._2 != 0)
      Compiled(t.cond.asMap.toVector, t.inh.asMap.keys.toVector, delta.toVector, t.rate)
    }.toArray

  private val rates = new Array[Double](transitions.length)

  /** The time to the next event and the marking after it; `None` if nothing is enabled. */
  def step(m: MSet[P], rnd: Random): Option[(Double, MSet[P])] =
    val counts = m.asMap
    var total = 0.0
    var i = 0
    while i < transitions.length do
      val t = transitions(i)
      val enabled =
        t.inh.forall(p => counts.getOrElse(p, 0) == 0) &&
          t.cond.forall((p, n) => counts.getOrElse(p, 0) >= n)
      val r = if enabled then t.rate(m) else 0.0
      rates(i) = if r > 0 then r else 0.0
      total += rates(i)
      i += 1
    if total <= 0 then None
    else
      val target = rnd.nextDouble() * total
      var chosen = 0
      var cumulative = rates(0)
      while (cumulative <= target || rates(chosen) == 0.0) && chosen < transitions.length - 1 do
        chosen += 1
        cumulative += rates(chosen)
      // Rounding can carry the scan past the last enabled transition: step back to it.
      while rates(chosen) == 0.0 do chosen -= 1
      val dt = Math.log(1 / rnd.nextDouble()) / total
      val next = transitions(chosen).delta.foldLeft(counts) { case (acc, (p, d)) =>
        val n = acc.getOrElse(p, 0) + d
        if n > 0 then acc.updated(p, n) else acc.removed(p)
      }
      Some((dt, FastMSet.ofMap(next)))
