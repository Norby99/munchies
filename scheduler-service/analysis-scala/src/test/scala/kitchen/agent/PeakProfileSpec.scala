package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random

class PeakProfileSpec extends AnyFunSuite:

  test("arrivals are sampled at each slot's own rate, and the fitted profile recovers it"):
    val rates = Vector(0.2, 1.0, 0.5)
    val rnd = new Random(8)
    val services = Vector.fill(400)(PeakProfile.sampleArrivals(rates, 10.0, rnd))
    services.flatten.forall(t => t >= 0 && t <= 30) shouldBe true
    val fitted = PeakProfile.fitProfile(services, 10.0, 3)
    fitted(0) shouldBe (0.2 +- 0.02)
    fitted(1) shouldBe (1.0 +- 0.05)
    fitted(2) shouldBe (0.5 +- 0.03)

  test("a slot nobody ordered in is fitted at rate zero"):
    PeakProfile.fitProfile(Seq(Seq(1.0, 2.0)), 10.0, 2) shouldBe Vector(0.2, 0.0)

  test("smoothing averages each slot with its neighbours and keeps a flat profile flat"):
    PeakProfile.smooth(Vector(1.0, 1.0, 1.0)) shouldBe Vector(1.0, 1.0, 1.0)
    val smoothed = PeakProfile.smooth(Vector(0.0, 4.0, 0.0))
    smoothed(1) shouldBe 2.0
    smoothed(0) shouldBe (4.0 / 3 +- 1e-9)
