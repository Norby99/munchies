package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

import java.util.Random

class RateEstimationSpec extends AnyFunSuite:

  private def syntheticSample(rnd: Random, count: Int, meanInterArrival: Double, meanPrep: Double): Seq[HistoricalOrder] =
    var t = 0.0
    (1 to count).map { _ =>
      t += -math.log(rnd.nextDouble()) * meanInterArrival
      val prep = -math.log(rnd.nextDouble()) * meanPrep
      val start = t * 60_000.0
      HistoricalOrder(start, start + prep * 60_000.0)
    }

  test("recovers lambda and mu close to the generating distribution's parameters"):
    val rnd = new Random(123)
    val history = syntheticSample(rnd, 3000, 4.0, 18.0)
    val rates = RateEstimation.fitRates(history)

    rates.sampleSize shouldBe 3000
    rates.lambdaPerMinute shouldBe (0.25 +- 0.05)
    rates.muPerMinute shouldBe (1.0 / 18 +- 0.01)

  test("throws with fewer than 2 samples"):
    an[IllegalArgumentException] should be thrownBy RateEstimation.fitRates(Seq())

  test("throws when a finish time is not after its start time"):
    an[IllegalArgumentException] should be thrownBy RateEstimation.fitRates(
      Seq(HistoricalOrder(0, 1000), HistoricalOrder(1000, 500)),
    )
