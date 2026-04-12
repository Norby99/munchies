package com.munchies.commons.repository

import com.munchies.commons.Entity
import com.munchies.commons.EntityId
import com.munchies.commons.Repository

open class InMemoryRepository<Id : EntityId<*>, E : Entity<Id>>(
  private val repo: MutableMap<Id, E> = mutableMapOf(),
) :
  Repository<Id, E> {
  override fun findById(id: Id): E? = repo[id]

  override fun findByPredicate(predicate: (E) -> Boolean): E? = repo.values.firstOrNull(predicate)

  override fun save(entity: E) {
    repo[entity.id] = entity
  }

  override fun update(entity: E) {
    repo[entity.id] = entity
  }

  override fun delete(entity: E) {
    repo.remove(entity.id)
  }
}
