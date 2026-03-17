import io.gitlab.arturbosch.detekt.Detekt
import utils.libs

plugins {
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
}

val javaVersion: String by project
kotlin {
  jvmToolchain(javaVersion.toInt())
}
tasks.withType<Detekt>().configureEach {
  jvmTarget = "1.8"
  config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}
