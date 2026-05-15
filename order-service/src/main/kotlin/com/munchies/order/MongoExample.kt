package com.munchies.order

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

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
  private val logger = LoggerFactory.getLogger(MongoStartupWriter::class.java)

  override fun onApplicationEvent(event: StartupEvent) {
    logger.info("Writing temporary document to MongoDB...")
    @Suppress("TooGenericExceptionCaught")
    try {
      val doc = TestDocument(
        message = "Hello from MongoExample at startup!",
        timestamp = System.currentTimeMillis(),
      )
      val saved = repository.save(doc)
      logger.info("Document written successfully to MongoDB with ID: ${saved.id}")

      val count = repository.count()
      logger.info("Total documents in DB: $count")
    } catch (e: Exception) {
      logger.error("Failed to write to MongoDB: ${e.message}", e)
    }
  }
}
