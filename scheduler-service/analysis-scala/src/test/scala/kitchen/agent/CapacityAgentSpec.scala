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

  private val service = Slots(lengthMinutes = 15.0, count = 2)
  /** All three stage rates at `rate`, one order per minute. */
  private def kitchen(config: Config, rate: Double) = KitchenModel(config, Rates(1.0, rate, rate, rate))
  /** The demo-friendly rule (epsilon 0.2, confidence 0.7: 19 runs per candidate). */
  private val quick = DecisionRule(epsilon = 0.2, confidence = 0.7)
  private def judged(caps: DelayCaps, rule: DecisionRule = quick) = Assessment(service, Goal(caps, 0.2), rule)

  test("evaluate reports ~zero breach probability for a comfortably provisioned candidate"):
    val result = evaluate(kitchen(comfortable, 5.0), admissionQuota = 3, judged(generousCaps), new Random(2026))
    result.averageDelayExceedsCapProbability shouldBe 0.0
    result.worstDelayExceedsCapProbability shouldBe 0.0

  test("evaluate reports high breach probability for a badly underpowered candidate"):
    val result = evaluate(kitchen(underpowered, 0.1), admissionQuota = 5, judged(tightCaps), new Random(2026))
    result.averageDelayExceedsCapProbability should be > 0.8
    result.worstDelayExceedsCapProbability should be > 0.8

  test("recommendCapacity picks the top of the range when the kitchen comfortably handles all of it"):
    val recommendation = recommendCapacity(kitchen(comfortable, 5.0), 1 to 4, judged(generousCaps), new Random(2026))
    recommendation.map(_.admissionQuota) shouldBe Some(4)

  test("recommendCapacity backs off from the top of the range under genuine stress"):
    val mildlyStressed =
      Config(Route.PrepThenCook, prepWorkers = 1, cookStationCapacities = List(1), Map(1 -> 1.0))
    val caps = DelayCaps(averageDelayCapMinutes = 1.0, worstDelayCapMinutes = 2.0)
    val recommendation = recommendCapacity(kitchen(mildlyStressed, 0.5), 1 to 10, judged(caps), new Random(2026))
    recommendation shouldBe defined
    recommendation.get.admissionQuota should be < 10

  test("recommendCapacity returns None when even the bottom of the range breaches the caps"):
    val recommendation = recommendCapacity(kitchen(underpowered, 0.1), 5 to 10, judged(tightCaps), new Random(2026))
    recommendation shouldBe None

  test("is deterministic for a fixed seed"):
    val a = evaluate(kitchen(comfortable, 5.0), 3, judged(generousCaps), new Random(42))
    val b = evaluate(kitchen(comfortable, 5.0), 3, judged(generousCaps), new Random(42))
    a shouldBe b

  test("with early rejection, a hopeless candidate is rejected after a fraction of its runs"):
    // epsilon 0.1 at 80% confidence asks for 116 runs. Rejection needs more than
    // 0.2 * 116 = 23.2 breaches, so at least 24 runs; nearly every run breaches here.
    val rule = DecisionRule(epsilon = 0.1, confidence = 0.8, earlyRejection = true)
    val result = evaluate(kitchen(underpowered, 0.1), 5, judged(tightCaps, rule), new Random(2026))
    result.runs should (be >= 24 and be < rule.runs)
    result.averageDelayExceedsCapProbability should be > 0.2

  test("with early rejection, a candidate that never breaches still gets all its runs"):
    val rule = quick.copy(earlyRejection = true)
    val result = evaluate(kitchen(comfortable, 5.0), 3, judged(generousCaps, rule), new Random(2026))
    result.runs shouldBe rule.runs
    result.averageDelayExceedsCapProbability shouldBe 0.0

  test("earlyRejection reaches the same verdicts at the edges of the range"):
    def recommend(config: Config, rate: Double, caps: DelayCaps, minQuota: Int) =
      recommendCapacity(kitchen(config, rate), minQuota to 10, judged(caps, quick.copy(earlyRejection = true)), new Random(2026))
    recommend(comfortable, 5.0, generousCaps, 1).map(_.admissionQuota) shouldBe Some(10)
    recommend(underpowered, 0.1, tightCaps, 5) shouldBe None

  test("the sequential test settles a candidate far from the tolerance in far fewer runs than the fixed sample"):
    val h = sequentialHypotheses(0.2, 0.05, 0.9)
    def runsToVerdict(p: Double, seed: Int): (Int, Boolean) =
      val rnd = new Random(seed)
      val test = SequentialTest.Test(h)
      var n = 0
      while test.verdict.isEmpty do
        test.observe(rnd.nextDouble() < p)
        n += 1
      (n, test.verdict.get)
    val good = (1 to 200).map(runsToVerdict(0.02, _))
    good.count(_._2) should be >= 190
    good.map(_._1).sum / 200.0 should be < 40.0
    val bad = (1 to 200).map(runsToVerdict(0.5, _))
    bad.count(!_._2) shouldBe 200
    bad.map(_._1).sum / 200.0 should be < 15.0

  test("the sequential test accepts a quota at the tolerance with at most the stated probability"):
    val h = sequentialHypotheses(0.2, 0.05, 0.9)
    val accepted = (1 to 2000).count { seed =>
      val rnd = new Random(seed)
      val test = SequentialTest.Test(h)
      while test.verdict.isEmpty do test.observe(rnd.nextDouble() < 0.2)
      test.verdict.get
    }
    accepted.toDouble / 2000 should be <= 0.11

  test("with the sequential test the search reaches the same verdicts as with the fixed sample"):
    val rule = DecisionRule(epsilon = 0.05, confidence = 0.9, earlyRejection = true, safetyMargin = true, sequentialTest = true)
    def recommend(config: Config, rate: Double, caps: DelayCaps, minQuota: Int) =
      recommendCapacity(kitchen(config, rate), minQuota to 10, judged(caps, rule), new Random(2026))
    val generous = recommend(comfortable, 5.0, generousCaps, 1)
    generous.map(_.admissionQuota) shouldBe Some(10)
    generous.get.runs should be < rule.runs
    recommend(underpowered, 0.1, tightCaps, 5) shouldBe None
