plugins {
  id("micronaut-server")
  id("com.google.devtools.ksp")
}

application {
  mainClass = "com.munchies.restaurant.MainKt"
}

dependencies {
  implementation(project(":commons"))
  testImplementation(testFixtures(project(":architecture-rules")))

  // Cucumber BDD
  testImplementation("io.cucumber:cucumber-java:7.34.4")
  testImplementation("io.cucumber:cucumber-junit:7.34.4")
  testImplementation("io.cucumber:cucumber-junit-platform-engine:7.30.0")

  // JUnit 5
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.5")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.5")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.5")
  testImplementation("org.junit.platform:junit-platform-suite:1.10.5")

  // For injections
  kspTest("io.micronaut:micronaut-inject-java")

  // Micronaut Test
  testImplementation("io.micronaut:micronaut-inject")
  testImplementation("io.micronaut:micronaut-context")
  testImplementation("io.micronaut.test:micronaut-test-junit5")

  // Coroutines
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

  // Mockk
  testImplementation("io.mockk:mockk:1.13.17")
}
