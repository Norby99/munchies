package com.munchies.user.domain.model

import com.munchies.commons.UUIDEntityId.Companion.newId
import io.kotest.matchers.types.shouldBeInstanceOf

val exampleUserIdValue = newId()
val exampleUserId = UserId(exampleUserIdValue)
const val EXAMPLE_USERNAME = "example-username"
val exampleEmail = Email("example@example.com")
val exampleUserProfileRes = UserProfile.factory
  .create(username = EXAMPLE_USERNAME, email = exampleEmail, role = UserRole.CUSTOMER.toString())

val exampleUserProfile =
  (
    exampleUserProfileRes
      as UserProfile.Companion.UserProfileFactory.UserProfileFactoryResult.Success
    ).profile
val exampleUser =
  User.factory.create(id = exampleUserIdValue, profile = exampleUserProfileRes)
    .shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Success>().user

fun User.update(id: UserId = this.id, profile: UserProfile = this.profile): User = (
  User.factory.create(
    id = id.value,
    profile = profile,
  ) as User.Companion.UserFactory.UserFactoryResult.Success
  ).user

fun User.update(id: String = this.id.value, profile: UserProfile = this.profile): User = (
  User.factory.create(
    id = id,
    profile = profile,
  ) as User.Companion.UserFactory.UserFactoryResult.Success
  ).user

fun User.Companion.UserFactory.UserFactoryResult.shouldBeSuccess(): User {
  this.shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Success>()
  return this.user
}
fun User.Companion.UserFactory.UserFactoryResult.shouldBeFailure(): String {
  this.shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Failure>()
  return this.reason
}
