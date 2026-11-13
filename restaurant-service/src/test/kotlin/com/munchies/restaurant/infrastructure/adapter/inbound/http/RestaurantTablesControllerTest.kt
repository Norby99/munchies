package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantTablesService
import com.munchies.restaurant.application.usecase.restaurant.GetTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetTablesResult
import com.munchies.restaurant.application.usecase.restaurant.TableSpec
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesResult
import com.munchies.restaurant.domain.valueobject.restaurant.Table
import com.munchies.restaurant.infrastructure.adapter.dto.TableDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantTablesControllerTest {

  private lateinit var restaurantService: RestaurantTablesService
  private lateinit var controller: RestaurantTablesController

  @BeforeEach
  fun setUp() {
    restaurantService = mockk()
    controller = RestaurantTablesController(restaurantService)
  }

  private val tables = listOf(Table(1, 2), Table(2, 4))

  private fun request() = UpdateTablesRequest(
    managerId = "manager-1",
    tables = arrayOf(TableDto(1, 2), TableDto(2, 4)),
  )

  // --- GET ---

  @Test
  fun `get should return 200 with the tables wire form`() = runBlocking {
    coEvery { restaurantService.getTables(any()) } returns GetTablesResult.Success(tables)

    val response = controller.getTables("restaurant-1")

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(listOf(1 to 2, 2 to 4), response.body().result.map { it.number to it.seats })
    coVerify { restaurantService.getTables(GetTablesCommand("restaurant-1")) }
  }

  @Test
  fun `get should throw NotFoundException for an unknown restaurant`() = runBlocking {
    coEvery { restaurantService.getTables(any()) } returns GetTablesResult.RestaurantNotFound

    val exception = assertThrows<NotFoundException> { controller.getTables("missing") }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `get should throw ValidationException for an invalid restaurant id`() = runBlocking {
    coEvery { restaurantService.getTables(any()) } returns
      GetTablesResult.InvalidRestaurant("bad id")

    val exception = assertThrows<ValidationException> { controller.getTables("") }

    assertEquals("bad id", exception.message)
  }

  // --- PUT ---

  @Test
  fun `put should return 200 with the saved tables`() = runBlocking {
    coEvery { restaurantService.updateTables(any()) } returns UpdateTablesResult.Success(tables)

    val response = controller.updateTables("restaurant-1", request())

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(listOf(1 to 2, 2 to 4), response.body().result.map { it.number to it.seats })
    coVerify {
      restaurantService.updateTables(
        UpdateTablesCommand(
          managerId = "manager-1",
          restaurantId = "restaurant-1",
          tables = listOf(TableSpec(1, 2), TableSpec(2, 4)),
        ),
      )
    }
  }

  @Test
  fun `put should throw ValidationException for invalid tables`() = runBlocking {
    coEvery { restaurantService.updateTables(any()) } returns
      UpdateTablesResult.InvalidTables("duplicated table numbers: [1]")

    val exception = assertThrows<ValidationException> {
      controller.updateTables("restaurant-1", request())
    }

    assertEquals("duplicated table numbers: [1]", exception.message)
  }

  @Test
  fun `put should throw NotFoundException for an unknown restaurant`() = runBlocking {
    coEvery { restaurantService.updateTables(any()) } returns UpdateTablesResult.NotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateTables("missing", request())
    }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `put should throw UnauthorizedException for another manager`() = runBlocking {
    coEvery { restaurantService.updateTables(any()) } returns UpdateTablesResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateTables("restaurant-1", request())
    }

    assertEquals("Unauthorized to update tables", exception.message)
  }
}
