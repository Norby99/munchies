package kitchen.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import u07.utils.MSet
import u07.modeling.SPN
import u07.modeling.CTMCSimulation.*
import java.util.Random
import kitchen.agent.CapacityAdvisor

class KitchenQueueSpec extends AnyFunSuite:

  test("arrival is not offered once the kitchen is at capacity (ordinary PN token conservation, no inhibitor arc)"):
    val ctmc = SPN.toCTMC(KitchenQueue.spn(lambdaPerMinute = 1.0, muPerMinute = 1.0))
    val full = MSet.ofList(List.fill(3)(KitchenQueue.InPrep)) // Free is empty
    val actions = ctmc.transitions(full)
    actions.map(_.state).forall(s => KitchenQueue.occupancy(s) <= 3) shouldBe true
    // only the completion action should be available
    actions.size shouldBe 1

  test("completion rate scales with occupancy (each in-flight order is independent)"):
    val ctmc = SPN.toCTMC(KitchenQueue.spn(lambdaPerMinute = 2.0, muPerMinute = 5.0))
    val twoInPrep = MSet(KitchenQueue.Free, KitchenQueue.Free, KitchenQueue.InPrep, KitchenQueue.InPrep)
    val actions = ctmc.transitions(twoInPrep)
    actions.exists(a => a.rate == 10.0) shouldBe true // 2 in-flight * mu=5

  test("initialMarking starts fully free"):
    val m = KitchenQueue.initialMarking(4)
    KitchenQueue.occupancy(m) shouldBe 0
    m(KitchenQueue.Free) shouldBe 4

  test("Gillespie simulation converges to the Erlang B closed form in steady state"):
    val lambda = 1.0
    val mu = 1.0
    val capacity = 2
    val analytic = CapacityAdvisor.erlangBlockingProbability(lambda / mu, capacity) // = 0.2

    val rnd = new Random(2026)
    val ctmc = SPN.toCTMC(KitchenQueue.spn(lambda, mu))
    val timeHorizon = 400.0
    val runs = 1500

    val avgFractionBlocked = (0 until runs).map { _ =>
      val trace = ctmc.newSimulationTrace(KitchenQueue.initialMarking(capacity), rnd)
        .takeWhile(_.time <= timeHorizon).toVector // O(1) indexed access below, not O(n) on a List
      var timeBlocked = 0.0
      for i <- trace.indices do
        val start = trace(i).time
        val end = if i + 1 < trace.length then trace(i + 1).time else timeHorizon
        if KitchenQueue.occupancy(trace(i).state) >= capacity then
          timeBlocked += math.max(0.0, math.min(end, timeHorizon) - start)
      timeBlocked / timeHorizon
    }.sum / runs

    math.abs(avgFractionBlocked - analytic) should be < 0.05
