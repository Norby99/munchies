package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.persistence.InMemoryRestaurantRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchRestaurantsTest {

  private lateinit var repository: InMemoryRestaurantRepository
  private lateinit var search: SearchRestaurantsUseCase

  private fun restaurant(name: String, address: String = "Via Roma 1, Cesena"): Restaurant =
    Restaurant.create(
      managerId = UserId(),
      name = RestaurantName.of(name),
      address = Address.of(address),
      phone = Phone.of("+39 333 1234567"),
      email = Email.of("info@example.com"),
    )

  private suspend fun names(query: String): List<String> {
    val result = search(SearchRestaurantsCommand(query))
    assertTrue(result is SearchRestaurantsResult.Success, "expected Success, got $result")
    return (result as SearchRestaurantsResult.Success).restaurants.map { it.name.value }
  }

  @BeforeEach
  fun setUp() {
    repository = InMemoryRestaurantRepository()
    search = SearchRestaurantsUseCase(repository)
    repository.save(restaurant("Trattoria da Piero", "Via Emilia 10, Forli"))
    repository.save(restaurant("pizzeria Bella Napoli"))
    repository.save(restaurant("Sushi Zen", "Corso Garibaldi 3, Forli"))
  }

  @Test
  fun `a blank query lists every restaurant sorted by name, ignoring case`() = runBlocking {
    assertEquals(listOf("pizzeria Bella Napoli", "Sushi Zen", "Trattoria da Piero"), names("  "))
  }

  @Test
  fun `matches the name ignoring case`() = runBlocking {
    assertEquals(listOf("pizzeria Bella Napoli"), names("PIZZ"))
  }

  @Test
  fun `matches the address too`() = runBlocking {
    assertEquals(listOf("Sushi Zen", "Trattoria da Piero"), names("forli"))
  }

  @Test
  fun `surrounding spaces are ignored`() = runBlocking {
    assertEquals(listOf("Sushi Zen"), names("  sushi "))
  }

  @Test
  fun `no match gives an empty list`() = runBlocking {
    assertEquals(emptyList<String>(), names("kebab"))
  }

  @Test
  fun `the result is capped at MAX_RESULTS`() = runBlocking {
    repeat(SearchRestaurantsUseCase.MAX_RESULTS + 5) { repository.save(restaurant("Bar $it")) }
    assertEquals(SearchRestaurantsUseCase.MAX_RESULTS, names("bar").size)
  }

  @Test
  fun `a query that is too long is a validation error`() = runBlocking {
    val result =
      search(SearchRestaurantsCommand("x".repeat(SearchRestaurantsUseCase.MAX_QUERY_LENGTH + 1)))
    assertTrue(result is SearchRestaurantsResult.ValidationError)
  }
}
