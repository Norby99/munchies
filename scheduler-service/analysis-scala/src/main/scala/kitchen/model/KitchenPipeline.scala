package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import scala.util.hashing.MurmurHash3
import kitchen.util.FastMSet
import SpnDsl.*

/**
 * A tandem kitchen as an SPN: prep (one item per worker) and cook (stations of
 * independent places, e.g. the 7 places of a pizza oven), with a loading step in front of
 * the cook stations. Workers are one pool shared by prep and loading.
 *
 * - A bake is Erlang with `bakePhases` phases: same mean, coefficient of variation
 *   `1 / sqrt(bakePhases)`, the usual CTMC approximation of a near-fixed duration.
 * - Admission is a per-slot quota, consumed on admission and reset by the driver between
 *   slots (`advanceSlot`): a fixed clock cannot be expressed with exponential transitions.
 * - `Admitted` and `Exit` only grow, so a driver can measure delay from the marking alone.
 */
object KitchenPipeline:

  enum Route:
    /** Prepped, then baked: a pizza. */
    case PrepThenCook
    /** Cooked, then assembled: a piadina or a burger. */
    case CookThenPrep

  enum Place:
    /** The quota left in the current slot. */
    case Admission
    case Admitted
    case Exit
    case AwaitingPrep
    case AwaitingCook
    case WorkerFree
    /** Separate from `StationLoading`, so that prep completes at a rate proportional to prep workers only. */
    case WorkerPrepping
    /** One worker at a time can load a station. */
    case StationLoadFree(station: Int)
    /** A worker loading `station`; they keep loading until demand or room runs out. */
    case StationLoading(station: Int)
    case Room(station: Int)
    case Baking(station: Int, phase: Int)

    // Used only with `Config.deferral`.
    /** Room left in the next slot and the one after, net of orders already moved there. */
    case RoomNext
    case RoomAfter
    case PendingNext
    case PendingAfter
    case MovedOne
    case MovedTwo
    case Lost
    /** Present while orders are taken; removed at closing. */
    case Open

    /** Content-based, so that a seed gives the same run regardless of identity hash codes. */
    override def hashCode: Int = MurmurHash3.productHash(this)

  export Place.*
  export Route.*

  /**
   * An order that does not fit its slot is offered the next slot with room: one slot later
   * (accepted with `acceptOneSlotLater`), else two (`acceptTwoSlotsLater`), else lost. A
   * moved order is due with its new slot.
   */
  case class Deferral(acceptOneSlotLater: Double = 0.9, acceptTwoSlotsLater: Double = 0.5):
    require(
      Seq(acceptOneSlotLater, acceptTwoSlotsLater).forall(p => p >= 0 && p <= 1),
      "acceptance probabilities must be in [0, 1]",
    )

  /** Rates per minute: order arrivals, and the per-item rates of prep, loading and baking. */
  case class Rates(
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
  ):
    def withArrivals(lambdaPerMinute: Double): Rates = copy(lambdaPerMinute = lambdaPerMinute)

  case class Config(
      route: Route,
      prepWorkers: Int,
      cookStationCapacities: List[Int],
      orderSizeProbabilities: Map[Int, Double],
      bakePhases: Int = 1,
      deferral: Option[Deferral] = None,
  ):
    require(prepWorkers > 0, "prepWorkers must be positive")
    require(bakePhases > 0, "bakePhases must be positive")
    require(cookStationCapacities.nonEmpty, "cookStationCapacities must not be empty")
    require(cookStationCapacities.forall(_ > 0), "every station's capacity must be positive")
    require(orderSizeProbabilities.nonEmpty, "orderSizeProbabilities must not be empty")
    require(orderSizeProbabilities.keys.forall(_ > 0), "order sizes must be positive")
    require(
      math.abs(orderSizeProbabilities.values.sum - 1.0) < 1e-9,
      "orderSizeProbabilities must sum to 1",
    )

  /** For bookkeeping transitions: fast enough to stand in for GSPN immediate transitions. */
  private val LoadRatePerMinute = 1e6

  def spn(config: Config, rates: Rates): SPN.SPN[Place] =
    import rates.*
    val (prepInput, prepOutput, cookOutput) = config.route match
      case Route.PrepThenCook => (AwaitingPrep, AwaitingCook, Exit)
      case Route.CookThenPrep => (AwaitingPrep, Exit, AwaitingPrep)
    val admissionOutput = admissionOutputOf(config.route)

    val admissions: SPN.SPN[Place] = config.orderSizeProbabilities.map { case (size, probability) =>
      MSet.fill(size)(Admission) ~ (lambdaPerMinute * probability) ~>
        (MSet.fill(size)(admissionOutput) ++ MSet.fill(size)(Admitted))
    }.toSet

    val prep: SPN.SPN[Place] = workerGatedTransitions(prepInput, WorkerPrepping, prepOutput, prepRatePerMinute)

    val loading: SPN.SPN[Place] = config.cookStationCapacities.indices.flatMap { station =>
      loadingTransitions(station, loadingRatePerMinute)
    }.toSet

    val cook: SPN.SPN[Place] = config.cookStationCapacities.indices.flatMap { station =>
      bakeTransitions(station, config.bakePhases, cookOutput, cookRatePerMinute)
    }.toSet

    val moves: SPN.SPN[Place] = config.deferral.toSet.flatMap(d => deferralTransitions(config, d, lambdaPerMinute))

    admissions ++ moves ++ prep ++ loading ++ cook

  private def admissionOutputOf(route: Route): Place = route match
    case Route.PrepThenCook => AwaitingPrep
    case Route.CookThenPrep => AwaitingCook

  // One transition per outcome (moved one slot, moved two, lost) and order size. The
  // rates depend on the remaining quota, since "fewer than n tokens" is not an inhibitor arc.
  private def deferralTransitions(config: Config, d: Deferral, lambdaPerMinute: Double): Set[SPN.Trn[Place]] =
    config.orderSizeProbabilities.toSet.flatMap { case (size, probability) =>
      val rate = lambdaPerMinute * probability
      def doesNotFit(m: MSet[Place]) = m(Admission) < size
      Set(
        SPN.Trn(
          MSet.ofList(Open :: List.fill(size)(RoomNext)),
          m => if doesNotFit(m) then rate * d.acceptOneSlotLater else 0.0,
          MSet.ofList(Open :: List.fill(size)(PendingNext) ++ List.fill(size)(MovedOne)),
          MSet(),
        ),
        SPN.Trn(
          MSet.ofList(Open :: List.fill(size)(RoomAfter)),
          m => if doesNotFit(m) && m(RoomNext) < size then rate * d.acceptTwoSlotsLater else 0.0,
          MSet.ofList(Open :: List.fill(size)(PendingAfter) ++ List.fill(size)(MovedTwo)),
          MSet(),
        ),
        SPN.Trn(
          MSet(Open),
          m =>
            if !doesNotFit(m) then 0.0
            else if m(RoomNext) >= size then rate * (1 - d.acceptOneSlotLater)
            else if m(RoomAfter) >= size then rate * (1 - d.acceptTwoSlotsLater)
            else rate,
          MSet.ofList(Open :: List.fill(size)(Lost)),
          MSet(),
        ),
      )
    }

  private def workerGatedTransitions(input: Place, busy: Place, output: Place, ratePerMinute: Double): Set[SPN.Trn[Place]] =
    Set(
      (WorkerFree, input) ~ LoadRatePerMinute ~> busy,
      busy ~ (m => m(busy) * ratePerMinute) ~> (WorkerFree, output),
    )

  // Loading takes a free worker, the station's loading slot and a free place. The worker
  // keeps loading while there is demand and room, then leaves at once.
  private def loadingTransitions(station: Int, ratePerMinute: Double): Set[SPN.Trn[Place]] =
    val free = StationLoadFree(station)
    val inProgress = StationLoading(station)
    val room = Room(station)
    val firstPhase = Baking(station, 0)
    Set(
      (WorkerFree, free, AwaitingCook, room) ~ ratePerMinute ~> (inProgress, firstPhase),
      (inProgress, AwaitingCook, room) ~ ratePerMinute ~> (inProgress, firstPhase),
      inProgress ~ LoadRatePerMinute ~> (WorkerFree, free) ^^^ AwaitingCook,
      inProgress ~ LoadRatePerMinute ~> (WorkerFree, free) ^^^ room,
    )

  // One transition per bake phase; the last one frees the place.
  private def bakeTransitions(station: Int, phases: Int, output: Place, cookRatePerMinute: Double): Set[SPN.Trn[Place]] =
    val phaseRate = phases * cookRatePerMinute
    (0 until phases).map { phase =>
      val baking = Baking(station, phase)
      if phase == phases - 1 then baking ~ (m => m(baking) * phaseRate) ~> (Room(station), output)
      else baking ~ (m => m(baking) * phaseRate) ~> Baking(station, phase + 1)
    }.toSet

  /** A `FastMSet`, so that every marking derived from it during a simulation is one too. */
  def initialMarking(config: Config, admissionQuota: Int): MSet[Place] =
    val admissionTokens = List.fill(admissionQuota)(Admission)
    val workerTokens = List.fill(config.prepWorkers)(WorkerFree)
    val loadSlotTokens = config.cookStationCapacities.indices.map(StationLoadFree.apply).toList
    val roomTokens = config.cookStationCapacities.zipWithIndex.flatMap { case (capacity, s) =>
      List.fill(capacity)(Room(s))
    }
    FastMSet.ofList(admissionTokens ++ workerTokens ++ loadSlotTokens ++ roomTokens)

  def resetAdmission(m: MSet[Place], quota: Int): MSet[Place] =
    FastMSet.ofMap(m.asMap.updated(Admission, quota))

  def serviceStart(config: Config, quotas: IndexedSeq[Int]): MSet[Place] =
    val m = initialMarking(config, quotas(0))
    if config.deferral.isEmpty then m
    else
      FastMSet.ofMap(
        m.asMap ++ Map(RoomNext -> quotas.lift(1).getOrElse(0), RoomAfter -> quotas.lift(2).getOrElse(0), Open -> 1)
          .filter(_._2 > 0),
      )

  /**
   * Opens slot `slot`, or closes the service when `slot == quotas.size`. With deferral,
   * the orders moved to the slot are admitted at once and the room bookkeeping shifts by one.
   */
  def advanceSlot(config: Config, m: MSet[Place], quotas: IndexedSeq[Int], slot: Int): MSet[Place] =
    config.deferral match
      case None => resetAdmission(m, if slot < quotas.size then quotas(slot) else 0)
      case Some(_) =>
        val moved = m(PendingNext)
        val output = admissionOutputOf(config.route)
        val open = slot < quotas.size
        val updated = m.asMap ++ Map(
          Admission -> (if open then m(RoomNext) else 0),
          RoomNext -> (if open then m(RoomAfter) else 0),
          RoomAfter -> (if open then quotas.lift(slot + 2).getOrElse(0) else 0),
          PendingNext -> m(PendingAfter),
          PendingAfter -> 0,
          Admitted -> (m(Admitted) + moved),
          output -> (m(output) + moved),
          Open -> (if open then 1 else 0),
        )
        FastMSet.ofMap(updated.filter(_._2 > 0))
