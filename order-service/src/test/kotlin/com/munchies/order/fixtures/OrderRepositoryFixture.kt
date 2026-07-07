package com.munchies.order.fixtures

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.ports.OrderRepository
import jakarta.inject.Singleton

@Singleton
class OrderRepositoryFixture : OrderRepository {

  private val inMemoryRepository = InMemoryRepository<OrderId, Order>()

  override fun findById(id: OrderId): Order? = inMemoryRepository.findById(id)

  override fun save(entity: Order) {
    inMemoryRepository.save(entity)
  }

  override fun update(entity: Order) {
    inMemoryRepository.update(entity)
  }

  override fun delete(entity: Order) {
    inMemoryRepository.delete(entity)
  }
}
