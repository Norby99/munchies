plugins {
  id("micronaut-serdeable")
}

dependencies {
  implementation(project(":user-service:dto"))
}
