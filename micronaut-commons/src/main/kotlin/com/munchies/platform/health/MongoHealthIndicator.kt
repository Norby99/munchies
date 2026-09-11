package com.munchies.platform.health

import com.mongodb.client.MongoClient
import io.micronaut.context.annotation.Requires
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.AbstractHealthIndicator
import jakarta.inject.Singleton
import org.bson.Document

/**
 * Reports MongoDB connectivity on the `/health` endpoint.
 *
 * Micronaut only ships a Mongo health indicator for the reactive driver; these services use
 * `micronaut-data-mongodb` on the synchronous driver, so `/health` would otherwise never reflect
 * the database. This runs a lightweight `ping` against the already-wired sync [MongoClient].
 *
 * Disable with `endpoints.health.mongodb.enabled: false`.
 */
@Singleton
@Requires(beans = [MongoClient::class])
@Requires(property = "endpoints.health.mongodb.enabled", notEquals = "false")
class MongoHealthIndicator(
  private val mongoClient: MongoClient,
) : AbstractHealthIndicator<Map<String, Any>>() {

  override fun getName(): String = "mongodb"

  override fun getHealthInformation(): Map<String, Any> {
    val result = mongoClient.getDatabase("admin").runCommand(Document("ping", 1))
    healthStatus = HealthStatus.UP
    return mapOf("ping" to (result["ok"] ?: 1))
  }
}
