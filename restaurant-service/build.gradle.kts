plugins {
  id("micronaut-server")
}

application {
  mainClass = "com.munchies.restaurant.MainKt"
}

dependencies {
  implementation(project(":commons"))

  // Cucumber BDD
  testImplementation("io.cucumber:cucumber-java:7.15.0")
  testImplementation("io.cucumber:cucumber-junit:7.15.0")
  testImplementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")

  // JUnit 5
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
  testImplementation("org.junit.platform:junit-platform-suite:1.10.0")

  // Micronaut Test
  testImplementation("io.micronaut.test:micronaut-test-junit5")

  // Coroutines
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

  // Mockk
  testImplementation("io.mockk:mockk:1.13.10")
}
