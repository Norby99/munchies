import utils.libs

plugins {
  id("kotlin-jvm")
  id("org.jetbrains.kotlin.plugin.allopen")
  id("com.google.devtools.ksp")
  id("com.gradleup.shadow")
  id("io.micronaut.aot")
}
dependencies {
  implementation(libs().micronaut.http.client)
  // implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
  // implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
  // runtimeOnly("ch.qos.logback:logback-classic")
  runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(libs().jakarta.validation.api)
  runtimeOnly("io.micronaut:micronaut-http-server-netty")

  implementation(libs().bundles.micronaut.serialization)
}
