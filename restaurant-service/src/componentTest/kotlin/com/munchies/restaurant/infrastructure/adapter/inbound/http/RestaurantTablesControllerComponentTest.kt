package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.dto.TableDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetTablesResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoRestaurantRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 * Component test of `GET`/`PUT /restaurants/{id}/tables`, over HTTP and against a real MongoDB,
 * so that the request and response bodies go through JSON serialization as they do for
 * table-reservation-service and the gateway.
 */
@MicronautTest(environments = ["prod"], transactional = false)
class RestaurantTablesControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  private val managerId = UserId()

  private fun seedRestaurant(): String {
    val restaurant = Restaurant.create(
      managerId = managerId,
      name = RestaurantName.of("Trattoria da Piero"),
      address = Address.of("Via Emilia 10, Forli"),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("info@example.com"),
    )
    restaurantRepository.save(restaurant)
    return restaurant.id.value
  }

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun tablesOf(restaurantId: String): List<Pair<Int, Int>> {
    val response = httpGet<GetTablesResponse>("/restaurants/$restaurantId/tables")
    response.status shouldBe HttpStatus.OK
    return response.body()!!.result.map { it.number to it.seats }
  }

  @Test
  fun `a restaurant has no tables until its manager configures them`() {
    tablesOf(seedRestaurant()) shouldBe emptyList()
  }

  @Test
  fun `the manager's layout is saved and read back`() {
    val restaurantId = seedRestaurant()
    val request = UpdateTablesRequest(
      managerId = managerId.value,
      tables = arrayOf(TableDto(number = 1, seats = 2), TableDto(number = 2, seats = 4)),
    )

    val response = httpPut<UpdateTablesResponse>("/restaurants/$restaurantId/tables", request)

    response.status shouldBe HttpStatus.OK
    tablesOf(restaurantId) shouldBe listOf(1 to 2, 2 to 4)
  }
}
