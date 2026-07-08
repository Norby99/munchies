package compose

import com.avast.gradle.dockercompose.ComposeExtension
import utils.ProjectLanguage
import utils.ProjectType
import utils.getProjectLanguage
import utils.getProjectType

plugins {
  id("com.avast.gradle.docker-compose")
}

configure<ComposeExtension> {
  useComposeFiles.set(
    listOf(
      "$rootDir/config/kafka/docker-compose.yml",
      "$rootDir/config/mongodb/docker-compose.yml",
      "$rootDir/config/service/docker-compose.yml",
    ),
  )

  // executes 'compose' 'build' before 'up'
  buildBeforeUp.set(true)
  // waiting for all ports to be open
  waitForTcpPorts.set(true)
  // true -> prints the output of the containers to the console [for debugging]
  captureContainersOutput.set(false)
  // false -> wipes all the data volumes on composeDown
  removeVolumes.set(false)
  // false -> containers are not removed on composeDown, speed up the next composeUp
  stopContainers.set(true)

  composeAdditionalArgs.set(listOf("--project-directory", rootDir.absolutePath))
}

tasks.named("composeBuild") {
  group = "compose"
  rootProject.subprojects.forEach { proj ->
    if (proj.getProjectType() == ProjectType.SERVICE &&
      proj.getProjectLanguage() == ProjectLanguage.KOTLIN
    ) {
      dependsOn("${proj.path}:dockerfile")
      dependsOn("${proj.path}:buildLayers")
    }
    if (proj.getProjectType() == ProjectType.SERVICE &&
      proj.getProjectLanguage() == ProjectLanguage.JS
    ) {
      dependsOn("${proj.path}:dockerCreate")
    }
  }
}

tasks.named("composeUp") {
  group = "compose"
}

tasks.named("composeDown") {
  group = "compose"
}
