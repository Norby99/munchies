package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

/**
 * A tandem pipeline: prep (serial, one item per worker) chained with cook (the same
 * `LaneAutomaton` KitchenStation's stations use, retargeted to pipeline-specific places),
 * with a third, also-serial loading stage in between the two -- placing a batch into a cook station
 * takes a worker's time too, not just the actual prepping. The route is fixed when
 * `Config` is built -- one net models one product/route.
 *
 * Loading always sits directly in front of the cook stations, in either route
 * (`AwaitingCook -> ReadyToBake(station)`), because that's the step that's physically the
 * same regardless of whether cook comes first or second: something has to be carried to
 * the oven/griddle and placed in it either way. Loading needs *two* resources at once,
 * not one: a free worker, from the *same* shared `WorkerFree` pool prep uses (a worker
 * loading a station can't be prepping the next item at that moment), and a free loading
 * slot *at that specific station* (`StationLoadFree(station)`, capped at exactly 1) --
 * two people's hands don't both fit in front of the same oven. With one station, that
 * caps loading at one at a time kitchen-wide; with several, loading at different
 * stations can run in parallel, bounded only by the worker pool.
 *
 * Once a worker claims a station's slot, they hold it -- and the worker -- for a whole
 * continuous session, placing one item after another (each taking real time, at
 * `loadingRatePerMinute`) rather than releasing and re-queuing for the slot after every
 * single item. They only step away (an instant decision, not a physical one) once
 * there's genuinely nothing left to do: no more demand (`AwaitingCook` empty) or no more
 * physical room (`Room(station)` empty) -- the same "keep going until a presence test
 * says stop" shape the cook automaton's own `grab`/`stop` already uses, just applied one
 * step earlier. Checking `Room` here is a read-arc (present in both `cond` and `eff`,
 * netting to no change): loading only *looks* at how much room is left to decide whether
 * continuing is worth it, it doesn't consume it -- that still happens exactly once, in
 * the cook automaton's own `grab`, when it actually pulls an item out of `ReadyToBake`.
 *
 * Admission is a per-span *quota*, not a KitchenStation-style concurrency cap: consumed
 * on admission, never refunded by a completion, reset only externally (`resetAdmission`,
 * between spans) -- a deterministic "every 15 minutes" event has no honest
 * representation among exponential transitions, so that part lives outside the SPN.
 *
 * `Admitted`/`Exit` are monotonic ledgers (never decremented), read directly off the
 * marking (`m(Admitted)`, `m(Exit)`) so a driver can measure delay without coloring
 * tokens.
 */
object KitchenPipeline:

  enum Route:
    /** Dough prepped, then baked -- a pizza. */
    case PrepThenCook
    /** Griddled/fried, then assembled -- a filled piadina or a dressed burger. */
    case CookThenPrep

  enum Lane:
    case Primary, Secondary

  enum Place:
    /** Per-span admission budget; reset only externally, by `resetAdmission`. */
    case Admission
    /** Monotonic ledger: total items ever admitted. */
    case Admitted
    /** Monotonic ledger: total items that have fully left the pipeline. */
    case Exit
    /** Admitted, routed to prep, not yet picked up by a free worker. */
    case AwaitingPrep
    /** Routed to cook (from admission or from a finished prep), not yet picked up by a
     * free worker to be loaded into a station. */
    case AwaitingCook
    /** Loaded into station `station` by a worker; this is what that station's cook
     * automaton actually reads from, not `AwaitingCook` directly. */
    case ReadyToBake(station: Int)
    /** Idle workers, starts at `Config.prepWorkers` -- shared by prepping and loading
     * at every station, not one pool per duty. */
    case WorkerFree
    /** Workers currently busy prepping, one item each. Kept separate from loading's own
     * busy marker (`StationLoading`) even though both draw from the same `WorkerFree`
     * pool: prep's completion rate must scale only by how many are prepping, not by the
     * total busy count across both duties. */
    case WorkerPrepping
    /** The loading slot at cook station `station`: free (1 token) or not (0) -- capped
     * at exactly 1, since only one worker's hands fit in front of a given station at a
     * time. Consumed alongside `WorkerFree` to start loading there, returned alongside
     * it when that load finishes. */
    case StationLoadFree(station: Int)
    /** A worker is mid-session loading station `station`, having already placed at
     * least one item -- always 0 or 1, since `StationLoadFree` caps it; that's why its
     * transitions' rates don't need to scale by an occupancy count the way
     * `WorkerPrepping`'s does. Persists across placing several items in a row, not just
     * one -- released only once demand or room genuinely runs out. */
    case StationLoading(station: Int)
    /** Room left in cook station `station`, shared by both its lanes. */
    case Room(station: Int)
    /** Cook station `station`'s lane `lane`, mid-load with `filled` grabbed so far. */
    case Load(station: Int, lane: Lane, filled: Int)
    /** Cook station `station`'s lane `lane`, baking a committed batch of `size`. */
    case Bake(station: Int, lane: Lane, size: Int)

  export Place.*
  export Lane.*
  export Route.*

  case class Config(
      route: Route,
      prepWorkers: Int,
      cookStationCapacities: List[Int],
      orderSizeProbabilities: Map[Int, Double],
  ):
    require(prepWorkers > 0, "prepWorkers must be positive")
    require(cookStationCapacities.nonEmpty, "cookStationCapacities must not be empty")
    require(cookStationCapacities.forall(_ > 0), "every station's capacity must be positive")
    require(orderSizeProbabilities.nonEmpty, "orderSizeProbabilities must not be empty")
    require(orderSizeProbabilities.keys.forall(_ > 0), "order sizes must be positive")
    require(
      math.abs(orderSizeProbabilities.values.sum - 1.0) < 1e-9,
      "orderSizeProbabilities must sum to 1",
    )

  /** Rate for this file's own bookkeeping transitions (loading's start/stop, prep
   * assignment): far above any realistic lambda/mu, so these resolve in a negligible
   * sliver of simulated time (see `LaneAutomaton`'s own constant of the same name and
   * purpose, used by cook's grab/stop instead). */
  private val LoadRatePerMinute = 1e6

  def spn(
      config: Config,
      lambdaPerMinute: Double,
      prepRatePerMinute: Double,
      loadingRatePerMinute: Double,
      cookRatePerMinute: Double,
  ): SPN.SPN[Place] =
    val (admissionOutput, prepInput, prepOutput, cookOutput) = config.route match
      case Route.PrepThenCook => (AwaitingPrep, AwaitingPrep, AwaitingCook, Exit)
      case Route.CookThenPrep => (AwaitingCook, AwaitingPrep, Exit, AwaitingPrep)

    val admissions: SPN.SPN[Place] = config.orderSizeProbabilities.map{ case (size, probability) =>
      MSet.fill(size)(Admission) ~ (lambdaPerMinute * probability) ~>
        (MSet.fill(size)(admissionOutput) ++ MSet.fill(size)(Admitted))
    }.toSet

    val prep: SPN.SPN[Place] = workerGatedTransitions(prepInput, WorkerPrepping, prepOutput, prepRatePerMinute)

    val loading: SPN.SPN[Place] = config.cookStationCapacities.indices.flatMap { station =>
      loadingTransitions(station, loadingRatePerMinute)
    }.toSet

    val cook: SPN.SPN[Place] = config.cookStationCapacities.zipWithIndex.flatMap { case (capacity, station) =>
      cookLaneTransitions(station, Lane.Primary, ReadyToBake(station), cookOutput,
        capacity, cookRatePerMinute, gatedBy = None) ++
        cookLaneTransitions(
          station,
          Lane.Secondary,
          ReadyToBake(station),
          cookOutput,
          capacity,
          cookRatePerMinute,
          gatedBy = Some(Lane.Primary),
        )
    }.toSet

    admissions ++ prep ++ loading ++ cook

  private def workerGatedTransitions(input: Place, busy: Place, output: Place, ratePerMinute: Double): Set[SPN.Trn[Place]] =
    Set(
      (WorkerFree, input) ~ LoadRatePerMinute ~> busy,
      busy ~ (m => m(busy) * ratePerMinute) ~> (WorkerFree, output),
    )

  // Loading needs a free worker (shared across every station and prep) AND that
  // specific station's own loading slot (capped at 1) at the same time -- a free hand
  // elsewhere doesn't help if this station's one spot is already taken, and vice versa.
  // Once claimed, the worker keeps placing items (each its own real-time step, at
  // ratePerMinute) for as long as there's still demand *and* room, checking the latter
  // via a read-arc (present in both cond and eff of the room place, netting to no
  // change) rather than consuming it -- that happens exactly once, in the cook
  // automaton's own grab. Releasing is instant: noticing there's nothing left to do
  // isn't a physical act the way carrying and placing an item is.
  private def loadingTransitions(station: Int, ratePerMinute: Double): Set[SPN.Trn[Place]] =
    val free = StationLoadFree(station)
    val inProgress = StationLoading(station)
    val readyToBake = ReadyToBake(station)
    val room = Room(station)
    Set(
      (WorkerFree, free, AwaitingCook, room) ~ ratePerMinute ~> (inProgress, room, readyToBake),
      (inProgress, AwaitingCook, room) ~ ratePerMinute ~> (inProgress, room, readyToBake),
      inProgress ~ LoadRatePerMinute ~> (WorkerFree, free) ^^^ AwaitingCook,
      inProgress ~ LoadRatePerMinute ~> (WorkerFree, free) ^^^ room,
    )

  private def cookLaneTransitions(
      station: Int,
      lane: Lane,
      input: Place,
      output: Place,
      capacity: Int,
      ratePerMinute: Double,
      gatedBy: Option[Lane],
  ): Set[SPN.Trn[Place]] =
    val siblingBake = gatedBy.map(sibling => (size: Int) => Bake(station, sibling, size))
    LaneAutomaton.transitions(
      input,
      output,
      room = Room(station),
      load = filled => Load(station, lane, filled),
      bake = size => Bake(station, lane, size),
      capacity,
      ratePerMinute,
      siblingBake,
    )

  def initialMarking(config: Config, admissionQuota: Int): MSet[Place] =
    val admissionTokens = List.fill(admissionQuota)(Admission)
    val workerTokens = List.fill(config.prepWorkers)(WorkerFree)
    val loadSlotTokens = config.cookStationCapacities.indices.map(StationLoadFree.apply).toList
    val roomTokens = config.cookStationCapacities.zipWithIndex.flatMap { case (capacity, s) =>
      List.fill(capacity)(Room(s))
    }
    val cookIdleTokens =
      config.cookStationCapacities.indices.flatMap(s => List(Load(s, Lane.Primary, 0), Load(s, Lane.Secondary, 0))).toList
    MSet.ofList(admissionTokens ++ workerTokens ++ loadSlotTokens ++ roomTokens ++ cookIdleTokens)

  /** Rewrites the admission quota to exactly `quota`, discarding whatever was left --
   * called by the driver at each span boundary, never by anything inside this SPN. */
  def resetAdmission(m: MSet[Place], quota: Int): MSet[Place] =
    MSet.ofMap(m.asMap.updated(Admission, quota))
