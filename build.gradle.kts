import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import utils.getServiceName

plugins {
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.dokka)
  id("munchies-subproject") apply false
  id("kubernetes-tasks")
}

allprojects {
  group = "munchies"
}

fun configureSpotlessForKotlin(project: Project) {
  project.configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      ktlint()
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      ktlint()
    }
  }
}
apply<SpotlessPlugin>()
configureSpotlessForKotlin(rootProject)

dokka {
  dokkaPublications.html {
    outputDirectory.set(
      rootProject.layout.buildDirectory
        .dir("docs/html"),
    )
  }
}

subprojects {
  apply<SpotlessPlugin>()
  configureSpotlessForKotlin(this)

  plugins.withId("org.jetbrains.dokka") {
    rootProject.dependencies {
      "dokka"(project(this@subprojects.path))
    }
  }

  apply(plugin = "munchies-subproject")
}

tasks.register("prepareOpenApiSpecs") {
  val serviceProjects = subprojects.filter { it.name.matches(Regex("service")) }

  dependsOn(serviceProjects.map { "${it.path}:build" })

  val specSources = serviceProjects.map { subproject ->
    val sourcePath = subproject.layout.buildDirectory
      .dir("generated/ksp/main/resources/META-INF/swagger")
      .get().asFile
    val targetName = "${getServiceName(subproject.parent!!)}.yml"
    sourcePath to targetName
  }
  val outputDir = rootProject.layout.buildDirectory.dir("openapi/").get().asFile

  inputs.files(specSources.map { (dir, _) -> dir })
  outputs.dir(outputDir)

  doLast {
    if (outputDir.exists().not()) outputDir.mkdir()
    specSources.forEach { (sourceDir, targetName) ->
      sourceDir.listFiles { f -> f.extension == "yml" }
        ?.forEach { file ->
          file.copyTo(outputDir.resolve(targetName), overwrite = true)
        }
    }
  }
}
