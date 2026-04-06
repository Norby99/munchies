import io.gitlab.arturbosch.detekt.Detekt
import utils.MUNCHIES_BASE_PACKAGE
import utils.libs

plugins {
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
  id("org.jetbrains.kotlinx.kover")
  id("dokka-convention")
}

val javaVersion: String by project
kotlin {
  jvmToolchain(javaVersion.toInt())
}

dependencies {
  testImplementation(libs().konsist)
  testImplementation(libs().kotest)
  testImplementation(libs().mockito)
}

tasks.withType<Detekt>().configureEach {
  jvmTarget = "1.8"
  config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    val testOutput = (
      System.getenv("TEST_OUTPUT")?.lowercase()
        ?: project.findProperty("testOutput")
        ?: ""
      )
      .toString()
      .lowercase()

    when (testOutput) {
      "all" -> {
        showStandardStreams = true
        events("passed", "skipped", "failed", "standardOut", "standardError")
      }
      "failed" -> {
        showStandardStreams = true
        events("failed", "standardOut", "standardError")
      }
    }
  }
}

kover {
  reports {
    filters {
      excludes {
        classes(
          "$MUNCHIES_BASE_PACKAGE.*.presentation.*",
          "$MUNCHIES_BASE_PACKAGE.architecture.*",
          // TODO
          "$MUNCHIES_BASE_PACKAGE.commons.*",
        )
      }
    }

    verify {
      rule {
        // minBound(70) TODO
      }
    }
  }
}
