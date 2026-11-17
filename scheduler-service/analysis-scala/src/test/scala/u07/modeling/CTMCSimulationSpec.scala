package u07.modeling

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.modeling.CTMC
import u07.modeling.CTMCSimulation.* // re-exports CTMC.* (Action, -->, ...) via `export CTMC.*`
import java.util.Random

class CTMCSimulationSpec extends AnyFunSuite:

  // A -> B and B -> A, both at rate 1: a ping-pong CTMC with no absorbing state.
  val pingPong: CTMC[String] = CTMC.ofFunction:
    case "A" => Set(1.0 --> "B")
    case "B" => Set(1.0 --> "A")

  // Resolves to "success" w.p. 0.3 (rate 3 vs rate 7 in Gillespie's race), then absorbs.
  val race: CTMC[String] = CTMC.ofFunction:
    case "pending" => Set(3.0 --> "success", 7.0 --> "failure")
    case _ => Set()

  test("a trace never contains a time beyond where it was truncated"):
    val trace = pingPong.newSimulationTrace("A", new Random(1)).takeWhile(_.time <= 10.0).toList
    trace.foreach(_.time should be <= 10.0)

  test("event times strictly increase"):
    val trace = pingPong.newSimulationTrace("A", new Random(7)).takeWhile(_.time <= 20.0).toList
    trace.sliding(2).foreach:
      case Seq(e1, e2) => e2.time should be > e1.time
      case _ => ()

  test("an absorbing state's continuation is tagged at +Infinity, not repeated at the same time"):
    val trace = race.newSimulationTrace("success", new Random(3)).take(3).toList
    trace.map(_.state) should be(List.fill(3)("success"))
    trace.drop(1).foreach(_.time shouldBe Double.PositiveInfinity)

  test("is deterministic for a given seed"):
    val traceA = pingPong.newSimulationTrace("A", new Random(99)).take(10).toList
    val traceB = pingPong.newSimulationTrace("A", new Random(99)).take(10).toList
    traceA should be(traceB)

  test("a bounded takeWhile over an absorbing chain terminates (regression for the OOM this project hit)"):
    // Would previously hang/OOM: the vendored algorithm repeated `Event(t, s)` forever
    // on absorption, so `time <= timeBound` never went false. See CTMCSimulation's header.
    val trace = race.newSimulationTrace("pending", new Random(11)).takeWhile(_.time <= 1000.0).toList
    trace should not be empty
    trace.last.state should (be("success") or be("failure"))
