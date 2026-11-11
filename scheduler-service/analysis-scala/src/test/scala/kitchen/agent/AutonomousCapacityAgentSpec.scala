package kitchen.agent

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import java.util.Random
import kitchen.model.KitchenPipeline.{Rates, Route}
import AutonomousCapacityAgent.*
import CapacityAgent.DelayCaps

class AutonomousCapacityAgentSpec extends AnyFunSuite:

  private val sizes = Map(1 -> 1.0)
  private val comfortable = KitchenConditions("comfortable", Rates(1.0, 5.0, 5.0, 5.0), Staffing(5, List(5, 5)))
  private val underpowered = KitchenConditions("underpowered", Rates(1.0, 0.1, 0.1, 0.1), Staffing(1, List(1)))

  private val setup = KitchenSetup(Route.PrepThenCook, sizes)
  private val slots = Slots(15.0, 2)
  private val caps = DelayCaps(2.0, 2.0)
  /** A quick rule (19 runs per candidate), with early rejection. */
  private val assessment = Assessment(slots, Goal(caps, 0.2), DecisionRule(0.2, 0.7, earlyRejection = true))

  private def settings(mode: Mode, minQuota: Int = 1, maxQuota: Int = 4) =
    AgentSettings(setup, assessment, minQuota, maxQuota, mode)

  private def kitchen(phases: Vector[KitchenConditions], initialQuota: Int = 2) =
    SimulatedKitchen(phases, setup, slots, caps, initialQuota, ordersPerPeriod = 200, outcomeRuns = 2, new Random(7))

  /** Replays fixed percepts and records every quota the agent applies. */
  private class ScriptedEnvironment(percepts: Seq[Percept]) extends KitchenEnvironment:
    private var next = 0
    var applied = Vector.empty[Int]
    override def perceive(): Percept =
      val p = percepts(next)
      next += 1
      p
    override def applyQuota(quota: Int): Unit = applied :+= quota

  test("fitStages recovers every stage's rate from a large sample"):
    val rnd = new Random(123)
    def exp(rate: Double) = -math.log(1.0 - rnd.nextDouble()) / rate
    var t = 0.0
    val samples = (1 to 5000).map { _ =>
      t += exp(0.5)
      StageSample(t, exp(2.0), exp(4.0), exp(0.25))
    }
    val rates = RateEstimation.fitStages(samples)
    rates.lambdaPerMinute shouldBe (0.5 +- 0.03)
    rates.prepRatePerMinute shouldBe (2.0 +- 0.1)
    rates.loadingRatePerMinute shouldBe (4.0 +- 0.2)
    rates.cookRatePerMinute shouldBe (0.25 +- 0.015)

  test("fitStages rejects a non-positive duration"):
    an[IllegalArgumentException] should be thrownBy RateEstimation.fitStages(
      Seq(StageSample(0, 1, 1, 1), StageSample(1, 1, 0, 1)),
    )

  test("in Auto mode the agent applies the quota it decides on"):
    val world = kitchen(Vector(comfortable))
    val decision = Agent(world, settings(Mode.Auto), new Random(2026)).step()
    decision.applied shouldBe true
    decision.quota shouldBe 4
    world.quotaInForce shouldBe 4

  test("in Advise mode the agent recommends but leaves the quota in force untouched"):
    val world = kitchen(Vector(comfortable), initialQuota = 2)
    val decision = Agent(world, settings(Mode.Advise), new Random(2026)).step()
    decision.applied shouldBe false
    decision.quota shouldBe 4
    world.quotaInForce shouldBe 2

  test("perceives a change in the kitchen and adapts its decision"):
    val degraded = KitchenConditions("degraded", Rates(1.0, 0.5, 0.5, 0.5), Staffing(1, List(1)))
    val world = kitchen(Vector(comfortable, degraded, degraded))
    val agent = Agent(world, settings(Mode.Auto, maxQuota = 6), new Random(2026))
    val rows = AdaptationScenario.run(world, agent, 3)
    rows(0).decision.beliefs.get.config.prepWorkers shouldBe 5
    rows(1).decision.beliefs.get.config.prepWorkers shouldBe 1
    // By period 2 the degraded period's log has reached the agent too.
    rows(2).decision.quota should be < rows(0).decision.quota

  test("the agent sees the previous period's order log but the coming period's staffing report"):
    val slowCook = comfortable.copy(name = "slow cook", rates = comfortable.rates.copy(cookRatePerMinute = 1.0), staffing = Staffing(4, List(5, 5)))
    val world = kitchen(Vector(comfortable, slowCook, slowCook))
    val agent = Agent(world, settings(Mode.Auto).copy(memoryPeriods = 1), new Random(2026))
    val beliefs = AdaptationScenario.run(world, agent, 3).map(_.decision.beliefs.get)
    // Period 1: the staff schedule already says 4 workers, but the log is still period 0's.
    beliefs(1).config.prepWorkers shouldBe 4
    beliefs(1).rates.cookRatePerMinute shouldBe (5.0 +- 1.0)
    // Period 2: the log of period 1 has reached the agent.
    beliefs(2).rates.cookRatePerMinute shouldBe (1.0 +- 0.2)

  test("a reported oven fault is believed from the fornaio's estimate, then from its own logs, and a repair from the working oven's logs"):
    val fault = comfortable.copy(
      name = "fault",
      rates = comfortable.rates.copy(cookRatePerMinute = 1.0),
      staffing = comfortable.staffing.copy(oven = OvenReport("fault", estimatedBakeMinutes = Some(0.5))),
    )
    val world = kitchen(Vector(comfortable, fault, fault, comfortable))
    val agent = Agent(world, settings(Mode.Auto).copy(memoryPeriods = 2), new Random(2026))
    val beliefs = AdaptationScenario.run(world, agent, 4).map(_.decision.beliefs.get)
    beliefs.map(_.bakeRateSource) shouldBe Vector(
      BakeRateSource.SameOvenLogs,
      BakeRateSource.ReportedEstimate,
      BakeRateSource.SameOvenLogs,
      BakeRateSource.SameOvenLogs,
    )
    beliefs(0).rates.cookRatePerMinute shouldBe (5.0 +- 1.0)
    // The fornaio's guess (0.5 minutes, rate 2), not the truth (rate 1), until a log shows it.
    beliefs(1).rates.cookRatePerMinute shouldBe 2.0
    beliefs(2).rates.cookRatePerMinute shouldBe (1.0 +- 0.2)
    // Repaired: back to the working oven's logs at once, though both recent logs are faulty.
    beliefs(3).rates.cookRatePerMinute shouldBe (5.0 +- 1.0)

  test("an oven state with no log and no estimate falls back to every recent log, with an alert"):
    val unknown = comfortable.copy(name = "unknown", staffing = comfortable.staffing.copy(oven = OvenReport("new oven")))
    val world = kitchen(Vector(comfortable, unknown))
    val agent = Agent(world, settings(Mode.Auto), new Random(2026))
    val second = AdaptationScenario.run(world, agent, 2)(1).decision
    second.beliefs.get.bakeRateSource shouldBe BakeRateSource.AnyLogs
    second.alert.get should include("no log or bake-time estimate")

  private val declared = StageTimes(prepMinutes = 0.2, loadingMinutes = 0.2, bakeMinutes = 0.2)

  test("with arrivals-only perception, the arrival rate comes from the log and every stage time from the staff"):
    // The log's stage durations say rate 5 for every stage; the staff declare 1 minute (rate 1).
    val profiled = comfortable.copy(staffing = comfortable.staffing.copy(declaredTimes = Some(declared.copy(bakeMinutes = 1.0))))
    val world = kitchen(Vector(profiled))
    val agent = Agent(world, settings(Mode.Auto).copy(perception = Perception.ArrivalsOnly), new Random(2026))
    val beliefs = agent.step().beliefs.get
    beliefs.bakeRateSource shouldBe BakeRateSource.DeclaredTimes
    beliefs.rates.lambdaPerMinute shouldBe (1.0 +- 0.2)
    beliefs.rates.prepRatePerMinute shouldBe 5.0
    beliefs.rates.cookRatePerMinute shouldBe 1.0

  test("with arrivals-only perception, a report without declared times keeps the previous beliefs, with an alert"):
    val profiled = comfortable.copy(staffing = comfortable.staffing.copy(declaredTimes = Some(declared)))
    val world = kitchen(Vector(profiled, comfortable))
    val agent = Agent(world, settings(Mode.Auto).copy(perception = Perception.ArrivalsOnly), new Random(2026))
    val rows = AdaptationScenario.run(world, agent, 2)
    rows(1).decision.beliefs shouldBe rows(0).decision.beliefs
    rows(1).decision.alert.get should include("declares no stage times")

  test("with arrivals-only perception, a service slower than declared makes the agent plan with longer times, and advise"):
    // The staff declare every stage twice as fast as it is: the agent picks a quota the
    // true kitchen cannot hold, and the next service's clearing time shows it.
    val slow = KitchenConditions("slow", Rates(1.0, 0.5, 0.5, 0.5), Staffing(2, List(2), declaredTimes = Some(StageTimes(1.0, 1.0, 1.0))))
    val world = kitchen(Vector(slow, slow))
    val agent = Agent(world, settings(Mode.Auto, maxQuota = 6).copy(perception = Perception.ArrivalsOnly), new Random(2026))
    val rows = AdaptationScenario.run(world, agent, 2)
    rows(0).decision.beliefs.get.slowdown shouldBe 1.0
    rows(0).decision.alert shouldBe None
    rows(1).decision.beliefs.get.slowdown shouldBe (2.0 +- 0.4)
    rows(1).decision.alert.get should include("slower than the declared stage times")
    rows(1).decision.quota should be < rows(0).decision.quota

  test("with arrivals-only perception, a new declaration supersedes the measured slowdown"):
    val slow = KitchenConditions("slow", Rates(1.0, 0.5, 0.5, 0.5), Staffing(1, List(1), declaredTimes = Some(declared)))
    val redeclared = slow.copy(staffing = slow.staffing.copy(declaredTimes = Some(StageTimes(2.0, 2.0, 2.0))))
    val world = kitchen(Vector(slow, redeclared))
    val agent = Agent(world, settings(Mode.Auto, maxQuota = 6).copy(perception = Perception.ArrivalsOnly), new Random(2026))
    val second = AdaptationScenario.run(world, agent, 2)(1).decision
    second.beliefs.get.slowdown shouldBe 1.0
    second.beliefs.get.rates.prepRatePerMinute shouldBe 0.5

  test("calibration recovers how much slower than declared the kitchen runs"):
    import _root_.kitchen.model.KitchenPipeline.Config
    val config = Config(Route.PrepThenCook, 2, List(3), sizes)
    val declaredTimes = StageTimes(1.0, 0.5, 2.0)
    val service = Slots(15.0, 4)
    val rnd = new Random(11)
    // The true kitchen: every stage 1.5 times its declared time.
    val truth = KitchenModel(config, Rates(1.0, 1 / 1.5, 1 / 0.75, 1 / 3.0))
    val clearing = (1 to 400).map { _ =>
      ServiceSimulation.simulateService(truth, admissionQuota = 4, service, rnd).averageDelayMinutes
    }.sum / 400
    val observed = Calibration.ObservedService(lambdaPerMinute = 1.0, admissionQuota = 4, clearingMinutes = clearing)
    val factor =
      Calibration.slowdownFactor(config, declaredTimes, observed, service, seed = 5, Calibration.Search(runs = 200))
    factor shouldBe (1.5 +- 0.15)

  test("the demand quantile is the share of slots whose whole demand a quota serves"):
    // Single-item orders, 10 expected per slot: the 80% quantile of a Poisson(10) is 13.
    StaffingAdvisor.demandQuantile(1.0, 10.0, Map(1 -> 1.0), 0.8, new Random(3), samples = 20000) shouldBe 13
    // Two-item orders double it.
    StaffingAdvisor.demandQuantile(1.0, 10.0, Map(2 -> 1.0), 0.8, new Random(3), samples = 20000) shouldBe 26

  test("the staffing advice asks for more makers when prep limits the quota, and for none when nothing does"):
    val prepBound = KitchenConditions("prep bound", Rates(1.0, 0.5, 5.0, 5.0), Staffing(1, List(10, 10), declaredTimes = Some(StageTimes(2.0, 0.2, 0.2))))
    val adviceOn = settings(Mode.Auto, maxQuota = 6).copy(perception = Perception.ArrivalsOnly, staffingAdvice = Some(StaffingAdvisor.Settings()))
    val advice = Agent(kitchen(Vector(prepBound)), adviceOn, new Random(2026)).step().staffing.get
    advice.scheduledWorkers shouldBe 1
    advice.recommendedWorkers.get should be > 1
    advice.reliable shouldBe true
    // A one-place oven so slow that no number of makers helps.
    val ovenBound = prepBound.copy(staffing = Staffing(1, List(1), declaredTimes = Some(StageTimes(0.2, 0.2, 10.0))))
    Agent(kitchen(Vector(ovenBound)), adviceOn, new Random(2026)).step().staffing.get.recommendedWorkers shouldBe None

  test("with moving, each period's outcome reports the pizzas moved to a later slot and those lost"):
    import _root_.kitchen.model.KitchenPipeline.Deferral
    val busy = comfortable.copy(rates = comfortable.rates.withArrivals(2.0))
    val world = SimulatedKitchen(
      Vector(busy), setup.copy(deferral = Some(Deferral())), slots, caps, initialQuota = 10, ordersPerPeriod = 200,
      outcomeRuns = 20, new Random(7),
    )
    val (outcome, _) = world.endPeriod()
    // About 30 orders per 15-minute slot against a quota of 10: many are moved, more are lost.
    outcome.averageMovedItems should be > 5.0
    outcome.averageMoveMinutes should (be >= 15.0 and be <= 30.0)
    outcome.averageLostItems should be > outcome.averageMovedItems

  test("each period's outcome counts the services that breached each cap"):
    val (calm, _) = kitchen(Vector(comfortable)).endPeriod()
    calm.services shouldBe 2
    calm.averageCapBreaches shouldBe 0
    calm.worstCapBreaches shouldBe 0
    val (swamped, _) = kitchen(Vector(underpowered), initialQuota = 4).endPeriod()
    swamped.averageCapBreaches shouldBe 2
    swamped.worstCapBreaches shouldBe 2

  test("falls back to the minimum quota and raises an alert when no quota meets the goal"):
    val world = kitchen(Vector(underpowered))
    val decision = Agent(world, settings(Mode.Auto, minQuota = 3, maxQuota = 5), new Random(2026)).step()
    decision.quota shouldBe 3
    decision.evaluation shouldBe None
    decision.alert.get should include("no quota in [3, 5]")
    world.quotaInForce shouldBe 3

  test("keeps its previous beliefs and alerts when a period's log is too small to refit"):
    val staffing = Staffing(5, List(5, 5))
    val rnd = new Random(1)
    def exp(rate: Double) = -math.log(1.0 - rnd.nextDouble()) / rate
    var t = 0.0
    val goodLog = (1 to 200).map { _ =>
      t += exp(1.0)
      StageSample(t, exp(5.0), exp(5.0), exp(5.0))
    }
    val environment = ScriptedEnvironment(
      Seq(Percept(0, goodLog, staffing, None), Percept(1, Seq(StageSample(0, 1, 1, 1)), staffing, None)),
    )
    val agent = Agent(environment, settings(Mode.Auto).copy(memoryPeriods = 1), new Random(2026))
    val first = agent.step()
    val second = agent.step()
    second.beliefs shouldBe first.beliefs
    second.alert.get should include("keeping previous beliefs")
    environment.applied should have size 2

  test("history from before installation fills the memory window from the first decision"):
    val world = kitchen(Vector(comfortable))
    val agent = Agent(world, settings(Mode.Auto).copy(memoryPeriods = 3), new Random(2026), history = world.pastLogs(2))
    // Two past logs plus the one period 0 perceives, 200 orders each, each on its own clock: the
    // arrival rate must still come out near the true 1 per minute, not three times it.
    val rates = agent.step().beliefs.get.rates
    rates.sampleSize shouldBe 600
    rates.lambdaPerMinute shouldBe (1.0 +- 0.15)

  test("fitStagesAcross fits the arrival rate over logs that each start their own clock"):
    val rnd = new Random(99)
    def exp(rate: Double) = -math.log(1.0 - rnd.nextDouble()) / rate
    def log() =
      var t = 0.0
      (1 to 1000).map { _ =>
        t += exp(0.5)
        StageSample(t, exp(2.0), exp(4.0), exp(0.25))
      }
    val logs = Seq(log(), log(), log())
    val rates = RateEstimation.fitStagesAcross(logs)
    rates.lambdaPerMinute shouldBe (0.5 +- 0.03)
    rates.prepRatePerMinute shouldBe (2.0 +- 0.1)
    rates.sampleSize shouldBe 3000
    // Flattening the same logs into one would triple the arrival rate.
    RateEstimation.fitStages(logs.flatten).lambdaPerMinute should be > 1.4
