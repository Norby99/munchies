package com.munchies.restaurant.domain.valueobject.restaurant

/**
 * A delivery zone drawn by the manager on the map: an area whose addresses may share a driver.
 *
 * It is output configuration consumed by scheduler-service. The scheduler measures straight-line
 * distances, so two addresses that look close can still be far apart by road (a river, a railway
 * or a ring road between them). Zones carry the manager's local knowledge: when a restaurant has
 * zones, a delivery may join an existing trip only if every stop of that trip, and the new address,
 * lie inside at least one common zone. Detour and trip budget still apply on top. Zones may
 * overlap. They do not restrict where the restaurant delivers: an address outside every zone is
 * still delivered, on a trip of its own.
 *
 * @property id Stable identifier of the zone, unique within the restaurant.
 * @property name The label the manager gives the zone, unique within the restaurant (ignoring case).
 * @property boundary The outer ring of the polygon, in drawing order, not closed (the last vertex
 * is implicitly joined to the first). Holes are not supported.
 */
data class DeliveryZone(
  val id: String,
  val name: String,
  val boundary: List<GeoPoint>,
) {
  init {
    require(id.isNotBlank()) { "zone id cannot be blank" }
    require(name.isNotBlank()) { "zone name cannot be blank" }
    require(name.length <= MAX_NAME_LENGTH) {
      "zone name cannot exceed $MAX_NAME_LENGTH characters"
    }
    require(boundary.size <= MAX_VERTICES) {
      "zone \"$name\" cannot have more than $MAX_VERTICES vertices"
    }
    require(boundary.distinct().size >= MIN_VERTICES) {
      "zone \"$name\" needs at least $MIN_VERTICES distinct vertices"
    }
  }

  companion object {
    /** Longest accepted zone name. */
    const val MAX_NAME_LENGTH = 60

    /** A polygon needs at least three distinct corners to enclose an area. */
    const val MIN_VERTICES = 3

    /** Upper bound on the vertices of one zone, to keep documents and point-in-polygon checks small. */
    const val MAX_VERTICES = 500
  }
}
