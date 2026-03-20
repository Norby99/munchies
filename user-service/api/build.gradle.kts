plugins {
  id("micronaut-base")
}

dependencies {
  implementation(project(":user-service:dto"))
  api(project(":user-service:dto"))
}
