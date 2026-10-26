package kitchen.model

import u07.utils.MSet
import u07.modeling.CTMCSimulation.*

/** A visualization aid for `testOnly`-style demos (not a correctness check): prints a
 * Gillespie trace as a sequence of marking diffs -- which places gained or lost tokens
 * at each event -- instead of the full marking every time, which is what actually makes
 * a trace readable for a model with this many places. */
object TraceDiff:
  def print[P](initial: MSet[P], trace: Trace[MSet[P]]): Unit =
    var last = initial
    for event <- trace do
      val adds = event.state.asMap
        .collect { case (p, n) if n > last(p) => s"${p}+${n - last(p)}" }
        .mkString(", ")
      val removes = last.asMap
        .collect { case (p, n) if n > event.state(p) => s"${p}-${n - event.state(p)}"}
        .mkString(", ")
      if adds.nonEmpty || removes.nonEmpty then
        println(f"t=${event.time}%7.4f   -[$removes]   +[$adds]")
      last = event.state
