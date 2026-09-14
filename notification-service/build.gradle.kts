plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":commons"))
  jsImplementation(project(":notification-shared"))
  jsImplementation(project(":user-shared"))
  jsImplementation(project(":payment-shared"))
}
