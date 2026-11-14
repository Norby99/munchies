package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.domain.valueobject.restaurant.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TablesUseCasesTest {

  private lateinit var repository: RestaurantRepository
  private lateinit var getUseCase: GetTablesUseCase
  private lateinit var updateUseCase: UpdateTablesUseCase
  private lateinit var manager: UserId
  private lateinit var restaurant: Restaurant

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    getUseCase = GetTablesUseCase(repository)
    updateUseCase = UpdateTablesUseCase(repository)
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

  private fun update(managerId: String, vararg tables: TableSpec): UpdateTablesResult =
    runBlocking {
      updateUseCase(UpdateTablesCommand(managerId, restaurant.id.value, tables.toList()))
    }

  @Test
  fun `a restaurant has no tables until the manager configures them`() {
    val result = runBlocking { getUseCase(GetTablesCommand(restaurant.id.value)) }
    assertEquals(GetTablesResult.Success(emptyList()), result)
  }

  @Test
  fun `get reports an unknown restaurant`() {
    val unknown = RestaurantId()
    every { repository.findById(unknown) } returns null
    val result = runBlocking { getUseCase(GetTablesCommand(unknown.value)) }
    assertTrue(result is GetTablesResult.RestaurantNotFound)
  }

  @Test
  fun `owner replaces the table layout, kept sorted by number`() {
    val result = update(manager.value, TableSpec(2, 4), TableSpec(1, 2))

    val expected = listOf(Table(1, 2), Table(2, 4))
    assertEquals(UpdateTablesResult.Success(expected), result)
    assertEquals(expected, restaurant.tables)
    verify { repository.update(restaurant) }
  }

  @Test
  fun `owner can remove every table`() {
    update(manager.value, TableSpec(1, 2))
    val result = update(manager.value)
    assertEquals(UpdateTablesResult.Success(emptyList()), result)
  }

  @Test
  fun `another manager cannot change the tables`() {
    val result = update("someone-else", TableSpec(1, 2))
    assertTrue(result is UpdateTablesResult.Unauthorized)
    assertTrue(restaurant.tables.isEmpty())
  }

  @Test
  fun `a table without seats is rejected`() {
    val result = update(manager.value, TableSpec(1, 0))
    assertTrue(result is UpdateTablesResult.InvalidTables)
  }

  @Test
  fun `a non-positive table number is rejected`() {
    val result = update(manager.value, TableSpec(0, 2))
    assertTrue(result is UpdateTablesResult.InvalidTables)
  }

  @Test
  fun `duplicated table numbers are rejected and leave the layout unchanged`() {
    update(manager.value, TableSpec(1, 2))
    val result = update(manager.value, TableSpec(3, 2), TableSpec(3, 4))
    assertTrue(result is UpdateTablesResult.InvalidTables)
    assertEquals(listOf(Table(1, 2)), restaurant.tables)
  }
}
