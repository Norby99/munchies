package kitchen.agent

import java.util.Random

/** Advice on how many pizza makers a service needs. Only the manager can change the staff schedule. */
object StaffingAdvisor:

  /**
   * `recommendedWorkers` is `None` when no staffing reaches `targetQuota` (something else,
   * e.g. the oven, is the limit). `reliable` is false under a calibrated slowdown, which
   * cannot tell a slow maker from a slow oven.
   */
  case class Advice(targetQuota: Int, scheduledWorkers: Int, recommendedWorkers: Option[Int], reliable: Boolean = true)

  /** The target quota serves the whole demand in a fraction `demandServiceLevel` of slots. */
  case class Settings(maxPrepWorkers: Int = 4, demandServiceLevel: Double = 0.8):
    require(maxPrepWorkers > 0, "maxPrepWorkers must be positive")
    require(demandServiceLevel > 0 && demandServiceLevel < 1, "demandServiceLevel must be in (0, 1)")

  /** The `level` quantile of the items one slot asks for, by sampling. */
  def demandQuantile(
      lambdaPerMinute: Double,
      spanLengthMinutes: Double,
      orderSizeProbabilities: Map[Int, Double],
      level: Double,
      rnd: Random,
      samples: Int = 4000,
  ): Int =
    require(level > 0 && level < 1, "level must be in (0, 1)")
    val sizes = orderSizeProbabilities.toVector.sortBy(_._1)
    def drawSize(): Int =
      val u = rnd.nextDouble()
      sizes.scanLeft((0, 0.0)) { case ((_, acc), (size, p)) => (size, acc + p) }.drop(1)
        .find(_._2 > u).map(_._1).getOrElse(sizes.last._1)
    val demands = Vector.fill(samples) {
      var t = -math.log(1.0 - rnd.nextDouble()) / lambdaPerMinute
      var items = 0
      while t <= spanLengthMinutes do
        items += drawSize()
        t += -math.log(1.0 - rnd.nextDouble()) / lambdaPerMinute
      items
    }.sorted
    demands(math.min(samples - 1, math.ceil(level * samples).toInt - 1))

  /** The fewest workers meeting the goal at `targetQuota`; `known` verdicts are not simulated again. */
  def fewestWorkers(
      kitchen: KitchenModel,
      targetQuota: Int,
      maxWorkers: Int,
      assessment: Assessment,
      rnd: Random,
      known: Map[Int, Boolean] = Map.empty,
  ): Option[Int] =
    val judged = assessment.copy(rule = assessment.rule.copy(earlyRejection = true))
    (1 to maxWorkers).find { workers =>
      known.getOrElse(
        workers, {
          val staffed = kitchen.copy(config = kitchen.config.copy(prepWorkers = workers))
          CapacityAgent.evaluate(staffed, targetQuota, judged, rnd).meets(judged.acceptUpTo)
        },
      )
    }
