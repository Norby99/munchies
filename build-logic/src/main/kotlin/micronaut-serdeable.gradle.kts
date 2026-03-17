import utils.libs

plugins {
  id("kotlin-jvm")
  id("org.jetbrains.kotlin.plugin.allopen")
  id("com.google.devtools.ksp")
  id("com.gradleup.shadow")
  id("io.micronaut.aot")
}

dependencies {
  implementation(libs().bundles.micronaut.serialization)
}
