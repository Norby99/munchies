package kitchen

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import java.util.Random
import u07.utils.Time
import kitchen.model.KitchenPipeline
import kitchen.agent.{
  AdaptationScenario,
  Assessment,
  AutonomousCapacityAgent,
  CapacityAdvisor,
  CapacityAgent,
  DecisionRule,
  Goal,
  HistoricalOrder,
  KitchenConditions,
  KitchenModel,
  PeakProfile,
  ProfiledKitchen,
  RateEstimation,
  SimulatedKitchen,
  Slots,
  StaffingAdvisor,
}

/** The functions exported to Node. The only file that knows it is compiled to JS. */
@JSExportTopLevel("SchedulerAnalysis")
object JsApi:

  @JSExport
  def adviseCapacity(
      lambdaPerMinute: Double,
      muPerMinute: Double,
      candidateCapacity: Int,
      timeHorizonMinutes: Double,
      seed: Double,
      epsilon: Double,
      confidence: Double,
  ): js.Object =
    val advice = Time.timed:
      CapacityAdvisor.adviseCapacity(
        lambdaPerMinute,
        muPerMinute,
        candidateCapacity,
        timeHorizonMinutes,
        new Random(seed.toLong),
        epsilon,
        confidence,
      )
    js.Dynamic.literal(
      candidateCapacity = advice.candidateCapacity,
      lambdaPerMinute = advice.lambdaPerMinute,
      muPerMinute = advice.muPerMinute,
      transientBlockingProbability = advice.transientBlockingProbability,
      epsilon = advice.epsilon,
      confidence = advice.confidence,
      runs = advice.runs,
      steadyStateBlockingProbability = advice.steadyStateBlockingProbability,
    )

  @JSExport
  def erlangBlockingProbability(offeredLoad: Double, capacity: Int): Double =
    CapacityAdvisor.erlangBlockingProbability(offeredLoad, capacity)

  /** `CapacityAgent.recommendCapacity`. The options are typed in `index.d.ts`. */
  @JSExport
  def recommendCapacity(options: js.Dynamic): js.Object =
    val kitchen = KitchenModel(parseConfig(field(options, "kitchen")), parseRates(field(options, "rates")))
    val assessment = parseAssessment(options, DecisionRule())
    val recommendation = Time.timed:
      CapacityAgent.recommendCapacity(kitchen, parseQuotas(options), assessment, seeded(options))
    recommendation match
      case Some(evaluation) =>
        js.Dynamic.literal(
          found = true,
          admissionQuota = evaluation.admissionQuota,
          averageDelayExceedsCapProbability = evaluation.averageDelayExceedsCapProbability,
          worstDelayExceedsCapProbability = evaluation.worstDelayExceedsCapProbability,
          runs = evaluation.runs,
        )
      case None =>
        js.Dynamic.literal(found = false)

  @JSExport
  def fitRates(history: js.Array[js.Dynamic]): js.Object =
    val parsed = history.toSeq.map { h =>
      HistoricalOrder(
        h.startedPrepAtEpochMs.asInstanceOf[Double],
        h.finishedPrepAtEpochMs.asInstanceOf[Double],
      )
    }
    val fitted = RateEstimation.fitRates(parsed)
    js.Dynamic.literal(
      lambdaPerMinute = fitted.lambdaPerMinute,
      muPerMinute = fitted.muPerMinute,
      sampleSize = fitted.sampleSize,
    )

  /**
   * Runs the agent against a `SimulatedKitchen`, one period per phase, alongside the
   * static `world.initialQuota` as a baseline. The options are typed in `index.d.ts`.
   */
  @JSExport
  def runAdaptationScenario(options: js.Dynamic): js.Array[js.Object] =
    val conditions = field[js.Array[js.Dynamic]](options, "phases").toVector.map { p =>
      KitchenConditions(
        field[String](p, "name"),
        parseRates(p),
        AutonomousCapacityAgent.Staffing(
          field[Int](p, "prepWorkers"),
          field[js.Array[Int]](p, "cookStationCapacities").toList,
          AutonomousCapacityAgent.OvenReport(
            optional[String](p, "ovenStatus").getOrElse("normal"),
            optional[Double](p, "estimatedBakeMinutes"),
          ),
          optional[js.Dynamic](p, "declaredMinutes").map(parseDeclaredTimes),
        ),
      )
    }
    val kitchen = parseKitchenSetup(field(options, "kitchen"))
    val assessment = parseAssessment(options, DecisionRule(earlyRejection = true))
    val agentOptions = field[js.Dynamic](options, "agent")
    val memoryPeriods = optional[Int](agentOptions, "memoryPeriods").getOrElse(1)
    val settings = AutonomousCapacityAgent.AgentSettings(
      kitchen,
      assessment,
      field[Int](agentOptions, "minQuota"),
      field[Int](agentOptions, "maxQuota"),
      AutonomousCapacityAgent.Mode.valueOf(field[String](agentOptions, "mode")),
      perception = optional[String](agentOptions, "perception")
        .map(AutonomousCapacityAgent.Perception.valueOf)
        .getOrElse(AutonomousCapacityAgent.Perception.StageLogs),
      memoryPeriods = memoryPeriods,
      staffingAdvice = Option.when(optional[Boolean](agentOptions, "staffingAdvice").getOrElse(false))(
        StaffingAdvisor.Settings(),
      ),
    )
    val world = field[js.Dynamic](options, "world")
    val seed = field[Double](options, "seed").toLong
    // Separate random streams, so the agent's choices do not change what the world draws.
    val simulated = SimulatedKitchen(
      conditions,
      kitchen,
      assessment.slots,
      assessment.caps,
      initialQuota = field[Int](world, "initialQuota"),
      ordersPerPeriod = field[Int](world, "ordersPerPeriod"),
      outcomeRuns = field[Int](world, "outcomeRuns"),
      rnd = new Random(seed),
    )
    // Enough past logs to fill the agent's memory from the first period.
    val agent = AutonomousCapacityAgent.Agent(
      simulated,
      settings,
      new Random(seed + 1),
      history = simulated.pastLogs(memoryPeriods - 1),
    )
    val rows = Time.timed(AdaptationScenario.run(simulated, agent, conditions.size))
    js.Array(rows.map { row =>
      val rates = row.decision.beliefs.map(_.rates)
      js.Dynamic.literal(
        period = row.decision.period,
        conditions = row.conditions.name,
        believedLambdaPerMinute = rates.map(_.lambdaPerMinute).getOrElse(Double.NaN),
        believedPrepRatePerMinute = rates.map(_.prepRatePerMinute).getOrElse(Double.NaN),
        believedCookRatePerMinute = rates.map(_.cookRatePerMinute).getOrElse(Double.NaN),
        believedPrepWorkers = row.decision.beliefs.map(_.config.prepWorkers).getOrElse(0),
        bakeRateSource = row.decision.beliefs.map(_.bakeRateSource.toString).getOrElse(""),
        believedSlowdown = row.decision.beliefs.map(_.slowdown).getOrElse(1.0),
        // 0: no advice requested, or no staffing reaches the target.
        targetQuota = row.decision.staffing.map(_.targetQuota).getOrElse(0),
        recommendedWorkers = row.decision.staffing.flatMap(_.recommendedWorkers).getOrElse(0),
        staffingAdviceReliable = row.decision.staffing.forall(_.reliable),
        recommendedQuota = row.decision.quota,
        applied = row.decision.applied,
        alert = row.decision.alert.getOrElse(""),
        quotaInForce = row.outcome.admissionQuota,
        averageDelayMinutes = row.outcome.averageDelayMinutes,
        worstDelayMinutes = row.outcome.worstDelayMinutes,
        averageCapBreaches = row.outcome.averageCapBreaches,
        worstCapBreaches = row.outcome.worstCapBreaches,
        movedItems = row.outcome.averageMovedItems,
        moveMinutes = row.outcome.averageMoveMinutes,
        lostItems = row.outcome.averageLostItems,
        baselineQuota = row.baseline.admissionQuota,
        baselineAverageDelayMinutes = row.baseline.averageDelayMinutes,
        baselineWorstDelayMinutes = row.baseline.worstDelayMinutes,
        baselineAverageCapBreaches = row.baseline.averageCapBreaches,
        baselineWorstCapBreaches = row.baseline.worstCapBreaches,
        baselineMovedItems = row.baseline.averageMovedItems,
        baselineMoveMinutes = row.baseline.averageMoveMinutes,
        baselineLostItems = row.baseline.averageLostItems,
        services = row.outcome.services,
      ): js.Object
    }*)

  /**
   * The peak-demand proof of concept: plans with a flat model, a per-slot model, and
   * per-slot quotas, then runs each plan on the true kitchen. See `index.d.ts`.
   */
  @JSExport
  def runPeakProfilePoC(options: js.Dynamic): js.Object =
    val ordersPerSlot = field[js.Array[Double]](options, "ordersPerSlot")
    val kitchenOptions = field[js.Dynamic](options, "kitchen")
    val config = parseConfig(kitchenOptions)
    val sizes = config.orderSizeProbabilities
    val slots = Slots(field[Double](options, "slotLengthMinutes"), ordersPerSlot.length)
    val goal = parseGoal(field(options, "goal"))
    val rule = parseRule(options, DecisionRule(earlyRejection = true, safetyMargin = true))
    val quotaOptions = field[js.Dynamic](options, "quotas")
    val quotas = parseQuotas(options)
    val seed = field[Double](options, "seed").toLong
    val outcomeRuns = field[Int](options, "outcomeRuns")

    val trueLambdas = ordersPerSlot.toVector.map(_ / slots.lengthMinutes)
    val worldRnd = new Random(seed)
    val past = Vector.fill(field[Int](options, "pastServices"))(
      PeakProfile.sampleArrivals(trueLambdas, slots.lengthMinutes, worldRnd),
    )
    val fitted = PeakProfile.smooth(PeakProfile.fitProfile(past, slots.lengthMinutes, slots.count))
    val averageLambda = fitted.sum / slots.count
    val kitchen = KitchenModel(config, parseRates(field(options, "rates"), Some(averageLambda)))
    val flatModel = ProfiledKitchen.flat(kitchen, slots)
    val profileModel = ProfiledKitchen(kitchen, fitted, slots.lengthMinutes)
    val truth = ProfiledKitchen(kitchen, trueLambdas, slots.lengthMinutes)
    val agentRnd = new Random(seed + 1)
    def best(model: ProfiledKitchen) =
      PeakProfile
        .bestFlatQuota(model, quotas, goal, rule, agentRnd)
        .getOrElse(PeakProfile.measurePlan(model, Vector.fill(slots.count)(quotas.min), goal.caps, rule.runs, agentRnd))
    val (flat, profile, shaped) = Time.timed {
      val flat = best(flatModel)
      val profile = best(profileModel)
      val shaped = PeakProfile.shapeQuotas(
        profileModel,
        start = profile,
        maxQuota = quotas.max,
        step = field[Int](quotaOptions, "step"),
        goal,
        rule,
        agentRnd,
      )
      (flat, profile, shaped)
    }
    val outcomeSeed = seed + 2
    def plan(name: String, believed: PeakProfile.PlanEvaluation) =
      val actual = PeakProfile.measurePlan(truth, believed.quotas, goal.caps, outcomeRuns, new Random(outcomeSeed))
      js.Dynamic.literal(
        name = name,
        quotas = js.Array(believed.quotas*),
        believedAverageBreach = believed.averageDelayExceedsCapProbability,
        believedWorstBreach = believed.worstDelayExceedsCapProbability,
        believedAdmittedItems = believed.meanAdmittedItems,
        averageCapBreaches = math.round(actual.averageDelayExceedsCapProbability * outcomeRuns).toInt,
        worstCapBreaches = math.round(actual.worstDelayExceedsCapProbability * outcomeRuns).toInt,
        averageDelayMinutes = actual.meanAverageDelayMinutes,
        worstDelayMinutes = actual.meanWorstDelayMinutes,
        admittedItems = actual.meanAdmittedItems,
        movedItems = actual.meanMovedItems,
        lostItems = actual.meanLostItems,
      )
    val meanSize = sizes.map((size, p) => size * p).sum
    js.Dynamic.literal(
      trueOrdersPerSlot = ordersPerSlot,
      fittedOrdersPerSlot = js.Array(fitted.map(_ * slots.lengthMinutes)*),
      demandItems = ordersPerSlot.sum * meanSize,
      runsPerCandidate = rule.runs,
      plans = js.Array(plan("flat model", flat), plan("profile model", profile), plan("per-slot quotas", shaped.plan)),
      searchedQuotas = js.Array(shaped.searched*),
      searchPassedCheck = shaped.passedCheck,
    )

  // Option parsing.

  private def field[A](o: js.Dynamic, name: String): A =
    optional[A](o, name).getOrElse(throw IllegalArgumentException(s"missing option '$name'"))

  private def optional[A](o: js.Dynamic, name: String): Option[A] =
    val v = o.selectDynamic(name)
    Option.when(!js.isUndefined(v) && v != null)(v.asInstanceOf[A])

  private def seeded(options: js.Dynamic): Random = new Random(field[Double](options, "seed").toLong)

  private def parseKitchenSetup(o: js.Dynamic): AutonomousCapacityAgent.KitchenSetup =
    AutonomousCapacityAgent.KitchenSetup(
      parseRoute(field(o, "route")),
      parseOrderSizes(field(o, "orderSizeProbabilities")),
      optional[Int](o, "bakePhases").getOrElse(1),
      optional[js.Dynamic](o, "deferral").map(parseDeferral),
    )

  private def parseConfig(o: js.Dynamic): KitchenPipeline.Config =
    parseKitchenSetup(o).configFor(
      AutonomousCapacityAgent.Staffing(field(o, "prepWorkers"), field[js.Array[Int]](o, "cookStationCapacities").toList),
    )

  /** `lambdaPerMinute`, when given, overrides the field. */
  private def parseRates(o: js.Dynamic, lambdaPerMinute: Option[Double] = None): KitchenPipeline.Rates =
    KitchenPipeline.Rates(
      lambdaPerMinute.getOrElse(field(o, "lambdaPerMinute")),
      field(o, "prepRatePerMinute"),
      field(o, "loadingRatePerMinute"),
      field(o, "cookRatePerMinute"),
    )

  private def parseQuotas(options: js.Dynamic): Range =
    val o = field[js.Dynamic](options, "quotas")
    field[Int](o, "min") to field[Int](o, "max")

  private def parseGoal(o: js.Dynamic): Goal =
    Goal(
      CapacityAgent.DelayCaps(
        field(o, "averageDelayCapMinutes"),
        field(o, "worstDelayCapMinutes"),
        optional[Double](o, "leadTimeMinutes").getOrElse(0.0),
      ),
      field(o, "riskTolerance"),
    )

  /** Missing fields take their value from `defaults`. */
  private def parseRule(options: js.Dynamic, defaults: DecisionRule): DecisionRule =
    optional[js.Dynamic](options, "rule").fold(defaults) { o =>
      DecisionRule(
        optional[Double](o, "epsilon").getOrElse(defaults.epsilon),
        optional[Double](o, "confidence").getOrElse(defaults.confidence),
        optional[Boolean](o, "earlyRejection").getOrElse(defaults.earlyRejection),
        optional[Boolean](o, "safetyMargin").getOrElse(defaults.safetyMargin),
        optional[Boolean](o, "sequentialTest").getOrElse(defaults.sequentialTest),
      )
    }

  private def parseAssessment(options: js.Dynamic, defaultRule: DecisionRule): Assessment =
    val slots = field[js.Dynamic](options, "slots")
    Assessment(
      Slots(field(slots, "lengthMinutes"), field(slots, "count")),
      parseGoal(field(options, "goal")),
      parseRule(options, defaultRule),
    )

  private def parseDeclaredTimes(o: js.Dynamic): AutonomousCapacityAgent.StageTimes =
    AutonomousCapacityAgent.StageTimes(field(o, "prep"), field(o, "loading"), field(o, "bake"))

  private def parseDeferral(o: js.Dynamic): KitchenPipeline.Deferral =
    KitchenPipeline.Deferral(field(o, "acceptOneSlotLater"), field(o, "acceptTwoSlotsLater"))

  private def parseRoute(route: String): KitchenPipeline.Route = route match
    case "PrepThenCook" => KitchenPipeline.Route.PrepThenCook
    case "CookThenPrep" => KitchenPipeline.Route.CookThenPrep
    case other          => throw IllegalArgumentException(s"unknown route: $other (expected PrepThenCook or CookThenPrep)")

  private def parseOrderSizes(probabilities: js.Dictionary[Double]): Map[Int, Double] =
    probabilities.toMap.map { case (size, probability) => size.toInt -> probability }
