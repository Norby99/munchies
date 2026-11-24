package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KitchenSetupTest {

  private val oven = CookStation("Oven", 7)

  private fun setup(
    prepWorkers: Int = 2,
    cookStations: List<CookStation> = listOf(oven),
    prepMinutes: Double = 3.0,
    loadingMinutes: Double = 0.5,
    cookMinutes: Double = 5.0,
  ) = KitchenSetup(
    route = KitchenRoute.PREP_THEN_COOK,
    prepWorkers = prepWorkers,
    cookStations = cookStations,
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )

  @Test
  fun `accepts a pizzeria with one oven`() {
    val kitchen = setup()
    assertEquals(listOf(oven), kitchen.cookStations)
  }

  @Test
  fun `accepts several stations`() {
    val kitchen = setup(cookStations = listOf(oven, CookStation("Grill", 2)))
    assertEquals(2, kitchen.cookStations.size)
  }

  @Test
  fun `refuses a kitchen without workers or with too many`() {
    assertThrows<IllegalArgumentException> { setup(prepWorkers = 0) }
    assertThrows<IllegalArgumentException> {
      setup(
        prepWorkers = KitchenSetup.MAX_PREP_WORKERS + 1,
      )
    }
  }

  @Test
  fun `refuses a kitchen without cook stations`() {
    assertThrows<IllegalArgumentException> { setup(cookStations = emptyList()) }
  }

  @Test
  fun `refuses two stations with the same name ignoring case`() {
    val error = assertThrows<IllegalArgumentException> {
      setup(cookStations = listOf(oven, CookStation("oven", 3)))
    }
    assertEquals("duplicated station names: [oven]", error.message)
  }

  @Test
  fun `refuses stage times that are zero, negative, too long or not a number`() {
    assertThrows<IllegalArgumentException> { setup(prepMinutes = 0.0) }
    assertThrows<IllegalArgumentException> { setup(loadingMinutes = -1.0) }
    assertThrows<IllegalArgumentException> {
      setup(
        cookMinutes = KitchenSetup.MAX_STAGE_MINUTES + 1,
      )
    }
    assertThrows<IllegalArgumentException> { setup(cookMinutes = Double.NaN) }
  }

  @Test
  fun `a station needs a name and at least one place`() {
    assertThrows<IllegalArgumentException> { CookStation(" ", 3) }
    assertThrows<IllegalArgumentException> { CookStation("Oven", 0) }
    assertThrows<IllegalArgumentException> {
      CookStation(
        "Oven",
        KitchenSetup.MAX_STATION_PLACES + 1,
      )
    }
    assertThrows<IllegalArgumentException> {
      CookStation("x".repeat(KitchenSetup.MAX_NAME_LENGTH + 1), 3)
    }
  }
}
