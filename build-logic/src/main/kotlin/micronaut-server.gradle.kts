plugins {
  id("micronaut-base")
  id("io.micronaut.application")
  id("io.micronaut.test-resources")
}

dependencies {
  runtimeOnly("io.micronaut:micronaut-http-server-netty")
  ksp("io.micronaut.serde:micronaut-serde-processor")
  ksp("io.micronaut:micronaut-http-validation")

  implementation("io.micronaut:micronaut-management")

  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.junit.platform:junit-platform-suite-engine")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:testcontainers")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  implementation("io.micronaut.mongodb:micronaut-mongo-sync")
  ksp("io.micronaut.data:micronaut-data-document-processor")
  implementation("io.micronaut.data:micronaut-data-mongodb")
  runtimeOnly("org.mongodb:mongodb-driver-sync")

  runtimeOnly("org.yaml:snakeyaml")
  ksp("io.micronaut.openapi:micronaut-openapi")
  compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
  implementation("io.swagger.core.v3:swagger-annotations")
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

if (rootProject.tasks.findByName("composeUp") == null) {
  val generatedComposeDir = rootProject.layout.buildDirectory.dir("docker")

  val generateComposeFile = rootProject.tasks.register("generateComposeFile") {
    val outputFile = generatedComposeDir.get().file("docker-compose.yml").asFile
    outputs.file(outputFile)

    val kafkaTemplateFile = rootProject.file("config/kafka/docker-compose.yml")
    val mongoTemplateFile = rootProject.file("config/mongodb/docker-compose.yml")
    val dockerTemplateFile = rootProject.file("config/service/docker-compose.yml")

    doLast {
      outputFile.parentFile.mkdirs()
      val builder = StringBuilder()
      builder.appendLine("services:")

      // Append Kafka template
      if (kafkaTemplateFile.exists()) {
        val kafkaLines = kafkaTemplateFile.readLines()
          .filterNot {
            it.trim().startsWith(
              "services:",
            ) || it.trim().startsWith("version:")
          }
        builder.appendLine(kafkaLines.joinToString("\n"))
      }

      var portOffset = 1
      val volumesBuilder = StringBuilder()

      // Extract service names dynamically from the existing docker-compose file!
      val discoveredServices = mutableListOf<String>()
      val appServicesBuilder = StringBuilder()

      if (dockerTemplateFile.exists()) {
        val dockerLines = dockerTemplateFile.readLines()
        var currentService = ""

        dockerLines.forEach { line ->
          if (line.trim().startsWith(
              "version:",
            ) || line.trim() == "services:"
          ) {
            return@forEach
          }

          if (line.startsWith(
              "  ",
            ) && !line.startsWith("   ") && line.trim().endsWith(":")
          ) {
            currentService = line.trim().dropLast(1).replace("munchies-", "")
            discoveredServices.add(currentService)
          }
          appServicesBuilder.appendLine(line)
        }
      }

      discoveredServices.forEach { serviceName ->
        val mongoName = "munchies-mongo-$serviceName"
        val mongoPort = 27017 + portOffset

        // Append Mongo template dynamically for each discovered service
        if (mongoTemplateFile.exists()) {
          val mongoText = mongoTemplateFile.readText()
            .replace("{{MONGO_SERVICE_NAME}}", mongoName)
            .replace("{{MONGO_PORT}}", mongoPort.toString())
            .replace("{{MONGO_VOLUME_NAME}}", "${mongoName}_data")

          val mongoLines = mongoText.lines()
          var inVolumes = false
          mongoLines.forEach { line ->
            if (line.trim().startsWith(
                "version:",
              ) || line.startsWith("services:")
            ) {
              return@forEach
            }

            if (line.startsWith("volumes:")) {
              inVolumes = true
              return@forEach
            }

            if (inVolumes) {
              if (line.isNotBlank() && !line.startsWith(" ")) {
                inVolumes = false
              } else if (line.isNotBlank()) {
                volumesBuilder.appendLine(line)
              }
            } else if (line.isNotBlank()) {
              // Fix mongo block indentation to correctly sit inside 'services:'
              builder.appendLine(line)
            }
          }
        }
        portOffset++
      }

      // Finally, append all the existing app services
      if (appServicesBuilder.isNotBlank()) {
        builder.append(appServicesBuilder.toString())
      }

      if (volumesBuilder.isNotBlank()) {
        builder.appendLine("volumes:")
        builder.appendLine(volumesBuilder.toString())
      }

      outputFile.writeText(builder.toString())
    }
  }

  rootProject.tasks.register<Exec>("composeUp") {
    group = "compose"
    dependsOn(generateComposeFile)

    // Depend on buildLayers & dockerfile based on projects containing Micronaut server plugin
    rootProject.subprojects.forEach { proj ->
      if (proj.plugins.hasPlugin("io.micronaut.application")) {
        dependsOn("${proj.path}:dockerfile")
        dependsOn("${proj.path}:buildLayers")
      }
    }

    workingDir(generatedComposeDir.get().asFile)
    val detachedParam = if (rootProject.hasProperty("d")) "-d" else ""
    commandLine(
      "bash",
      "-c",
      "docker compose -f docker-compose.yml up --build $detachedParam",
    )
  }

  rootProject.tasks.register<Exec>("composeDown") {
    group = "compose"
    dependsOn(generateComposeFile)

    workingDir(generatedComposeDir.get().asFile)
    commandLine(
      "bash",
      "-c",
      "docker compose -f docker-compose.yml down",
    )
  }
}
