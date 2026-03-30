plugins {
  id("micronaut-base")
}

dependencies {
  implementation(project(":suggestion-service:dto"))
  api(project(":suggestion-service:dto"))
}
