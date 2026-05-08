plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":notification-service:shared"))
  jsImplementation(project(":payment-service:shared"))
  jsImplementation(project(":table-reservation-service:shared"))
  jsImplementation(project(":user-service:shared"))
}
