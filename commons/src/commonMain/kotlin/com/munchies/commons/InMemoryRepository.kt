package com.munchies.commons

open class InMemoryRepository<Id : EntityId<*>, E : AggregateRoot<Id>>(
  private val repo: MutableMap<Id, E> = mutableMapOf(),
) :
  Repository<Id, E> {
  override fun findById(id: Id): E? = repo[id]

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
