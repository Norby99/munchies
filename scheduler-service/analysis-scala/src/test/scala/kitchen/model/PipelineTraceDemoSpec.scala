package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import java.util.Random
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import KitchenPipeline.*

/** Prints a pipeline trace, not a check. Run with `sbt "testOnly kitchen.PipelineTraceDemoSpec"`. */
class PipelineTraceDemoSpec extends AnyFunSuite:

  test("watch a trace"):
    val probabilities = Map(1 -> 0.1, 2 -> 0.2, 3 -> 0.3, 4 -> 0.3, 5 -> 0.1)
    val config = Config(Route.PrepThenCook, prepWorkers = 2,
      cookStationCapacities = List(2, 2), orderSizeProbabilities = probabilities)
    val ctmc = SPN.toCTMC(
      KitchenPipeline.spn(
        config,
        KitchenPipeline.Rates(
          lambdaPerMinute = 3,
          prepRatePerMinute = 10,
          loadingRatePerMinute = 20,
          cookRatePerMinute = 0.5,
        ),
      ),
    )
    val initial = KitchenPipeline.initialMarking(config, admissionQuota = 10)
    val trace = ctmc.newSimulationTrace(initial, new Random(42)).takeWhile(_.time <= 6.0)
    TraceDiff.print(initial, trace)
