package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.utils.MSet
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import java.util.Random
import kitchen.agent.CapacityAdvisor
import KitchenStation.*

class KitchenStationSpec extends AnyFunSuite:

  test("a station with stationCapacity=1 degenerates to KitchenQueue's Erlang B steady state"):
    val lambda = 1.0
    val mu = 1.0
    val stations = 2
    val config = Config(stations, stationCapacity = 1, orderSizeProbabilities = Map(1 -> 1.0))
    val analytic = CapacityAdvisor.erlangBlockingProbability(lambda / mu, stations) // = 0.2

    val rnd = new Random(2026)
    val ctmc = SPN.toCTMC(KitchenStation.spn(config, lambda, mu))
    // Shorter horizon/fewer runs than KitchenQueueSpec's analogous check: this model's
    // per-item loading automaton (see KitchenStation's header) adds a bounded but real
    // constant-factor cost per Gillespie step over the plain KitchenQueue, so matching
    // KitchenQueueSpec's parameters here would take minutes instead of seconds.
    val timeHorizon = 100.0
    val runs = 300

    val avgFractionBlocked = (0 until runs).map { _ =>
      val trace = ctmc
        .newSimulationTrace(initialMarking(config), rnd)
        .takeWhile(_.time <= timeHorizon)
        .toVector
      var timeBlocked = 0.0
      for i <- trace.indices do
        val start = trace(i).time
        val end = if i + 1 < trace.length then trace(i + 1).time else timeHorizon
        if isAtCapacity(trace(i).state) then
          timeBlocked += math.max(0.0, math.min(end, timeHorizon) - start)
      timeBlocked / timeHorizon
    }.sum / runs

    math.abs(avgFractionBlocked - analytic) should be < 0.07

  private val threeSlotConfig = Config(stations = 1, stationCapacity = 3, orderSizeProbabilities = Map(1 -> 1.0))
  private val threeSlotCtmc = SPN.toCTMC(KitchenStation.spn(threeSlotConfig, lambdaPerMinute = 1.0, muPerMinute = 1.0))

  private def marking(places: Place*): MSet[Place] = MSet.ofList(places.toList)

  test("a partially filled lane keeps grabbing while orders are waiting and room remains (never settles early)"):
    val m = marking(
      List.fill(5)(Waiting) ++
        List.fill(2)(Room(0)) ++
        List(Load(0, Primary, 1), Load(0, Secondary, 0))*
    )
    val actions = threeSlotCtmc.transitions(m)
    actions.map(_.state) should contain(marking(List.fill(4)(Waiting) ++ List(Room(0), Load(0, Primary, 2), Load(0, Secondary, 0))*))
    actions.map(_.state) should not contain marking(List.fill(5)(Waiting) ++ List.fill(2)(Room(0)) ++ List(Bake(0, Primary, 1), Load(0, Secondary, 0))*)

  test("a lane stops and bakes once orders run out, even if it never reached full capacity"):
    val m = marking(List.fill(2)(Room(0)) ++ List(Load(0, Primary, 1), Load(0, Secondary, 0))*) // Waiting = 0
    val actions = threeSlotCtmc.transitions(m)
    actions.map(_.state) should contain(marking(List.fill(2)(Room(0)) ++ List(Bake(0, Primary, 1), Load(0, Secondary, 0))*))

  test("a lane stops and bakes once its station's room runs out, even with more orders waiting"):
    val m = marking(List.fill(5)(Waiting) ++ List(Load(0, Primary, 1), Load(0, Secondary, 0))*) // Room(0) = 0
    val actions = threeSlotCtmc.transitions(m)
    actions.map(_.state) should contain(marking(List.fill(5)(Waiting) ++ List(Bake(0, Primary, 1), Load(0, Secondary, 0))*))

  test("the secondary lane cannot start while the primary lane is still loading"):
    val m = marking(
      List.fill(5)(Waiting) ++ List.fill(1)(Room(0)) ++ List(Load(0, Primary, 2), Load(0, Secondary, 0))*
    )
    val actions = threeSlotCtmc.transitions(m)
    actions.map(_.state).exists(_(Load(0, Secondary, 1)) > 0) shouldBe false

  test("the secondary lane can start once the primary has committed to a partial bake, capped at the leftover room"):
    val m = marking(
      List.fill(5)(Waiting) ++ List.fill(1)(Room(0)) ++ List(Bake(0, Primary, 2), Load(0, Secondary, 0))*
    )
    val actions = threeSlotCtmc.transitions(m)
    actions.map(_.state) should contain(
      marking(List.fill(4)(Waiting) ++ List(Bake(0, Primary, 2), Load(0, Secondary, 1))*),
    )

  test("an arriving order is lost outright if it doesn't fully fit, rather than partially admitted"):
    val config = Config(stations = 1, stationCapacity = 3, orderSizeProbabilities = Map(2 -> 1.0))
    val ctmc = SPN.toCTMC(KitchenStation.spn(config, lambdaPerMinute = 1.0, muPerMinute = 1.0))
    val m = marking(List.fill(1)(Capacity) ++ List.fill(3)(Room(0)) ++ List(Load(0, Primary, 0), Load(0, Secondary, 0))*)
    val actions = ctmc.transitions(m)
    actions.map(_.state).exists(_(Waiting) > 0) shouldBe false

  test("occupancy sums whatever each lane is currently baking, across stations"):
    val m = marking(Bake(0, Primary, 3), Bake(1, Secondary, 1), Load(1, Primary, 0))
    occupancy(m) shouldBe 4

  test("isAtCapacity reflects the shared Capacity counter, not any single station's room"):
    isAtCapacity(marking(Capacity)) shouldBe false
    isAtCapacity(marking(Room(0), Room(0))) shouldBe true
