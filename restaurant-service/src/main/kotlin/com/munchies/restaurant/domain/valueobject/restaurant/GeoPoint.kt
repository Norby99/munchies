package com.munchies.restaurant.domain.valueobject.restaurant

/**
 * A WGS84 coordinate. Used for the restaurant's own location, which scheduler-service needs
 * to measure delivery detours in straight-line kilometres.
 */
data class GeoPoint(
  val lat: Double,
  val lng: Double,
) {
  init {
    require(
      lat.isFinite() && lat in -MAX_LAT..MAX_LAT,
    ) { "lat must be a finite number in [-90, 90]" }
    require(
      lng.isFinite() && lng in -MAX_LNG..MAX_LNG,
    ) { "lng must be a finite number in [-180, 180]" }
  }

  private companion object {
    /** Largest absolute latitude, in degrees (the poles). */
    const val MAX_LAT = 90.0

    /** Largest absolute longitude, in degrees (the antimeridian). */
    const val MAX_LNG = 180.0
  }
}
