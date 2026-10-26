package u08.modeling

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.modeling.CTMC
import u07.modeling.CTMC.*
import u08.modeling.CTMCExperiment.*

/**
 * Exercises the vendored u08.modelling.CTMCExperiment (Property/eventually/bounded/
 * experiment) directly and unseeded, the way the course itself uses it — as opposed to
 * kitchen.CapacityAdvisor, which bypasses `experiment` for the reasons explained in
 * this file's header. Tolerance is wide because there is no seed to pin down here.
 */
class CTMCExperimentSpec extends AnyFunSuite:

  // Resolves to "success" w.p. 0.3 (rate 3 vs rate 7 in Gillespie's race), then absorbs.
  val race: CTMC[String] = CTMC.ofFunction:
    case "pending" => Set(3.0 --> "success", 7.0 --> "failure")
    case _ => Set()

  test("experiment recovers the known race probability within a generous tolerance"):
    val p = race.experiment(runs = 4000, prop = race.eventually(_ == "success"), s0 = "pending", timeBound = 1000.0)
    math.abs(p - 0.3) should be < 0.05
