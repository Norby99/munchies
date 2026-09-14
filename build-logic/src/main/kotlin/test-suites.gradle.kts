@file:Suppress("UnstableApiUsage")
// Thanks gradle

import utils.MUNCHIES_BASE_PACKAGE
import utils.libs

plugins {
  id("jvm-test-suite")
  id("org.jetbrains.kotlinx.kover")
}

testing {
  suites {
    register("integrationTest", JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        implementation(libs().kotest)
        implementation(libs().mockk)
        implementation(libs().micronaut.test.junit5)
        implementation(libs().testcontainers.core)
        implementation(libs().testcontainers.mongodb)
        implementation(libs().testcontainers.kafka)
        implementation(libs().testcontainers.junit.jupiter)
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
        implementation(libs().micronaut.test.junit5)
        implementation(libs().testcontainers.core)
        implementation(libs().testcontainers.mongodb)
        implementation(libs().testcontainers.kafka)
        implementation(libs().testcontainers.junit.jupiter)
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

val testTasksToExclude = listOf("integrationTest", "componentTest")
kover {
  currentProject {
    instrumentation {
      disabledForTestTasks.addAll(testTasksToExclude)
    }
    sources {
      excludedSourceSets.addAll(testTasksToExclude)
    }
  }

  reports {
    filters {
      excludes {
        classes(
          "$MUNCHIES_BASE_PACKAGE.architecture.*",
          "$MUNCHIES_BASE_PACKAGE.commons.*",
          "$MUNCHIES_BASE_PACKAGE.e2e.*",
        )
      }
    }

    verify {
      rule {
        minBound(70)
      }
    }
  }
}
