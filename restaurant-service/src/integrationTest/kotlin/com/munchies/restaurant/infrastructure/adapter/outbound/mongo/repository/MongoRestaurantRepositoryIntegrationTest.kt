package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

/**
 * Integration test: exercises [MongoRestaurantRepository] against a real MongoDB
 * instance (via Testcontainers) to verify the persistence adapter maps the
 * [Restaurant] aggregate correctly to and from the document model. No HTTP layer
 * or use cases are involved - see [MongoMenuRepositoryIntegrationTest] for the
 * sibling test on the menu side.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoRestaurantRepositoryIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var repository: MongoRestaurantRepository

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf(
        "mongodb.uri" to "${mongo.connectionString}/restaurant-service",
        "mongodb.package-names[0]" to
          "com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document",
      ),
      "prod",
    )
    repository = context.getBean(MongoRestaurantRepository::class.java)
  }

  /**
   * Clean up the database after each test to ensure isolation between tests.
   */
  @AfterEach
  fun cleanup() {
    context.getBean(MongoCrudRestaurantRepository::class.java).deleteAll()
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  private fun restaurant(managerId: UserId = UserId()): Restaurant = Restaurant.create(
    managerId = managerId,
    name = RestaurantName.of("Trattoria da Piero"),
    address = Address.of("Via Roma 1, Milano"),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("piero@example.com"),
  )

  @Test
  fun `saves and retrieves a restaurant by id`() {
    val restaurant = restaurant()

    repository.save(restaurant)
    val found = repository.findById(restaurant.id)

    found shouldBe restaurant
    found!!.name.value shouldBe "Trattoria da Piero"
    found.address.value shouldBe "Via Roma 1, Milano"
  }

  @Test
  fun `findById returns null when the restaurant does not exist`() {
    repository.findById(RestaurantId()).shouldBeNull()
  }

  @Test
  fun `findAllByManagerId only returns restaurants owned by that manager`() {
    val managerId = UserId()
    val owned = restaurant(managerId)
    val someoneElses = restaurant(UserId())
    repository.save(owned)
    repository.save(someoneElses)

    val found = repository.findAllByManagerId(managerId)

    found shouldHaveSize 1
    found.first().id shouldBe owned.id
  }

  private fun named(name: String, address: String): Restaurant = Restaurant.create(
    managerId = UserId(),
    name = RestaurantName.of(name),
    address = Address.of(address),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("info@example.com"),
  )

  @Test
  fun `search matches name or address ignoring case, and a blank text matches all`() {
    repository.save(named("Trattoria da Piero", "Via Emilia 10, Forli"))
    repository.save(named("Pizzeria Bella Napoli", "Via Roma 1, Cesena"))
    repository.save(named("Sushi Zen", "Corso Garibaldi 3, FORLI"))

    repository.search("PIZZ").map { it.name.value } shouldBe listOf("Pizzeria Bella Napoli")
    repository.search("forli").map {
      it.name.value
    }.toSet() shouldBe setOf("Trattoria da Piero", "Sushi Zen")
    repository.search("kebab") shouldHaveSize 0
    repository.search("") shouldHaveSize 3
  }

  @Test
  fun `search treats regex characters in the text literally`() {
    repository.save(named("Bar (Centro)", "Via Roma 1"))
    repository.save(named("Bar Centro", "Via Roma 2"))

    repository.search("(centro)").map { it.name.value } shouldBe listOf("Bar (Centro)")
    repository.search(".*") shouldHaveSize 0
  }

  @Test
  fun `update persists changes to the restaurant's details`() {
    val restaurant = restaurant()
    repository.save(restaurant)

    restaurant.updateName(RestaurantName.of("Trattoria da Piero e Figli"))
    repository.update(restaurant)

    repository.findById(restaurant.id)!!.name.value shouldBe "Trattoria da Piero e Figli"
  }

  @Test
  fun `delete removes the restaurant`() {
    val restaurant = restaurant()
    repository.save(restaurant)

    repository.delete(restaurant)

    repository.findById(restaurant.id).shouldBeNull()
  }
}
