package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CapacityPolicyUseCasesTest {

  private lateinit var repository: RestaurantRepository
  private lateinit var getUseCase: GetCapacityPolicyUseCase
  private lateinit var updateUseCase: UpdateCapacityPolicyUseCase
  private lateinit var manager: UserId

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    getUseCase = GetCapacityPolicyUseCase(repository)
    updateUseCase = UpdateCapacityPolicyUseCase(repository)
    manager = UserId.of("manager-1")
  }

  private fun restaurant(policy: CapacityPolicy? = null): Restaurant {
    val base = Restaurant.create(
      managerId = manager,
      name = RestaurantName.of("Trattoria"),
      address = Address.of("Via Roma 1"),
      phone = Phone.of("+39 06 1234567"),
      email = Email.of("info@trattoria.com"),
    )
    if (policy != null) base.updateCapacityPolicy(policy)
    return base
  }

  private fun policy() = CapacityPolicy(
    slotMinutes = 30,
    maxCapacityUnits = 20,
    opening = LocalTime.of(11, 0),
    closing = LocalTime.of(22, 0),
    prepLeadMinutes = 30,
    maxAdvanceDays = 7,
    maxTripsPerSlot = 2,
    maxTripKmBudget = 12.0,
    maxDetourKm = 0.5,
  )

  private fun updateCommand(
    restaurantId: String,
    managerId: String = "manager-1",
    opening: String = "11:00",
    closing: String = "22:00",
    maxDetourKm: Double = 0.5,
  ) = UpdateCapacityPolicyCommand(
    managerId = managerId,
    restaurantId = restaurantId,
    slotMinutes = 30,
    maxCapacityUnits = 20,
    opening = opening,
    closing = closing,
    prepLeadMinutes = 30,
    maxAdvanceDays = 7,
    maxTripsPerSlot = 2,
    maxTripKmBudget = 12.0,
    maxDetourKm = maxDetourKm,
  )

  // --- Get ---

  @Test
  fun `get returns the configured policy`() = runBlocking {
    val existing = restaurant(policy())
    every { repository.findById(any()) } returns existing

    val result = getUseCase(GetCapacityPolicyCommand(existing.id.value))

    assertTrue(result is GetCapacityPolicyResult.Success)
    assertEquals(policy(), (result as GetCapacityPolicyResult.Success).policy)
  }

  @Test
  fun `get reports NotConfigured when the restaurant has no policy`() = runBlocking {
    val existing = restaurant(null)
    every { repository.findById(any()) } returns existing

    val result = getUseCase(GetCapacityPolicyCommand(existing.id.value))

    assertEquals(GetCapacityPolicyResult.NotConfigured, result)
  }

  @Test
  fun `get reports RestaurantNotFound for an unknown restaurant`() = runBlocking {
    every { repository.findById(any()) } returns null

    val result = getUseCase(GetCapacityPolicyCommand("missing"))

    assertEquals(GetCapacityPolicyResult.RestaurantNotFound, result)
  }

  @Test
  fun `get reports InvalidRestaurant for a malformed id`() = runBlocking {
    val result = getUseCase(GetCapacityPolicyCommand(""))

    assertTrue(result is GetCapacityPolicyResult.InvalidRestaurant)
  }

  // --- Update ---

  @Test
  fun `update saves the new policy for the owning manager`() = runBlocking {
    val existing = restaurant(null)
    every { repository.findById(any()) } returns existing

    val result = updateUseCase(updateCommand(existing.id.value))

    assertTrue(result is UpdateCapacityPolicyResult.Success)
    assertNotNull(existing.capacityPolicy)
    coVerify(exactly = 1) { repository.update(existing) }
  }

  @Test
  fun `update rejects a non-owner manager without saving`() = runBlocking {
    val existing = restaurant(null)
    every { repository.findById(any()) } returns existing

    val result = updateUseCase(updateCommand(existing.id.value, managerId = "someone-else"))

    assertEquals(UpdateCapacityPolicyResult.Unauthorized, result)
    assertNull(existing.capacityPolicy)
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `update reports NotFound for an unknown restaurant`() = runBlocking {
    every { repository.findById(any()) } returns null

    val result = updateUseCase(updateCommand("missing"))

    assertEquals(UpdateCapacityPolicyResult.NotFound, result)
  }

  @Test
  fun `update rejects a policy that violates a domain rule`() = runBlocking {
    val existing = restaurant(null)
    every { repository.findById(any()) } returns existing

    val result = updateUseCase(updateCommand(existing.id.value, maxDetourKm = -1.0))

    assertTrue(result is UpdateCapacityPolicyResult.InvalidPolicy)
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `update rejects an unparseable opening time`() = runBlocking {
    val existing = restaurant(null)
    every { repository.findById(any()) } returns existing

    val result = updateUseCase(updateCommand(existing.id.value, opening = "9.00"))

    assertTrue(result is UpdateCapacityPolicyResult.InvalidPolicy)
  }
}
