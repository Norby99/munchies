package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CookStation
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenRoute
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KitchenSetupUseCasesTest {

  private lateinit var repository: RestaurantRepository
  private lateinit var getUseCase: GetKitchenSetupUseCase
  private lateinit var updateUseCase: UpdateKitchenSetupUseCase
  private lateinit var manager: UserId
  private lateinit var restaurant: Restaurant

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    getUseCase = GetKitchenSetupUseCase(repository)
    updateUseCase = UpdateKitchenSetupUseCase(repository)
    manager = UserId.of("manager-1")
    restaurant = Restaurant.create(
      managerId = manager,
      name = RestaurantName.of("Trattoria"),
      address = Address.of("Via Roma 1"),
      phone = Phone.of("+39 0541 000000"),
      email = Email.of("info@trattoria.com"),
    )
    every { repository.findById(restaurant.id) } returns restaurant
  }

  private fun command(
    managerId: String = manager.value,
    route: String = "PREP_THEN_COOK",
    prepWorkers: Int = 2,
    stations: List<CookStationSpec> = listOf(CookStationSpec("  Oven ", 7)),
  ) = UpdateKitchenSetupCommand(
    managerId = managerId,
    restaurantId = restaurant.id.value,
    route = route,
    prepWorkers = prepWorkers,
    cookStations = stations,
    prepMinutes = 3.0,
    loadingMinutes = 0.5,
    cookMinutes = 5.0,
  )

  private fun update(command: UpdateKitchenSetupCommand) = runBlocking { updateUseCase(command) }

  @Test
  fun `the kitchen is not configured until the manager describes it`() {
    val result = runBlocking { getUseCase(GetKitchenSetupCommand(restaurant.id.value)) }
    assertEquals(GetKitchenSetupResult.NotConfigured, result)
  }

  @Test
  fun `get reports an unknown restaurant`() {
    val unknown = RestaurantId()
    every { repository.findById(unknown) } returns null
    val result = runBlocking { getUseCase(GetKitchenSetupCommand(unknown.value)) }
    assertEquals(GetKitchenSetupResult.RestaurantNotFound, result)
  }

  @Test
  fun `owner saves the kitchen, station names are trimmed`() {
    val result = update(command())

    assertTrue(result is UpdateKitchenSetupResult.Success)
    val setup = (result as UpdateKitchenSetupResult.Success).setup
    assertEquals(KitchenRoute.PREP_THEN_COOK, setup.route)
    assertEquals(listOf(CookStation("Oven", 7)), setup.cookStations)
    assertEquals(setup, restaurant.kitchenSetup)
    verify { repository.update(restaurant) }

    val read = runBlocking { getUseCase(GetKitchenSetupCommand(restaurant.id.value)) }
    assertEquals(GetKitchenSetupResult.Success(setup), read)
  }

  @Test
  fun `an unknown route is an invalid setup`() {
    val result = update(command(route = "FRY_THEN_SERVE"))

    assertTrue(result is UpdateKitchenSetupResult.InvalidSetup)
    assertTrue((result as UpdateKitchenSetupResult.InvalidSetup).error.contains("FRY_THEN_SERVE"))
    assertNull(restaurant.kitchenSetup)
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `a kitchen the domain refuses is an invalid setup`() {
    val result = update(command(stations = emptyList()))

    assertEquals(
      UpdateKitchenSetupResult.InvalidSetup("the kitchen needs at least one cook station"),
      result,
    )
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `another manager is unauthorized`() {
    val result = update(command(managerId = "manager-2"))

    assertEquals(UpdateKitchenSetupResult.Unauthorized, result)
    assertNull(restaurant.kitchenSetup)
  }

  @Test
  fun `a request without a manager is unauthorized`() {
    assertEquals(UpdateKitchenSetupResult.Unauthorized, update(command(managerId = "")))
  }

  @Test
  fun `an unknown restaurant is not found`() {
    val unknown = RestaurantId()
    every { repository.findById(unknown) } returns null

    val result = update(command().copy(restaurantId = unknown.value))

    assertEquals(UpdateKitchenSetupResult.NotFound, result)
  }
}
