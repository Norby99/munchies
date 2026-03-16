import utils.MUNCHIES_BASE_PACKAGE
import utils.getServiceName
import utils.libs

plugins {
  id("kotlin-jvm")
  id("org.jetbrains.kotlin.plugin.allopen")
  id("com.google.devtools.ksp")
  id("com.gradleup.shadow")
  id("io.micronaut.application")
  id("io.micronaut.aot")
}
dependencies {
  ksp("io.micronaut:micronaut-http-validation")
  ksp("io.micronaut.serde:micronaut-serde-processor")
  implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
  implementation("io.micronaut.serde:micronaut-serde-jackson")
  // implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
  // implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
  compileOnly("io.micronaut:micronaut-http-client")
  // runtimeOnly("ch.qos.logback:logback-classic")
  runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation(libs().jakarta.validation.api)
  runtimeOnly("io.micronaut:micronaut-http-server-netty")
}

micronaut {
  runtime("netty")
  processing {
    incremental(true)
  }
  aot {
    // Please review carefully the optimizations enabled below
    // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
    optimizeServiceLoading = false
    convertYamlToJava = false
    precomputeOperations = true
    cacheEnvironment = true
    optimizeClassLoading = true
    deduceEnvironment = true
    optimizeNetty = true
  }
}

application {
  mainClass = "$MUNCHIES_BASE_PACKAGE.${getServiceName(project)}.MainKt"
}
