import java.util.Random
import kitchen.model.KitchenPipeline
import KitchenPipeline.*
import kitchen.agent.{Assessment, CapacityAgent, DecisionRule, Goal, KitchenModel, ParallelCapacityAgent, Slots}
import kitchen.agent.CapacityAgent.DelayCaps

/** Times `CapacityAgent.evaluate` against `ParallelCapacityAgent.evaluate`. Run with `sbt analysisJVM/run`. */
object ParallelismDemo:
  def main(args: Array[String]): Unit =
    val config = Config(
      Route.PrepThenCook,
      prepWorkers = 3,
      cookStationCapacities = List(7),
      orderSizeProbabilities = Map(
        1 -> 0.05, 2 -> 0.15, 3 -> 0.2, 4 -> 0.2, 5 -> 0.15, 6 -> 0.15, 7 -> 0.05, 8 -> 0.05,
      ),
    )
    val kitchen = KitchenModel(config, Rates(3.0, 6.0, 20.0, 1.0 / 6))
    val caps = DelayCaps(averageDelayCapMinutes = 5.0, worstDelayCapMinutes = 15.0)
    val admissionQuota = 12
    val epsilon = 0.1
    val confidence = 0.9
    // The goal's tolerance is not used: `evaluate` only estimates, it does not judge.
    val assessment = Assessment(Slots(15.0, 12), Goal(caps, 0.2), DecisionRule(epsilon, confidence))

    println(s"cores available: ${Runtime.getRuntime.availableProcessors}")
    println(s"runs per candidate: ${assessment.rule.runs}")
    println()

    def time[A](label: String)(body: => A): A =
      val t0 = System.nanoTime()
      val result = body
      println(f"$label%-28s elapsed=${(System.nanoTime() - t0) / 1e9}%6.2fs")
      result

    val sequential = time("CapacityAgent (sequential)"):
      CapacityAgent.evaluate(kitchen, admissionQuota, assessment, new Random(2026))
    println(f"  -> P(avg>cap)=${sequential.averageDelayExceedsCapProbability}%.3f  " +
      f"P(worst>cap)=${sequential.worstDelayExceedsCapProbability}%.3f  (${sequential.runs} runs)")
    println()

    val (pool, ec) = ParallelCapacityAgent.newExecutionContext()
    given scala.concurrent.ExecutionContext = ec
    val parallel = time("ParallelCapacityAgent"):
      ParallelCapacityAgent.evaluate(kitchen, admissionQuota, assessment, new Random(2026))
    println(f"  -> P(avg>cap)=${parallel.averageDelayExceedsCapProbability}%.3f  " +
      f"P(worst>cap)=${parallel.worstDelayExceedsCapProbability}%.3f  (${parallel.runs} runs)")
    pool.shutdown()
