plugins {
  id("express-server")
}

dependencies {
  add("jsImplementation", project(":commons"))
  add("jsImplementation", project(":payment-shared"))
}
