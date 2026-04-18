import utils.ProjectType
import utils.getProjectType
import utils.getServiceName

plugins {
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.dokka)
  id("munchies-subproject") apply false
  id("kubernetes-tasks")
}

apply(plugin = "linter-convention")

subprojects {
  if (this@subprojects.getProjectType() == ProjectType.KOTLIN) {
    plugins.withId("org.jetbrains.dokka") {
      rootProject.dependencies {
        "dokka"(project(this@subprojects.path))
      }
    }
  }

  apply(plugin = "munchies-subproject")
  apply(plugin = "linter-convention")
}

tasks.register("prepareOpenApiSpecs") {
  val serviceProjects = subprojects.filter { it.name.matches(Regex("service")) }
  dependsOn(serviceProjects.map { "${it.path}:build" })

  val specSources = serviceProjects.map { subproject ->
    val sourcePath =
      subproject.layout.buildDirectory
        .dir(
          if (subproject.getProjectType() == ProjectType.KOTLIN) {
            "generated/ksp/main/resources/META-INF/swagger"
          } else {
            "openapi/"
          },
        ).get().asFile
    val targetName = "${getServiceName(subproject.parent!!)}.yml"
    sourcePath to targetName
  }
  val outputDir = rootProject.layout.buildDirectory.dir("openapi/").get().asFile

  println("Spec sources: $specSources")
  inputs.files(specSources.map { (dir, _) -> dir })
  outputs.dir(outputDir)

  doLast {
    if (outputDir.exists().not()) outputDir.mkdir()
    specSources.forEach { (sourceDir, targetName) ->
      sourceDir.listFiles { f -> f.extension == "yml" || f.extension == "yaml" }
        ?.forEach { file ->
          file.copyTo(outputDir.resolve(targetName), overwrite = true)
        }
    }
  }
}
