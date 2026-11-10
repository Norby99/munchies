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
    require(lat.isFinite() && lat in -90.0..90.0) { "lat must be a finite number in [-90, 90]" }
    require(lng.isFinite() && lng in -180.0..180.0) { "lng must be a finite number in [-180, 180]" }
  }
}
