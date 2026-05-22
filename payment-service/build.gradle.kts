plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":commons"))
  jsImplementation(project(":payment-shared"))
}
