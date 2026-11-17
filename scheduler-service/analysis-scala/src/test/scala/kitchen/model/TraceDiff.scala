package kitchen.model

import u07.utils.MSet
import u07.modeling.CTMCSimulation.*

/** Prints a trace as the tokens each event adds and removes. A visual aid, not a check. */
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
