package kitchen.agent

import java.util.Random
import scala.util.{Failure, Success, Try}
import kitchen.model.KitchenPipeline
import CapacityAgent.{CandidateEvaluation, DelayCaps}

/**
 * `CapacityAgent` wrapped in a perceive -> decide -> act cycle, run once per period
 * against a `KitchenEnvironment`. Perception fits the model from order logs and staffing
 * reports, the decision is `CapacityAgent.recommendCapacity`, and the action applies the
 * quota (`Mode.Auto`) or only reports it (`Mode.Advise`).
 */
object AutonomousCapacityAgent:

  /** How many past logs the agent keeps: a year of weekly services. */
  val HistoryLimit = 52

  /** The oven's state as reported before a service, with the fornaio's bake-time estimate if any. */
  case class OvenReport(status: String = "normal", estimatedBakeMinutes: Option[Double] = None):
    require(estimatedBakeMinutes.forall(_ > 0), "estimatedBakeMinutes must be positive")

  /** Who and what is in the kitchen for the coming service. */
  case class Staffing(
      prepWorkers: Int,
      cookStationCapacities: List[Int],
      oven: OvenReport = OvenReport(),
      declaredTimes: Option[StageTimes] = None,
  )

  /** Mean minutes per pizza of each stage, as the staff declare them. */
  case class StageTimes(prepMinutes: Double, loadingMinutes: Double, bakeMinutes: Double):
    require(prepMinutes > 0 && loadingMinutes > 0 && bakeMinutes > 0, "declared stage times must be positive")

    /** The model's rates for these times, each stretched by `slowdown`. */
    def rates(lambdaPerMinute: Double, slowdown: Double = 1.0): KitchenPipeline.Rates =
      KitchenPipeline.Rates(
        lambdaPerMinute,
        1.0 / (prepMinutes * slowdown),
        1.0 / (loadingMinutes * slowdown),
        1.0 / (bakeMinutes * slowdown),
      )

  /** The facts about the kitchen that do not change between services. */
  case class KitchenSetup(
      route: KitchenPipeline.Route,
      orderSizeProbabilities: Map[Int, Double],
      bakePhases: Int = 1,
      deferral: Option[KitchenPipeline.Deferral] = None,
  ):
    def configFor(staffing: Staffing): KitchenPipeline.Config =
      KitchenPipeline.Config(
        route,
        staffing.prepWorkers,
        staffing.cookStationCapacities,
        orderSizeProbabilities,
        bakePhases,
        deferral,
      )

  /** What the agent learns from the order logs. */
  enum Perception:
    /** Every rate, from per-stage timings. Only a simulated kitchen logs these. */
    case StageLogs
    /** Only the arrival rate; stage times are the declared ones, corrected by `Calibration`. */
    case ArrivalsOnly

  /** Where the believed bake rate came from. */
  enum BakeRateSource:
    case SameOvenLogs, ReportedEstimate, AnyLogs, DeclaredTimes

  /**
   * How the services of a period went under the quota in force, over `services`
   * simulated services. `averageClearingMinutes` is how long after a slot's end its last
   * order was ready, which is what calibration compares with the model.
   */
  case class ServiceOutcome(
      admissionQuota: Int,
      averageDelayMinutes: Double,
      worstDelayMinutes: Double,
      averageCapBreaches: Int,
      worstCapBreaches: Int,
      services: Int,
      averageClearingMinutes: Double,
      averageMovedItems: Double = 0.0,
      averageMoveMinutes: Double = 0.0,
      averageLostItems: Double = 0.0,
  )

  case class Percept(
      period: Int,
      orderLog: Seq[StageSample],
      staffing: Staffing,
      lastService: Option[ServiceOutcome],
  )

  /** The agent's view of its environment: one sensor and one actuator. */
  trait KitchenEnvironment:
    def perceive(): Percept
    def applyQuota(quota: Int): Unit

  enum Mode:
    case Advise, Auto

  /**
   * `memoryPeriods`: how many recent logs the beliefs are fitted from.
   * `slowdownThreshold`: the smallest calibrated slowdown the agent acts on.
   */
  case class AgentSettings(
      kitchen: KitchenSetup,
      assessment: Assessment,
      minQuota: Int,
      maxQuota: Int,
      mode: Mode,
      perception: Perception = Perception.StageLogs,
      memoryPeriods: Int = 1,
      slowdownThreshold: Double = 1.05,
      staffingAdvice: Option[StaffingAdvisor.Settings] = None,
  ):
    require(minQuota > 0 && minQuota <= maxQuota, "need 0 < minQuota <= maxQuota")
    require(memoryPeriods > 0, "memoryPeriods must be positive")
    def quotas: Range = minQuota to maxQuota

  /** The model the agent will simulate. `slowdown` is already included in `rates`. */
  case class Beliefs(
      rates: FittedStageRates,
      config: KitchenPipeline.Config,
      bakeRateSource: BakeRateSource,
      slowdown: Double = 1.0,
  ):
    def kitchen: KitchenModel = KitchenModel(config, rates.toRates)

  case class Decision(
      period: Int,
      beliefs: Option[Beliefs],
      quota: Int,
      evaluation: Option[CandidateEvaluation],
      applied: Boolean,
      alert: Option[String],
      staffing: Option[StaffingAdvisor.Advice] = None,
  )

  /** `history`: order logs from before the agent was installed, oldest first. */
  class Agent(
      environment: KitchenEnvironment,
      settings: AgentSettings,
      rnd: Random,
      history: Seq[Seq[StageSample]] = Nil,
  ):
    // Past logs, each tagged with the oven state reported for its service.
    private var memory = Vector.empty[(String, Seq[StageSample])]
    private var untaggedHistory = history.toVector
    private var reportedOvenStatus: Option[String] = None
    private var beliefs: Option[Beliefs] = None
    private var lastQuota = settings.minQuota
    // ArrivalsOnly: the staffing of the service whose outcome arrives next, and the last
    // measured slowdown with the declaration it was measured against.
    private var lastStaffing: Option[Staffing] = None
    private var slowdown: Option[(Double, StageTimes)] = None

    def step(): Decision =
      val percept = environment.perceive()
      if settings.perception == Perception.ArrivalsOnly then calibrate(percept)
      lastStaffing = Some(percept.staffing)
      val (newBeliefs, perceptionAlert) = revise(percept)
      beliefs = newBeliefs.orElse(beliefs)

      beliefs match
        case None =>
          Decision(percept.period, None, lastQuota, None, applied = false, perceptionAlert)
        case Some(current) =>
          val (quota, evaluation, decisionAlert) = decide(current)
          val applied = settings.mode == Mode.Auto
          if applied then environment.applyQuota(quota)
          lastQuota = quota
          Decision(
            percept.period,
            Some(current),
            quota,
            evaluation,
            applied,
            (perceptionAlert.toList ++ decisionAlert.toList).reduceOption(_ + "; " + _),
            settings.staffingAdvice.map(adviseStaffing(_, current, evaluation)),
          )

    /**
     * Measures how much slower than declared the last service ran. Only a slowdown is
     * acted on, never a speed-up: a quota lowered by mistake costs throughput, one raised
     * by mistake costs late orders.
     */
    private def calibrate(percept: Percept): Unit =
      val measured = for
        outcome <- percept.lastService
        staffing <- lastStaffing
        declared <- staffing.declaredTimes
        config <- pipelineConfig(staffing).toOption
        (lambda, _) <- Try(RateEstimation.arrivalRateAcross(Seq(percept.orderLog))).toOption
      yield
        val observed = Calibration.ObservedService(lambda, outcome.admissionQuota, outcome.averageClearingMinutes)
        val factor = Calibration.slowdownFactor(config, declared, observed, slots, rnd.nextLong())
        (factor, declared)
      slowdown = measured.filter(_._1 >= settings.slowdownThreshold)

    private def adviseStaffing(
        advice: StaffingAdvisor.Settings,
        current: Beliefs,
        evaluation: Option[CandidateEvaluation],
    ): StaffingAdvisor.Advice =
      val target = math.min(
        settings.maxQuota,
        StaffingAdvisor.demandQuantile(
          current.rates.lambdaPerMinute,
          slots.lengthMinutes,
          settings.kitchen.orderSizeProbabilities,
          advice.demandServiceLevel,
          rnd,
        ),
      )
      val scheduled = current.config.prepWorkers
      // The quota search already answered for the scheduled staffing.
      val known = Map(scheduled -> evaluation.exists(_.admissionQuota >= target))
      val workers =
        StaffingAdvisor.fewestWorkers(current.kitchen, target, advice.maxPrepWorkers, settings.assessment, rnd, known)
      StaffingAdvisor.Advice(target, scheduled, workers, reliable = current.slowdown == 1.0)

    private def slots: Slots = settings.assessment.slots

    private def revise(percept: Percept): (Option[Beliefs], Option[String]) =
      settings.perception match
        case Perception.StageLogs    => reviseFromStageLogs(percept)
        case Perception.ArrivalsOnly => reviseFromArrivals(percept)

    /** Arrival rate from the recent logs; stage times as declared, times the measured slowdown. */
    private def reviseFromArrivals(percept: Percept): (Option[Beliefs], Option[String]) =
      memory = (memory ++ untaggedHistory.map(percept.staffing.oven.status -> _) :+
        (percept.staffing.oven.status -> percept.orderLog)).takeRight(HistoryLimit)
      untaggedHistory = Vector.empty
      val recent = memory.takeRight(settings.memoryPeriods).map(_._2)
      val fitted = for
        declared <- percept.staffing.declaredTimes
          .toRight(IllegalArgumentException("the staffing report declares no stage times"))
          .toTry
        (lambda, sampleSize) <- Try(RateEstimation.arrivalRateAcross(recent))
      yield
        // A new declaration supersedes the slowdown measured against the old one.
        val factor = slowdown.collect { case (f, basis) if basis == declared => f }.getOrElse(1.0)
        (FittedStageRates(declared.rates(lambda, factor), sampleSize), factor)
      val advice = fitted.toOption.collect { case (_, factor) if factor > 1.0 =>
        f"the last service ran slower than the declared stage times explain: planning with times ${(factor - 1) * 100}%.0f%% longer; check the declared times"
      }
      (fitted, pipelineConfig(percept.staffing)) match
        case (Success((rates, factor)), Success(cfg)) =>
          (Some(Beliefs(rates, cfg, BakeRateSource.DeclaredTimes, factor)), advice)
        case (Failure(e), _)                => (None, Some(s"could not refit rates, keeping previous beliefs: ${e.getMessage}"))
        case (_, Failure(e))                => (None, Some(s"unusable staffing report, keeping previous beliefs: ${e.getMessage}"))

    private def pipelineConfig(staffing: Staffing): Try[KitchenPipeline.Config] =
      Try(settings.kitchen.configFor(staffing))

    /**
     * Every rate from the recent logs, except the bake rate, which comes from logs
     * recorded with the oven in its currently reported state. After a repair the agent
     * thus goes straight back to the working oven's rate.
     */
    private def reviseFromStageLogs(percept: Percept): (Option[Beliefs], Option[String]) =
      val oven = percept.staffing.oven
      val loggedStatus = reportedOvenStatus.getOrElse(oven.status)
      memory = (memory ++ untaggedHistory.map(loggedStatus -> _) :+ (loggedStatus -> percept.orderLog))
        .takeRight(HistoryLimit)
      untaggedHistory = Vector.empty
      reportedOvenStatus = Some(oven.status)
      val recent = memory.takeRight(settings.memoryPeriods).map(_._2)
      val sameOven = memory.filter(_._1 == oven.status).takeRight(settings.memoryPeriods).map(_._2)
      val fitted = Try(RateEstimation.fitStagesAcross(recent)).map { rates =>
        Try(RateEstimation.fitStagesAcross(sameOven)).toOption match
          case Some(sameOvenRates) =>
            (rates.copy(cookRatePerMinute = sameOvenRates.cookRatePerMinute), BakeRateSource.SameOvenLogs, None)
          case None =>
            oven.estimatedBakeMinutes match
              case Some(minutes) => (rates.copy(cookRatePerMinute = 1.0 / minutes), BakeRateSource.ReportedEstimate, None)
              case None =>
                val alert = s"oven reported as '${oven.status}', with no log or bake-time estimate for it: using every recent log"
                (rates, BakeRateSource.AnyLogs, Some(alert))
      }
      (fitted, pipelineConfig(percept.staffing)) match
        case (Success((rates, source, alert)), Success(cfg)) => (Some(Beliefs(rates, cfg, source)), alert)
        case (Failure(e), _)                => (None, Some(s"could not refit rates, keeping previous beliefs: ${e.getMessage}"))
        case (_, Failure(e))                => (None, Some(s"unusable staffing report, keeping previous beliefs: ${e.getMessage}"))

    /** The largest quota meeting the goal, or the minimum quota with an alert if none does. */
    private def decide(current: Beliefs): (Int, Option[CandidateEvaluation], Option[String]) =
      CapacityAgent.recommendCapacity(current.kitchen, settings.quotas, settings.assessment, rnd) match
        case Some(evaluation) => (evaluation.admissionQuota, Some(evaluation), None)
        case None =>
          (
            settings.minQuota,
            None,
            Some(s"no quota in [${settings.minQuota}, ${settings.maxQuota}] meets the delay goal; falling back to the minimum"),
          )
