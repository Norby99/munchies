package com.munchies.e2e.fixtures.user

import com.munchies.commons.domain.port.AuthRole
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import java.util.UUID

object UserCredentials {
  private val userId = UUID.randomUUID().toString().take(8)
  private val managerId = UUID.randomUUID().toString().take(8)

  val customerJson = RegisterUserRequest(
    username = "customer-$userId",
    email = "customer-$userId@e2e-test.local",
    role = AuthRole.CUSTOMER.toString(),
    hashedPassword = "test-password-$userId",
    saltValue = "salt",
  ).toJson()

  val managerJson = RegisterUserRequest(
    username = "manager-$managerId",
    email = "manager-$managerId@e2e-test.local",
    role = AuthRole.MANAGER.toString(),
    hashedPassword = "test-password-$managerId",
    saltValue = "salt",
  ).toJson()
}
