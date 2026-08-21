@file:Suppress("UnstableApiUsage")

import utils.libs

plugins {
  id("kotlin-jvm")
}

testing {
  suites {
    register("e2eTest", JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        implementation(libs().kotest)
        implementation(libs().cucumber.java)
        implementation(libs().cucumber.junit)
        implementation(libs().cucumber.junit.platform.engine)
        implementation(libs().cucumber.picocontainer)
        implementation(libs().micronaut.http.client)
        implementation(libs().kotlinx.serialization.json)
        implementation(libs().junit.platform.suite)
        implementation(libs().junit.platform.suite.engine)
        runtimeOnly(libs().junit.platform.suite.launcher)
        runtimeOnly(libs().micronaut.serde.jackson)
      }
    }
  }
}

sourceSets {
  named("e2eTest") {
    kotlin.srcDir("src/e2e/kotlin")
    resources.srcDir("src/e2e/resources")
  }
}

tasks.named<Test>("e2eTest") {
  group = "verification"
  dependsOn(rootProject.tasks.named("composeUp"))
  finalizedBy(rootProject.tasks.named("composeDown"))
  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showStandardStreams = true
  }
}
