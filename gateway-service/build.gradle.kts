plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":commons"))
  jsImplementation(project(":notification-shared"))
  jsImplementation(project(":payment-shared"))
  jsImplementation(project(":table-reservation-shared"))
  jsImplementation(project(":user-shared"))
}
