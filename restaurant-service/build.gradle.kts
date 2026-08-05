plugins {
  id("micronaut-server")
}

application {
  mainClass = "com.munchies.restaurant.MainKt"
}

dependencies {
  implementation(project(":commons"))
  implementation(project(":restaurant-shared"))
  testImplementation(testFixtures(project(":architecture-rules")))
}
