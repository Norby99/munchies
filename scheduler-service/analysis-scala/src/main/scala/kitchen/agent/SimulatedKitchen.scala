package kitchen.agent

import java.util.Random
import kitchen.model.KitchenPipeline
import AutonomousCapacityAgent.*
import CapacityAgent.DelayCaps

/** The kitchen's true conditions during one phase. The agent sees only their logs and reports. */
case class KitchenConditions(name: String, rates: KitchenPipeline.Rates, staffing: Staffing)

/**
 * A simulated restaurant, one period per phase. Before a period's service the agent sees
 * the previous period's log and the coming period's staffing, so demand changes reach it
 * one period late and staffing changes in time. Every period is also run under the static
 * `initialQuota`, with the same random numbers, as a baseline.
 */
class SimulatedKitchen(
    phases: Vector[KitchenConditions],
    kitchen: KitchenSetup,
    slots: Slots,
    caps: DelayCaps,
    initialQuota: Int,
    ordersPerPeriod: Int,
    outcomeRuns: Int,
    rnd: Random,
) extends KitchenEnvironment:
  require(phases.nonEmpty, "need at least one phase")

  private val logRnd = new Random(rnd.nextLong())
  private val outcomeRnd = new Random(rnd.nextLong())
  private var period = 0
  private var quota = initialQuota
  private var lastOutcome: Option[ServiceOutcome] = None
  private var lastLog: Seq[StageSample] = sampleLog(phases.head)

  def conditions: KitchenConditions = phases(math.min(period, phases.size - 1))
  def quotaInForce: Int = quota

  override def perceive(): Percept =
    Percept(period, lastLog, conditions.staffing, lastOutcome)

  /** Logs of `periods` services before the scenario, under the first phase's conditions. */
  def pastLogs(periods: Int): Vector[Seq[StageSample]] =
    Vector.fill(periods)(sampleLog(phases.head))

  override def applyQuota(newQuota: Int): Unit =
    require(newQuota > 0, "quota must be positive")
    quota = newQuota

  /** Runs this period under the quota in force and under the baseline, then advances. */
  def endPeriod(): (ServiceOutcome, ServiceOutcome) =
    val periodSeed = outcomeRnd.nextLong()
    val outcome = measure(quota, new Random(periodSeed))
    val baseline = measure(initialQuota, new Random(periodSeed))
    lastOutcome = Some(outcome)
    lastLog = sampleLog(conditions)
    period += 1
    (outcome, baseline)

  private def measure(quotaToRun: Int, runRnd: Random): ServiceOutcome =
    val c = conditions
    val model = KitchenModel(kitchen.configFor(c.staffing), c.rates)
    val raw = (1 to outcomeRuns).map(_ => ServiceSimulation.simulateService(model, quotaToRun, slots, runRnd))
    val results = raw.map(_.withLeadTime(caps.leadTimeMinutes))
    ServiceOutcome(
      quotaToRun,
      results.map(_.averageDelayMinutes).sum / outcomeRuns,
      results.map(_.worstDelayMinutes).sum / outcomeRuns,
      results.count(_.averageDelayMinutes > caps.averageDelayCapMinutes),
      results.count(_.worstDelayMinutes > caps.worstDelayCapMinutes),
      outcomeRuns,
      raw.map(_.averageDelayMinutes).sum / outcomeRuns,
      raw.map(r => r.movedOneSlotItems + r.movedTwoSlotsItems).sum.toDouble / outcomeRuns,
      {
        val moved = raw.map(r => r.movedOneSlotItems + r.movedTwoSlotsItems).sum
        val minutes = raw.map(r => (r.movedOneSlotItems + 2 * r.movedTwoSlotsItems) * slots.lengthMinutes).sum
        if moved == 0 then 0.0 else minutes / moved
      },
      raw.map(_.lostItems).sum.toDouble / outcomeRuns,
    )

  private def exponential(ratePerMinute: Double): Double = -math.log(1.0 - logRnd.nextDouble()) / ratePerMinute

  private def bake(cookRatePerMinute: Double): Double =
    (1 to kitchen.bakePhases).map(_ => exponential(kitchen.bakePhases * cookRatePerMinute)).sum

  private def sampleLog(c: KitchenConditions): Seq[StageSample] =
    val r = c.rates
    var t = 0.0
    (1 to ordersPerPeriod).map { _ =>
      t += exponential(r.lambdaPerMinute)
      StageSample(t, exponential(r.prepRatePerMinute), exponential(r.loadingRatePerMinute), bake(r.cookRatePerMinute))
    }

/** Runs an agent against a `SimulatedKitchen` for one period per phase. */
object AdaptationScenario:

  /** `baseline`: the same period under the kitchen's initial, static quota. */
  case class Row(conditions: KitchenConditions, decision: Decision, outcome: ServiceOutcome, baseline: ServiceOutcome)

  def run(kitchen: SimulatedKitchen, agent: Agent, periods: Int): Vector[Row] =
    (0 until periods).map { _ =>
      val conditions = kitchen.conditions
      val decision = agent.step()
      val (outcome, baseline) = kitchen.endPeriod()
      Row(conditions, decision, outcome, baseline)
    }.toVector
