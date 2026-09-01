package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoCrudRestaurantRepository
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository.MongoRestaurantRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class GetManagerRestaurantsControllerComponentTest : BaseMenuController() {

  @Inject
  lateinit var restaurantRepository: MongoRestaurantRepository

  @Inject
  lateinit var mongoCrudRestaurantRepository: MongoCrudRestaurantRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudRestaurantRepository.deleteAll()
  }

  private fun restaurantFor(managerId: UserId): Restaurant = Restaurant.create(
    managerId = managerId,
    name = RestaurantName.of("Trattoria da Piero"),
    address = Address.of("Via Roma 1, Milano"),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("piero@example.com"),
  )

  @Test
  fun `getManagerRestaurants should return 200 OK with only that manager's restaurants`() {
    val managerId = UserId()
    restaurantRepository.save(restaurantFor(managerId))
    restaurantRepository.save(restaurantFor(UserId()))

    val response =
      httpGet<GetManagerRestaurantsResponse>("/restaurants/manager/${managerId.value}/")

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.toList() shouldHaveSize 1
  }

  @Test
  fun `getManagerRestaurants should return 200 OK with empty list for manager with none`() {
    val response =
      httpGet<GetManagerRestaurantsResponse>("/restaurants/manager/${UserId().value}/")

    response.status shouldBe HttpStatus.OK
    response.body()!!.result.toList() shouldHaveSize 0
  }
}
