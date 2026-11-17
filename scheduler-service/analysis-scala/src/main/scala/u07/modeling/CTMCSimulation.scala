// Vendored from ../../../asmd-public-models (src/main/scala/u07/modelling/CTMCSimulation.scala,
// commit 3297344) — see analysis-scala/README.md for the reuse rationale.
//
// One deliberate deviation from the original, on the line marked below: the original
// re-emits `Event(t, s)` (the SAME t) forever once a state is absorbing. That is fine
// as long as callers only ever `.take(n)` a bounded number of elements, but it becomes
// an infinite loop — and an OOM, since LazyList memoizes every element it forces — the
// moment a caller does `.takeWhile(_.time <= timeBound)` (exactly what
// u08.modelling.CTMCExperiment.bounded and this project's own CapacityAdvisor do):
// `t` never exceeds timeBound, so the predicate never goes false. Tagging the
// post-absorption event with `Double.PositiveInfinity` instead fixes that composition
// without changing the algorithm itself — `takeWhile` then stops at the first such
// event instead of forcing it forever. Reproduced by this project's own
// CTMCSimulationSpec before the fix was applied.
package u07.modeling

import java.util.Random
import u07.utils.Stochastics

object CTMCSimulation:

  case class Event[A](time: Double, state: A)
  type Trace[A] = LazyList[Event[A]]

  export CTMC.*

  extension [S](self: CTMC[S])
    def newSimulationTrace(s0: S, rnd: Random): Trace[S] =
      LazyList.iterate(Event(0.0, s0)):
        case Event(t, s) =>
          if self.transitions(s).isEmpty
          then
            Event(Double.PositiveInfinity, s) // deviation: was `Event(t, s)` — see file header
          else
            val choices = self.transitions(s) map (t => (t.rate, t.state))
            val next = Stochastics.cumulative(choices.toList)
            val sumR = next.last._1
            val choice = Stochastics.draw(next)(using rnd)
            Event(t + Math.log(1 / rnd.nextDouble()) / sumR, choice)
