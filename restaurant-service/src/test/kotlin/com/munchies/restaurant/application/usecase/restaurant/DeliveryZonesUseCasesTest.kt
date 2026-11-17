package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeliveryZonesUseCasesTest {

  private lateinit var repository: RestaurantRepository
  private lateinit var getUseCase: GetDeliveryZonesUseCase
  private lateinit var updateUseCase: UpdateDeliveryZonesUseCase
  private lateinit var manager: UserId
  private lateinit var restaurant: Restaurant

  private val triangle = listOf(
    ZoneVertexSpec(44.00, 12.60),
    ZoneVertexSpec(44.00, 12.70),
    ZoneVertexSpec(44.05, 12.65),
  )

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    getUseCase = GetDeliveryZonesUseCase(repository)
    updateUseCase = UpdateDeliveryZonesUseCase(repository)
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

  private fun update(managerId: String, vararg zones: DeliveryZoneSpec): UpdateDeliveryZonesResult =
    runBlocking {
      updateUseCase(UpdateDeliveryZonesCommand(managerId, restaurant.id.value, zones.toList()))
    }

  @Test
  fun `a restaurant has no zones until the manager draws them`() {
    val result = runBlocking { getUseCase(GetDeliveryZonesCommand(restaurant.id.value)) }
    assertEquals(GetDeliveryZonesResult.Success(emptyList()), result)
  }

  @Test
  fun `get reports an unknown restaurant`() {
    val unknown = RestaurantId()
    every { repository.findById(unknown) } returns null
    val result = runBlocking { getUseCase(GetDeliveryZonesCommand(unknown.value)) }
    assertTrue(result is GetDeliveryZonesResult.RestaurantNotFound)
  }

  @Test
  fun `owner saves zones, a new zone gets a generated id and names are trimmed`() {
    val result = update(
      manager.value,
      DeliveryZoneSpec(id = "", name = "  Centro ", boundary = triangle),
      DeliveryZoneSpec(id = "kept-id", name = "Mare", boundary = triangle),
    )

    assertTrue(result is UpdateDeliveryZonesResult.Success)
    val zones = (result as UpdateDeliveryZonesResult.Success).zones
    assertEquals(listOf("Centro", "Mare"), zones.map { it.name })
    assertTrue(zones[0].id.isNotBlank())
    assertEquals("kept-id", zones[1].id)
    assertEquals(zones, restaurant.deliveryZones)
    verify { repository.update(restaurant) }
  }

  @Test
  fun `overlapping zones are accepted`() {
    val result = update(
      manager.value,
      DeliveryZoneSpec("", "A", triangle),
      DeliveryZoneSpec("", "B", triangle),
    )
    assertTrue(result is UpdateDeliveryZonesResult.Success)
  }

  @Test
  fun `owner can remove every zone`() {
    update(manager.value, DeliveryZoneSpec("", "Centro", triangle))
    val result = update(manager.value)
    assertEquals(UpdateDeliveryZonesResult.Success(emptyList()), result)
  }

  @Test
  fun `another manager cannot change the zones`() {
    val result = update("someone-else", DeliveryZoneSpec("", "Centro", triangle))
    assertTrue(result is UpdateDeliveryZonesResult.Unauthorized)
    assertTrue(restaurant.deliveryZones.isEmpty())
  }

  @Test
  fun `a zone with an invalid vertex is rejected`() {
    val result = update(
      manager.value,
      DeliveryZoneSpec("", "Centro", triangle + ZoneVertexSpec(95.0, 12.0)),
    )
    assertTrue(result is UpdateDeliveryZonesResult.InvalidZones)
  }

  @Test
  fun `a zone with fewer than three vertices is rejected`() {
    val result = update(manager.value, DeliveryZoneSpec("", "Centro", triangle.take(2)))
    assertTrue(result is UpdateDeliveryZonesResult.InvalidZones)
  }

  @Test
  fun `duplicated zone names are rejected ignoring case and leave the zones unchanged`() {
    update(manager.value, DeliveryZoneSpec("", "Centro", triangle))
    val before = restaurant.deliveryZones
    val result = update(
      manager.value,
      DeliveryZoneSpec("", "Mare", triangle),
      DeliveryZoneSpec("", "mare", triangle),
    )
    assertTrue(result is UpdateDeliveryZonesResult.InvalidZones)
    assertEquals(before, restaurant.deliveryZones)
  }

  @Test
  fun `duplicated zone ids are rejected`() {
    val result = update(
      manager.value,
      DeliveryZoneSpec("same", "A", triangle),
      DeliveryZoneSpec("same", "B", triangle),
    )
    assertTrue(result is UpdateDeliveryZonesResult.InvalidZones)
  }

  @Test
  fun `more zones than the limit are rejected`() {
    val zones = (0..Restaurant.MAX_DELIVERY_ZONES).map {
      DeliveryZoneSpec(
        "",
        "Zone $it",
        triangle,
      )
    }
    val result = update(manager.value, *zones.toTypedArray())
    assertTrue(result is UpdateDeliveryZonesResult.InvalidZones)
  }
}
