plugins {
  id("express-server")
}

dependencies {
  add("jsImplementation", project(":commons"))
  add("jsImplementation", project(":table-reservation-service:shared"))
}
