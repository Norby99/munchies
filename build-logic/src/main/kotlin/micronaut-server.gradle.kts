
plugins {
  id("micronaut-base")
  id("io.micronaut.application")
}

dependencies {
  runtimeOnly("io.micronaut:micronaut-http-server-netty")
  ksp("io.micronaut.serde:micronaut-serde-processor")
  ksp("io.micronaut:micronaut-http-validation")
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
