// Vendored from ../../../asmd-public-models (src/main/scala/u08/modelling/CTMCExperiment.scala,
// commit 3297344) — see analysis-scala/README.md for the reuse rationale.
//
// One adjustment from the original: the course keeps each lesson folder self-contained,
// so u08's own copy imports its own (re-pasted) CTMC/CTMCSimulation/Stochastics/MSet —
// which in u08 specifically are declared under the typo'd package `scala.u08.utils`
// (present in the fetched source; not something to silently "fix" by renaming, so it's
// simply not vendored here). Rather than duplicate u07's classes a second time under
// u08, this file imports u07.modelling.{CTMC, CTMCSimulation.*} directly — same types,
// same behaviour, one fewer copy to keep in sync (the explicit CTMC import replaces
// same-package visibility, which is what the original relied on to see u08's own copy
// of the trait). That's the only change; eventually/bounded/experiment below are
// untouched.
//
// Not used by kitchen/CapacityAdvisor.scala: `experiment`'s `rnd` is a `given` defined
// right here in this object and resolved lexically, not as a `using` parameter — callers
// cannot supply their own seed, which the capacity advisor needs for reproducible runs
// and tests. See kitchen/Statistics.scala. CTMCExperimentSpec exercises this file's own
// `experiment` directly, unseeded, to keep it genuinely in use.
package u08.modeling

import java.util.Random
import u07.modeling.CTMC
import u07.modeling.CTMCSimulation.*

object CTMCExperiment:

  opaque type Property[A] = Trace[A] => Boolean

  given rnd: Random = new Random

  extension [S](self: CTMC[S])
    // globally is simply achieved by equivalence not G x= F not x
    def eventually[A](filt: A => Boolean): Property[A] =
      trace => trace exists (e => filt(e.state))

    // takes a property and makes it time bounded by the magics of streams
    def bounded[A](timeBound: Double)(prop: Property[A]): Property[A] =
      trace => prop(trace takeWhile (_.time <= timeBound))

    // a PRISM-like experiment, giving a statistical result (in [0,1])
    def experiment(runs: Int, prop: Property[S], s0: S, timeBound: Double): Double =
      (0 until runs).count: _ =>
        bounded(timeBound)(prop)(self.newSimulationTrace(s0, rnd))
      .toDouble/runs
