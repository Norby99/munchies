package kitchen.model

import u07.utils.MSet
import u07.modeling.SPN
import SpnDsl.*

/**
 * Generalizes KitchenQueue to stations that batch-serve up to `stationCapacity` items
 * together (an oven, a fry basket), fed by batched arrivals. `stationCapacity = 1` is
 * exactly KitchenQueue (a grill, one item at a time).
 *
 * Policy: always prefer a full load; accept a partial load only when demand runs out
 * first. A station that commits to a partial bake may open a second, independent lane
 * for one later arrival into the leftover room -- never a third, since a lane only opens
 * once the sibling has already committed to baking. The per-lane grab/stop/complete
 * automaton itself is shared with KitchenPipeline's cook stage -- see `LaneAutomaton`
 * for how it's built and why it has that shape.
 */
object KitchenStation:

  enum Lane:
    case Primary, Secondary

  enum Place:
    /** Admitted but not yet assigned to a station. */
    case Waiting
    /** Admission budget, `stations * stationCapacity` tokens; exhausted means the next
     * order is lost outright, not queued. */
    case Capacity
    /** Room left in station `station`, shared by both its lanes. */
    case Room(station: Int)
    /** Lane `lane` of station `station`, mid-load with `filled` grabbed so far;
     * `filled = 0` means idle. */
    case Load(station: Int, lane: Lane, filled: Int)
    /** Lane `lane` of station `station`, baking a committed batch of `size`. */
    case Bake(station: Int, lane: Lane, size: Int)

  export Place.*
  export Lane.*

  case class Config(stations: Int, stationCapacity: Int, orderSizeProbabilities: Map[Int, Double]):
    require(stations > 0, "stations must be positive")
    require(stationCapacity > 0, "stationCapacity must be positive")
    require(orderSizeProbabilities.nonEmpty, "orderSizeProbabilities must not be empty")
    require(orderSizeProbabilities.keys.forall(_ > 0), "order sizes must be positive")
    require(
      math.abs(orderSizeProbabilities.values.sum - 1.0) < 1e-9,
      "orderSizeProbabilities must sum to 1",
    )

  def spn(config: Config, lambdaPerMinute: Double, muPerMinute: Double): SPN.SPN[Place] =
    val arrivals: SPN.SPN[Place] = config.orderSizeProbabilities.map {
      case (size, probability) =>
        MSet.fill(size)(Capacity) ~ (lambdaPerMinute * probability) ~> MSet.fill(size)(Waiting)
    }.toSet

    val stations: SPN.SPN[Place] = (0 until config.stations).flatMap { station =>
      laneTransitions(station, Lane.Primary, config.stationCapacity, muPerMinute, gatedBy = None) ++
        laneTransitions(station, Lane.Secondary, config.stationCapacity, muPerMinute, gatedBy = Some(Lane.Primary))
    }.toSet

    arrivals ++ stations

  private def laneTransitions(
      station: Int,
      lane: Lane,
      capacity: Int,
      muPerMinute: Double,
      gatedBy: Option[Lane],
  ): Set[SPN.Trn[Place]] =
    val siblingBake = gatedBy.map(sibling => (size: Int) => Bake(station, sibling, size))
    LaneAutomaton.transitions(
      input = Waiting,
      output = Capacity,
      room = Room(station),
      load = filled => Load(station, lane, filled),
      bake = size => Bake(station, lane, size),
      capacity,
      muPerMinute,
      siblingBake,
    )

  def initialMarking(config: Config): MSet[Place] =
    val capacityTokens = List.fill(config.stations * config.stationCapacity)(Capacity)
    val roomTokens = (0 until config.stations).flatMap(s => List.fill(config.stationCapacity)(Room(s))).toList
    val idleTokens =
      (0 until config.stations).flatMap(s => List(Load(s, Lane.Primary, 0), Load(s, Lane.Secondary, 0))).toList
    MSet.ofList(capacityTokens ++ roomTokens ++ idleTokens)

  /** The kitchen has no room left to admit a new order, of any size. */
  def isAtCapacity(m: MSet[Place]): Boolean = m(Capacity) == 0

  /** Total items currently baking across every station and lane (not counting `Waiting`). */
  def occupancy(m: MSet[Place]): Int =
    m.asMap.collect { case (Bake(_, _, size), count) if count > 0 => size }.sum
