package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random
import kitchen.model.KitchenPipeline
import KitchenPipeline.*
import CapacityAgent.*

class CapacityAgentSpec extends AnyFunSuite:

  private val comfortable = Config(Route.PrepThenCook, prepWorkers = 5, cookStationCapacities = List(5, 5), Map(1 -> 1.0))
  private val underpowered = Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
  private val generousCaps = DelayCaps(averageDelayCapMinutes = 5.0, worstDelayCapMinutes = 5.0)
  private val tightCaps = DelayCaps(averageDelayCapMinutes = 1.0, worstDelayCapMinutes = 1.0)

  test("evaluate reports ~zero breach probability for a comfortably provisioned candidate"):
    val result = evaluate(
      comfortable,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 5.0,
      loadingRatePerMinute = 5.0,
      cookRatePerMinute = 5.0,
      admissionQuota = 3,
      spanLengthMinutes = 15.0,
      spans = 2,
      caps = generousCaps,
      rnd = new Random(2026),
      epsilon = 0.2,
      confidence = 0.7,
    )
    result.averageDelayExceedsCapProbability shouldBe 0.0
    result.worstDelayExceedsCapProbability shouldBe 0.0

  test("evaluate reports high breach probability for a badly underpowered candidate"):
    val result = evaluate(
      underpowered,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 0.1,
      loadingRatePerMinute = 0.1,
      cookRatePerMinute = 0.1,
      admissionQuota = 5,
      spanLengthMinutes = 15.0,
      spans = 2,
      caps = tightCaps,
      rnd = new Random(2026),
      epsilon = 0.2,
      confidence = 0.7,
    )
    result.averageDelayExceedsCapProbability should be > 0.8
    result.worstDelayExceedsCapProbability should be > 0.8

  test("recommendCapacity picks the top of the range when the kitchen comfortably handles all of it"):
    val recommendation = recommendCapacity(
      comfortable,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 5.0,
      loadingRatePerMinute = 5.0,
      cookRatePerMinute = 5.0,
      minQuota = 1,
      maxQuota = 4,
      spanLengthMinutes = 15.0,
      spans = 2,
      caps = generousCaps,
      riskTolerance = 0.2,
      rnd = new Random(2026),
      epsilon = 0.2,
      confidence = 0.7,
    )
    recommendation.map(_.admissionQuota) shouldBe Some(4)

  test("recommendCapacity backs off from the top of the range under genuine stress"):
    val mildlyStressed =
      Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
    val recommendation = recommendCapacity(
      mildlyStressed,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 0.5,
      loadingRatePerMinute = 0.5,
      cookRatePerMinute = 0.5,
      minQuota = 1,
      maxQuota = 10,
      spanLengthMinutes = 15.0,
      spans = 2,
      caps = DelayCaps(averageDelayCapMinutes = 1.0, worstDelayCapMinutes = 2.0),
      riskTolerance = 0.2,
      rnd = new Random(2026),
      epsilon = 0.2,
      confidence = 0.7,
    )
    recommendation shouldBe defined
    recommendation.get.admissionQuota should be < 10

  test("recommendCapacity returns None when even the bottom of the range breaches the caps"):
    val recommendation = recommendCapacity(
      underpowered,
      lambdaPerMinute = 1.0,
      prepRatePerMinute = 0.1,
      loadingRatePerMinute = 0.1,
      cookRatePerMinute = 0.1,
      minQuota = 5,
      maxQuota = 10,
      spanLengthMinutes = 15.0,
      spans = 2,
      caps = tightCaps,
      riskTolerance = 0.2,
      rnd = new Random(2026),
      epsilon = 0.2,
      confidence = 0.7,
    )
    recommendation shouldBe None

  test("is deterministic for a fixed seed"):
    val a = evaluate(comfortable, 1.0, 5.0, 5.0, 5.0, 3, 15.0, 2, generousCaps, new Random(42), 0.2, 0.7)
    val b = evaluate(comfortable, 1.0, 5.0, 5.0, 5.0, 3, 15.0, 2, generousCaps, new Random(42), 0.2, 0.7)
    a shouldBe b
