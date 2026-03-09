import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
  alias(libs.plugins.spotless) apply false
  id("munchies-subproject") apply false
}

allprojects {
  group = "munchies"
}

fun configureSpotlessForKotlin(project: Project) {
  project.configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      ktlint()
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      ktlint()
    }
  }
}
apply<SpotlessPlugin>()
configureSpotlessForKotlin(rootProject)

subprojects {
  apply<SpotlessPlugin>()
  configureSpotlessForKotlin(this)

  apply(plugin = "munchies-subproject")
}
