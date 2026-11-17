package com.munchies.restaurant.domain.valueobject.restaurant

/**
 * The order in which one item goes through the kitchen's two stages.
 *
 * The capacity model of scheduler-service (`analysis-scala`, `KitchenPipeline.Route`) describes one
 * product's route, so a restaurant declares the route most of its items follow.
 */
enum class KitchenRoute {
  /** Prepared by hand, then cooked: a pizza is topped, then baked. */
  PREP_THEN_COOK,

  /** Cooked, then assembled: a burger's patty is grilled, then the burger is built. */
  COOK_THEN_PREP,
}

/**
 * A place where items are cooked, such as an oven or a grill.
 *
 * @property name The label the staff use for it, unique within the kitchen (ignoring case).
 * @property places How many items it cooks at the same time, each independently of the others (a
 * pizza oven with room for 7 pizzas has 7 places).
 */
data class CookStation(
  val name: String,
  val places: Int,
) {
  init {
    require(name.isNotBlank()) { "station name cannot be blank" }
    require(name.length <= KitchenSetup.MAX_NAME_LENGTH) {
      "station name cannot exceed ${KitchenSetup.MAX_NAME_LENGTH} characters"
    }
    require(places in 1..KitchenSetup.MAX_STATION_PLACES) {
      "station \"$name\" must have between 1 and ${KitchenSetup.MAX_STATION_PLACES} places"
    }
  }
}

/**
 * The kitchen of a restaurant as its staff describe it: who works in it, what it cooks with, and
 * how long each stage takes for one item.
 *
 * It is input for the capacity agent of scheduler-service (`analysis-scala`,
 * `AutonomousCapacityAgent`), which simulates the kitchen to choose how many items one scheduling
 * slot may admit. [prepWorkers] and [cookStations] are the agent's `Staffing`; the three stage
 * times are its `StageTimes` (with [cookMinutes] as the bake time). The agent fits the demand from
 * the order history itself, and corrects these times when the kitchen runs slower than declared.
 *
 * Times are means for one item (one kitchen unit), in minutes.
 *
 * @property route The order of the two stages.
 * @property prepWorkers How many people prepare items; each prepares one item at a time.
 * @property cookStations Where items are cooked; at least one.
 * @property prepMinutes How long one worker takes to prepare one item.
 * @property loadingMinutes How long a worker takes to put one item into a cook station (a pizza
 * into the oven); the worker is busy meanwhile.
 * @property cookMinutes How long one item cooks.
 */
data class KitchenSetup(
  val route: KitchenRoute,
  val prepWorkers: Int,
  val cookStations: List<CookStation>,
  val prepMinutes: Double,
  val loadingMinutes: Double,
  val cookMinutes: Double,
) {
  init {
    require(prepWorkers in 1..MAX_PREP_WORKERS) {
      "prepWorkers must be between 1 and $MAX_PREP_WORKERS"
    }
    require(cookStations.isNotEmpty()) { "the kitchen needs at least one cook station" }
    require(cookStations.size <= MAX_COOK_STATIONS) {
      "the kitchen cannot have more than $MAX_COOK_STATIONS cook stations"
    }
    val duplicatedNames = cookStations.groupingBy { it.name.trim().lowercase() }.eachCount()
      .filterValues { it > 1 }.keys
    require(duplicatedNames.isEmpty()) { "duplicated station names: ${duplicatedNames.sorted()}" }
    requireStageTime("prepMinutes", prepMinutes)
    requireStageTime("loadingMinutes", loadingMinutes)
    requireStageTime("cookMinutes", cookMinutes)
  }

  private fun requireStageTime(name: String, minutes: Double) {
    // Zero is refused too: the model turns each time into a rate (1 / minutes).
    require(minutes.isFinite() && minutes > 0 && minutes <= MAX_STAGE_MINUTES) {
      "$name must be greater than 0 and at most $MAX_STAGE_MINUTES"
    }
  }

  companion object {
    /** Longest accepted station name. */
    const val MAX_NAME_LENGTH = 60

    /** Upper bounds that keep the simulated kitchen (one Petri net place per station) small. */
    const val MAX_PREP_WORKERS = 50
    const val MAX_COOK_STATIONS = 20
    const val MAX_STATION_PLACES = 100

    /** Longest accepted stage time: four hours for one item is already a typing mistake. */
    const val MAX_STAGE_MINUTES = 240.0
  }
}
