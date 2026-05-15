package com.munchies.commons

/**
 * Base value object for entity identifiers.
 *
 * Wraps a unique identifier of generic type [Id] and implements value-semantics equality
 * based on the identifier value rather than object reference.
 *
 * @param Id the type of the underlying identifier (e.g., String, UUID).
 * @property value the unique identifier.
 */
open class EntityId<Id>(open val value: Id) {
  /**
   * Compares two [EntityId] instances based on their underlying values.
   *
   * @param other the object to compare with.
   * @return `true` if both instances have equal values and are of the same type.
   */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as EntityId<*>

    return value == other.value
  }

  /**
   * Generates a hash code based on the underlying identifier value.
   *
   * @return hash code derived from [value].
   */
  override fun hashCode(): Int {
    return value.hashCode()
  }
}

/**
 * Base class for domain entities.
 *
 * Entities are identified by a unique [id] of type [Id] and implement equality
 * based on their identifier. Entities are mutable objects that exist for a continuous period.
 *
 * @param Id the type of the entity identifier (must extend [EntityId]).
 * @property id the unique domain identifier of this entity.
 */
open class Entity<Id : EntityId<*>>(open val id: Id) {
  /**
   * Compares two [Entity] instances based on their identifiers.
   *
   * @param other the object to compare with.
   * @return `true` if both entities have equal identifiers and are of the same type.
   */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Entity<*>

    return id == other.id
  }

  /**
   * Generates a hash code based on the entity identifier.
   *
   * @return hash code derived from the entity's [id].
   */
  override fun hashCode(): Int {
    return id.hashCode()
  }
}

/**
 * Base class for aggregate roots in Domain-Driven Design.
 *
 * An aggregate root is the entry point to an aggregate cluster and is responsible for
 * maintaining invariants within its aggregate boundary.
 *
 * @param Id the type of the aggregate root identifier (must extend [EntityId]).
 * @param id the unique domain identifier.
 */
open class AggregateRoot<Id : EntityId<*>>(id: Id) : Entity<Id>(id)

/**
 * Factory interface for creating domain entities.
 *
 * Used to encapsulate complex creation logic and ensure entities are created in valid states.
 *
 * @param E the type of entity being created (must extend [Entity]).
 */
interface Factory<E : Entity<*>>

/**
 * Repository interface for persisting and retrieving entities.
 *
 * Acts as a collection-like abstraction over entity storage, providing CRUD operations
 * and factory-like creation capabilities.
 *
 * @param Id the type of entity identifier (must extend [EntityId]).
 * @param E the type of entity being managed (must extend [Entity]).
 */
interface Repository<Id : EntityId<*>, E : Entity<Id>> {
  /**
   * Retrieves an entity by its identifier.
   *
   * @param id the unique identifier of the entity.
   * @return the entity if found, or `null` if not found.
   */
  fun findById(id: Id): E?

  /**
   * Retrieves an entity matching a given predicate.
   *
   * @param predicate a function that takes an entity and returns `true` if it matches the criteria.
   * @return the first entity that matches the predicate, or `null` if no match is found.
   */
  fun findByPredicate(predicate: (E) -> Boolean): E?

  /**
   * Persists a new entity.
   *
   * @param entity the entity to persist.
   */
  fun save(entity: E)

  /**
   * Updates an existing entity.
   *
   * @param entity the entity with updated state.
   */
  fun update(entity: E)

  /**
   * Deletes an existing entity.
   *
   * @param entity the entity to delete.
   */
  fun delete(entity: E)
}
