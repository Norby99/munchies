@file:Suppress("UnstableApiUsage")
// Thanks gradle

import utils.libs

plugins {
  id("jvm-test-suite")
}

testing {
  suites {
    register("integrationTest", JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        implementation(libs().kotest)
        implementation(libs().mockk)
        implementation("org.testcontainers:testcontainers")
        implementation("org.testcontainers:mongodb")
        implementation("org.testcontainers:junit-jupiter")
      }
      targets {
        all { testTask.configure { shouldRunAfter("test") } }
      }
    }

    register("componentTest", JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        implementation(libs().kotest)
        implementation(libs().mockk)
        implementation("io.micronaut.test:micronaut-test-junit5")
        implementation("org.testcontainers:testcontainers")
        implementation("org.testcontainers:mongodb")
        implementation("org.testcontainers:junit-jupiter")
      }
      targets {
        all { testTask.configure { shouldRunAfter("integrationTest") } }
      }
    }
  }
}

sourceSets {
  named("integrationTest") {
    compileClasspath += sourceSets.named("main").get().output
    runtimeClasspath += sourceSets.named("main").get().output
  }
  named("componentTest") {
    compileClasspath += sourceSets.named("main").get().output
    runtimeClasspath += sourceSets.named("main").get().output
  }
}

listOf("integrationTest", "componentTest").forEach { suiteName ->
  configurations.named("${suiteName}Implementation") {
    extendsFrom(configurations["implementation"])
  }
  configurations.named("${suiteName}RuntimeOnly") {
    extendsFrom(configurations["runtimeOnly"])
  }
}

tasks.named("check") {
  dependsOn("integrationTest", "componentTest")
}
