package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import java.util.Random
import kitchen.model.KitchenPipeline
import KitchenPipeline.*
import CapacityAgent.*

// Prints a scenario's result instead of asserting. Run with `sbt "testOnly kitchen.agent.AdHocRunSpec"`.
class AdHocRunSpec extends AnyFunSuite:

  test("coarse scan: where does breach probability drop under a 0.2 risk tolerance"):
    val config = Config(
      Route.PrepThenCook,
      prepWorkers = 3,
      cookStationCapacities = List(7),
      orderSizeProbabilities = Map(
        1 -> 0.05, 2 -> 0.15, 3 -> 0.2, 4 -> 0.2, 5 -> 0.15, 6 -> 0.15, 7 -> 0.05, 8 -> 0.05,
      ),
    )
    val caps = DelayCaps(averageDelayCapMinutes = 5.0, worstDelayCapMinutes = 15.0)
    val rnd = new Random(2026)
    val kitchen = KitchenModel(
      config,
      Rates(
        lambdaPerMinute = 3.0,
        prepRatePerMinute = 60.0 / 10, // 10s mean prep
        loadingRatePerMinute = 60.0 / 3, // 3s mean load
        cookRatePerMinute = 1.0 / 6, // 6min mean cook
      ),
    )

    // Loose precision: a coarse scan.
    val assessment = Assessment(Slots(15.0, 12), Goal(caps, 0.2), DecisionRule(epsilon = 0.15, confidence = 0.8))
    for quota <- Seq(12, 9, 6, 3, 1) do
      val evaluation = evaluate(kitchen, admissionQuota = quota, assessment, rnd)
      println(s"quota=${evaluation.admissionQuota}  " +
        f"P(avg delay > ${caps.averageDelayCapMinutes}%.0f min) = ${evaluation.averageDelayExceedsCapProbability}%.3f  " +
        f"P(worst delay > ${caps.worstDelayCapMinutes}%.0f min) = ${evaluation.worstDelayExceedsCapProbability}%.3f  " +
        s"(${evaluation.runs} runs)")
