plugins {
  id("micronaut-server")
}

application {
  mainClass = "com.munchies.restaurant.MainKt"
}

dependencies {
  implementation(project(":commons"))
  testImplementation(testFixtures(project(":architecture-rules")))
}
