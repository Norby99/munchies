import utils.*

plugins {
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.dokka)
  id("munchies-subproject") apply false

  id("k8s.task")
  id("k8s.info")

  id("compose.task")
  id("compose.showDb")
}

apply(plugin = "linter-convention")

subprojects {
  if (this@subprojects.getProjectLanguage() == ProjectLanguage.KOTLIN) {
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
  val serviceProjects = subprojects.filter { it.getProjectType() == ProjectType.SERVICE }
  dependsOn(serviceProjects.map { "${it.path}:build" })

  val specSources = serviceProjects.map { subproject ->
    val sourcePath =
      subproject.layout.buildDirectory
        .dir(
          if (subproject.getProjectLanguage() == ProjectLanguage.KOTLIN) {
            "generated/ksp/main/resources/META-INF/swagger"
          } else {
            "openapi/"
          },
        ).get().asFile
    val targetName = "${getServiceName(subproject.parent!!)}.yml"
    sourcePath to targetName
  }
  val outputDir = rootProject.layout.buildDirectory.dir("openapi/").get().asFile

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

tasks.register<Sync>("prepareTypeDocs") {
  val typeDocsProjects = subprojects
    .filter { it.getProjectType() == ProjectType.SERVICE }
    .filter { it.getProjectLanguage() == ProjectLanguage.JS }
  dependsOn(typeDocsProjects.map { "${it.path}:typeDocs" })

  typeDocsProjects.forEach { subproject ->
    val sourcePath = subproject.layout.buildDirectory
      .dir("typedoc/").get().asFile
    val targetName = "${getServiceName(subproject)}-service"

    from(sourcePath) {
      into(targetName)
    }
  }

  into(rootProject.layout.buildDirectory.dir("typescript/"))
}

tasks.clean {
  delete(rootProject.projectDir.resolve("node_modules"))
}
