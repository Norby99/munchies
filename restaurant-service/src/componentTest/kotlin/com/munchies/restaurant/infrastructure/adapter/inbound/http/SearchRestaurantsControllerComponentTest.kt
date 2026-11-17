package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.SearchRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoRestaurantRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Component test of `GET /restaurants/?q=`, the customer search, against a real MongoDB. */
@MicronautTest(environments = ["prod"], transactional = false)
class SearchRestaurantsControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  private fun restaurant(name: String, address: String): Restaurant = Restaurant.create(
    managerId = UserId(),
    name = RestaurantName.of(name),
    address = Address.of(address),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("info@example.com"),
  )

  @BeforeEach
  fun seed() {
    restaurantRepository.save(restaurant("Trattoria da Piero", "Via Emilia 10, Forli"))
    restaurantRepository.save(restaurant("Pizzeria Bella Napoli", "Via Roma 1, Cesena"))
  }

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun search(path: String): List<String> {
    val response = httpGet<SearchRestaurantsResponse>(path)
    response.status shouldBe HttpStatus.OK
    return response.body()!!.result.map { it.name }
  }

  @Test
  fun `without q it lists every restaurant sorted by name`() {
    search("/restaurants/") shouldBe listOf("Pizzeria Bella Napoli", "Trattoria da Piero")
  }

  @Test
  fun `q filters by name or address ignoring case`() {
    search("/restaurants/?q=pizz") shouldBe listOf("Pizzeria Bella Napoli")
    search("/restaurants/?q=FORLI") shouldBe listOf("Trattoria da Piero")
    search("/restaurants/?q=kebab") shouldBe emptyList()
  }
}
