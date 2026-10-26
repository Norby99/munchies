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

  test("a comfortably provisioned kitchen keeps every span's delay near zero"):
    val result = simulateService(
      config = route,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 2.0,
      loadingRatePerMinute = 2.0,
      cookRatePerMinute = 1.0,
      admissionQuota = 5,
      spanLengthMinutes = 15.0,
      spans = 4,
      rnd = new Random(2026),
    )
    result.perSpanDelayMinutes.length shouldBe 4
    result.totalDelayMinutes should be < 5.0
    result.worstDelayMinutes should be < 3.0

  test("an underpowered kitchen accumulates growing delay across spans (backlog carries over)"):
    val underpowered =
      Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
    val result = simulateService(
      config = underpowered,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 0.2,
      loadingRatePerMinute = 0.2,
      cookRatePerMinute = 0.1,
      admissionQuota = 10,
      spanLengthMinutes = 15.0,
      spans = 3,
      rnd = new Random(2026),
    )
    result.worstDelayMinutes should be > 0.0
    result.perSpanDelayMinutes(2) should be >= result.perSpanDelayMinutes(0)

  test("totalDelayMinutes, averageDelayMinutes and worstDelayMinutes are consistent with perSpanDelayMinutes"):
    val result =
      simulateService(route, 1.0, 2.0, 2.0, 1.0, admissionQuota = 5, spanLengthMinutes = 15.0, spans = 4, new Random(1))
    result.totalDelayMinutes shouldBe (result.perSpanDelayMinutes.sum +- 1e-9)
    result.averageDelayMinutes shouldBe (result.totalDelayMinutes / 4 +- 1e-9)
    result.worstDelayMinutes shouldBe result.perSpanDelayMinutes.max

  test("is deterministic for a fixed seed"):
    val a = simulateService(route, 1.0, 2.0, 2.0, 1.0, admissionQuota = 5, spanLengthMinutes = 15.0, spans = 4, new Random(99))
    val b = simulateService(route, 1.0, 2.0, 2.0, 1.0, admissionQuota = 5, spanLengthMinutes = 15.0, spans = 4, new Random(99))
    a shouldBe b
