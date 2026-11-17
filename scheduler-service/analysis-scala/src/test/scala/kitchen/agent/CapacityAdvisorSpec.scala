package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random

class CapacityAdvisorSpec extends AnyFunSuite:

  private val lambda = 0.25 // ~1 order every 4 minutes
  private val mu = 1.0 / 18 // ~18 minute mean prep time
  private val horizon = 60.0
  private val epsilon = 0.05
  private val confidence = 0.9

  test("erlangBlockingProbability matches the known single-server closed form a/(1+a)"):
    CapacityAdvisor.erlangBlockingProbability(1.0, 1) shouldBe (0.5 +- 1e-9)
    CapacityAdvisor.erlangBlockingProbability(3.0, 1) shouldBe (0.75 +- 1e-9)

  test("erlangBlockingProbability matches the known two-server closed form for a=1: a^2/(2+2a+a^2)"):
    CapacityAdvisor.erlangBlockingProbability(1.0, 2) shouldBe (0.2 +- 1e-9)

  test("uses exactly the number of runs requiredRuns prescribes"):
    val advice = CapacityAdvisor.adviseCapacity(lambda, mu, 3, horizon, new Random(1), epsilon, confidence)
    advice.runs shouldBe Statistics.requiredRuns(epsilon, confidence)

  test("is deterministic for a fixed seed"):
    val a = CapacityAdvisor.adviseCapacity(lambda, mu, 3, horizon, new Random(2026), epsilon, confidence)
    val b = CapacityAdvisor.adviseCapacity(lambda, mu, 3, horizon, new Random(2026), epsilon, confidence)
    a shouldBe b

  test("blocks less often as candidate capacity grows, for the same rates"):
    val rnd = new Random(7)
    val small = CapacityAdvisor.adviseCapacity(lambda, mu, 1, horizon, rnd, epsilon, confidence)
    val large = CapacityAdvisor.adviseCapacity(lambda, mu, 6, horizon, rnd, epsilon, confidence)
    large.transientBlockingProbability should be < small.transientBlockingProbability
    large.steadyStateBlockingProbability should be < small.steadyStateBlockingProbability
