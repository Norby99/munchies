package com.munchies.order

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

@MappedEntity
data class TestDocument(
  @field:Id
  @field:GeneratedValue
  var id: String? = null,
  var message: String,
  var timestamp: Long,
)

@MongoRepository
interface TestDocumentRepository : CrudRepository<TestDocument, String>

@Singleton
class MongoStartupWriter(
  private val repository: TestDocumentRepository,
) : ApplicationEventListener<StartupEvent> {
  override fun onApplicationEvent(event: StartupEvent) {
    println("Writing temporary document to MongoDB...")
    try {
      val doc = TestDocument(
        message = "Hello from MongoExample at startup!",
        timestamp = System.currentTimeMillis(),
      )
      val saved = repository.save(doc)
      println("Document written successfully to MongoDB with ID: ${saved.id}")

      val count = repository.count()
      println("Total documents in DB: $count")
    } catch (_: Exception) {
      println("Failed to write to MongoDB: Please check the MongoDB connection.")
    }
  }
}
