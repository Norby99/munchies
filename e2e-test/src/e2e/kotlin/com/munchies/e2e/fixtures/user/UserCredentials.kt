package com.munchies.e2e.fixtures.user

import com.munchies.commons.domain.port.AuthRole
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import java.util.UUID

object UserCredentials {

  fun newCustomer(): String {
    val id = UUID.randomUUID().toString().take(8)
    return RegisterUserRequest(
      username = "customer-$id",
      email = "customer-$id@e2e-test.local",
      role = AuthRole.CUSTOMER.toString(),
      hashedPassword = "test-password-$id",
      saltValue = "salt",
    ).toJson()
  }

  fun newManager(): String {
    val id = UUID.randomUUID().toString().take(8)
    return RegisterUserRequest(
      username = "manager-$id",
      email = "manager-$id@e2e-test.local",
      role = AuthRole.MANAGER.toString(),
      hashedPassword = "test-password-$id",
      saltValue = "salt",
    ).toJson()
  }
}
