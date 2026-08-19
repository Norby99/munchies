package com.munchies.e2e.support

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.Document

class MongoTestSupport(connectionString: String) {
  private val client = MongoClient.create(connectionString)
  private val database = client.getDatabase(connectionString.substringAfterLast("/"))

  suspend fun deleteMany(collection: String, filter: Document) {
    database.getCollection(collection).deleteMany(filter)
  }

  suspend fun findOne(collection: String, filter: Document): Document? =
    database.getCollection<Document>(collection).find(filter).firstOrNull()

  suspend fun findAll(collection: String, filter: Document): List<Document> =
    database.getCollection<Document>(collection).find(filter).toList()

  fun close() = client.close()
}
