plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":commons"))
  jsImplementation(project(":restaurant-shared"))
  jsImplementation(project(":table-reservation-shared"))
}
