
plugins {
  id("micronaut-base")
  id("io.micronaut.application")
  id("io.micronaut.test-resources")
}

dependencies {
  runtimeOnly("io.micronaut:micronaut-http-server-netty")
  ksp("io.micronaut.serde:micronaut-serde-processor")
  ksp("io.micronaut:micronaut-http-validation")

  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.junit.platform:junit-platform-suite-engine")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:testcontainers")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

micronaut {
  runtime("netty")
  testRuntime("junit5")
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

val javaVersion: String by project
tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
  jdkVersion = javaVersion
}
