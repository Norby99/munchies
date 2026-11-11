package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random
import u07.utils.MSet
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import SpnDsl.*

class SpnSimulatorSpec extends AnyFunSuite:

  enum P:
    case A, B, C

  import P.*

  private val race = SPN[P](A ~ 2.0 ~> B, A ~ 1.0 ~> C)

  // Markings are compared by content: the simulator returns `FastMSet`s, which never
  // equal a vendored `MSet` of the same content.

  test("an absorbing marking has no next step"):
    SpnSimulator(race).step(MSet(B), new Random(1)) shouldBe None

  test("each enabled transition fires in proportion to its rate, after an exponential time at the total rate"):
    val simulator = SpnSimulator(race)
    val rnd = new Random(5)
    val steps = (1 to 30000).map(_ => simulator.step(MSet(A), rnd).get)
    steps.count(_._2.asMap == MSet(B).asMap).toDouble / steps.size shouldBe (2.0 / 3 +- 0.01)
    steps.map(_._1).sum / steps.size shouldBe (1.0 / 3 +- 0.01)

  test("inhibitors and marking-dependent rates are honoured"):
    val spn = SPN[P](A ~ ((m: MSet[P]) => m(A) * 1.0) ~> B, (A ~ 100.0 ~> C) ^^^ B)
    val simulator = SpnSimulator(spn)
    // With a B present, the fast transition is inhibited: only A -> B, at rate 2 (two As).
    simulator.step(MSet(A, A, B), new Random(3)).get._2.asMap shouldBe MSet(A, B, B).asMap

  test("agrees with the vendored simulator on the time an order takes through the pipeline"):
    import KitchenPipeline.*
    val config = Config(Route.PrepThenCook, prepWorkers = 2, cookStationCapacities = List(3), Map(2 -> 1.0), bakePhases = 3)
    val spn = KitchenPipeline.spn(config, KitchenPipeline.Rates(1.0, 2.0, 10.0, 0.5))
    val start = KitchenPipeline.initialMarking(config, admissionQuota = 2)
    val runs = 3000

    val vendored = SPN.toCTMC(spn)
    val rnd1 = new Random(17)
    val vendoredMean = (1 to runs).map { _ =>
      vendored.newSimulationTrace(start, rnd1).find(_.state(Exit) == 2).get.time
    }.sum / runs

    val simulator = SpnSimulator(spn)
    val rnd2 = new Random(17)
    val fastMean = (1 to runs).map { _ =>
      var m = start
      var t = 0.0
      while m(Exit) < 2 do
        val (dt, next) = simulator.step(m, rnd2).get
        t += dt
        m = next
      t
    }.sum / runs

    fastMean shouldBe (vendoredMean +- 0.05 * vendoredMean)
