package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random
import kitchen.model.KitchenPipeline
import KitchenPipeline.*
import ServiceSimulation.*

class ServiceSimulationSpec extends AnyFunSuite:

  test("perSpanDelays: a cohort delivered before its own deadline has zero delay"):
    val delays = perSpanDelays(
      admittedPerSpan = Vector(5, 5),
      exitHistory = Vector(0.0 -> 0, 10.0 -> 5, 32.0 -> 10),
      spanLengthMinutes = 15.0,
    )
    delays shouldBe Vector(0.0, 2.0)

  test("perSpanDelays: a span with nothing admitted never contributes positive delay"):
    val delays = perSpanDelays(
      admittedPerSpan = Vector(5, 0, 5),
      exitHistory = Vector(0.0 -> 0, 10.0 -> 5, 40.0 -> 10),
      spanLengthMinutes = 15.0,
    )
    delays(1) shouldBe 0.0

  private val route = Config(Route.PrepThenCook, prepWorkers = 2, cookStationCapacities = List(3), Map(1 -> 1.0))
  private val comfortable = KitchenModel(route, Rates(1.0, 2.0, 2.0, 1.0))

  test("a comfortably provisioned kitchen keeps every span's delay near zero"):
    val result = simulateService(KitchenModel(route, Rates(1.0, 2.0, 2.0, 1.0)), 5, Slots(15.0, 4), new Random(2026))
    result.perSpanDelayMinutes.length shouldBe 4
    result.totalDelayMinutes should be < 5.0
    result.worstDelayMinutes should be < 3.0

  test("an underpowered kitchen accumulates growing delay across spans (backlog carries over)"):
    val underpowered =
      Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
    val result =
      simulateService(KitchenModel(underpowered, Rates(1.0, 0.2, 0.2, 0.1)), 10, Slots(15.0, 3), new Random(2026))
    result.worstDelayMinutes should be > 0.0
    result.perSpanDelayMinutes(2) should be >= result.perSpanDelayMinutes(0)

  test("totalDelayMinutes, averageDelayMinutes and worstDelayMinutes are consistent with perSpanDelayMinutes"):
    val result =
      simulateService(comfortable, admissionQuota = 5, Slots(15.0, 4), new Random(1))
    result.totalDelayMinutes shouldBe (result.perSpanDelayMinutes.sum +- 1e-9)
    result.averageDelayMinutes shouldBe (result.totalDelayMinutes / 4 +- 1e-9)
    result.worstDelayMinutes shouldBe result.perSpanDelayMinutes.max

  test("is deterministic for a fixed seed"):
    val a = simulateService(comfortable, admissionQuota = 5, Slots(15.0, 4), new Random(99))
    val b = simulateService(comfortable, admissionQuota = 5, Slots(15.0, 4), new Random(99))
    a shouldBe b

  test("the same seed gives the same service, even though every call builds a fresh net"):
    // Each call builds a new SPN whose rate functions hash by identity, so this catches
    // any dependence of the sample path on hash-based iteration order.
    val config = Config(Route.PrepThenCook, prepWorkers = 2, cookStationCapacities = List(3), Map(1 -> 1.0))
    def run() = simulateService(KitchenModel(config, Rates(1.0, 1.0, 1.0, 0.5)), 6, Slots(15.0, 4), new Random(2026)).perSpanDelayMinutes
    val first = run()
    (1 to 5).foreach(_ => run() shouldBe first)

  test("withLeadTime judges every span against a deadline that much later"):
    val result = ServiceResult(Vector(0.0, 4.0, 20.0), 24.0, 8.0, 20.0)
    val promised = result.withLeadTime(15.0)
    promised.perSpanDelayMinutes shouldBe Vector(0.0, 0.0, 5.0)
    promised.averageDelayMinutes shouldBe (5.0 / 3)
    promised.worstDelayMinutes shouldBe 5.0
    result.withLeadTime(0.0) shouldBe result

  test("a profiled service with the same rate and quota in every slot is the plain service, draw for draw"):
    val config = Config(Route.PrepThenCook, 2, List(3), Map(1 -> 0.5, 2 -> 0.5), bakePhases = 2)
    val kitchen = KitchenModel(config, Rates(0.8, 1.0, 5.0, 0.5))
    val plain = ServiceSimulation.simulateService(kitchen, 6, Slots(15.0, 4), new java.util.Random(9))
    val profiledKitchen = ProfiledKitchen(kitchen, Vector.fill(4)(0.8), 15.0)
    val profiled = ServiceSimulation.simulateProfiledService(profiledKitchen, Vector.fill(4)(6), new java.util.Random(9))
    profiled shouldBe plain

  test("each slot of a profiled service admits up to its own quota"):
    val config = Config(Route.PrepThenCook, 2, List(3), Map(1 -> 1.0))
    // Heavy demand in every slot, quotas 1, 0 and 3: exactly 4 items get in.
    val kitchen = ProfiledKitchen(KitchenModel(config, Rates(0.0, 5.0, 5.0, 5.0)), Vector.fill(3)(20.0), 15.0)
    val r = ServiceSimulation.simulateProfiledService(kitchen, Vector(1, 0, 3), new java.util.Random(4))
    r.admittedItems shouldBe 4

  test("an order that does not fit is moved to the first later slot with room, ahead of that slot's own orders"):
    // Every customer accepts; demand far above quotas of 2 in each of 3 slots.
    val config = Config(Route.PrepThenCook, 2, List(3), Map(1 -> 1.0), deferral = Some(Deferral(1.0, 1.0)))
    val kitchen = ProfiledKitchen(KitchenModel(config, Rates(0.0, 5.0, 5.0, 5.0)), Vector.fill(3)(20.0), 15.0)
    val r = ServiceSimulation.simulateProfiledService(kitchen, Vector(2, 2, 2), new java.util.Random(4))
    // Slot 0's overflow takes all the room of slots 1 and 2, so their own orders are lost.
    r.admittedItems shouldBe 6
    r.movedOneSlotItems shouldBe 2
    r.movedTwoSlotsItems shouldBe 2
    r.lostItems should be > 0

  test("customers who decline the later slot are lost, in the proportion given"):
    val config = Config(Route.PrepThenCook, 2, List(3), Map(1 -> 1.0), deferral = Some(Deferral(0.9, 0.5)))
    // Slot 0 has no quota, the next slot room for everyone: each slot-0 order is moved
    // with probability 0.9 and lost otherwise.
    val kitchen = ProfiledKitchen(KitchenModel(config, Rates(0.0, 5.0, 5.0, 5.0)), Vector(1.0, 0.01, 0.01), 15.0)
    val rnd = new java.util.Random(12)
    val results = (1 to 300).map(_ =>
      ServiceSimulation.simulateProfiledService(kitchen, Vector(0, 1000, 1000), rnd),
    )
    val moved = results.map(_.movedOneSlotItems).sum.toDouble
    val lost = results.map(_.lostItems).sum.toDouble
    results.map(_.movedTwoSlotsItems).sum shouldBe 0
    moved / (moved + lost) shouldBe (0.9 +- 0.02)

  test("without moving, an order that does not fit is simply not admitted, and nothing is counted as moved or lost"):
    val config = Config(Route.PrepThenCook, 2, List(3), Map(1 -> 1.0))
    val kitchen = ProfiledKitchen(KitchenModel(config, Rates(0.0, 5.0, 5.0, 5.0)), Vector.fill(3)(20.0), 15.0)
    val r = ServiceSimulation.simulateProfiledService(kitchen, Vector(2, 2, 2), new java.util.Random(4))
    r.admittedItems shouldBe 6
    (r.movedOneSlotItems, r.movedTwoSlotsItems, r.lostItems) shouldBe (0, 0, 0)

