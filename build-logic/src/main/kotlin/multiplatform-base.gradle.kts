import com.github.gradle.node.npm.task.NpmTask
import utils.libs

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("com.github.node-gradle.node")
  id("dokka-convention")
}

val javaVersion: String by project
kotlin {
  jvmToolchain(javaVersion.toInt())

  jvm()

  js(IR) {
    binaries.library()
    generateTypeScriptDefinitions()
    nodejs()
    useCommonJs()

    compilations["main"].packageJson {
      customField("files", listOf("kotlin/"))
      customField("license", "Apache-2.0")
      customField(
        "repository",
        mapOf(
          "type" to "git",
          "url" to "git+https://github.com/Norby99/munchies",
        ),
      )
    }
  }

  sourceSets {
    all {
      languageSettings {
        optIn("kotlin.js.ExperimentalJsExport")
      }

      dependencies {
        implementation(libs().kotlinx.serialization.json)
      }
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs().kotlinx.serialization.json)
      }
    }
    val commonTest by getting {
      dependencies {
      }
    }
    val jvmMain by getting {
      dependencies {
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(kotlin("stdlib-js"))
      }
    }
  }
}

tasks.register<NpmTask>("pack_${project.name}") {
  println("Packing ${project.name}...")

  dependsOn(project.tasks.named("build"))

  val packageDir = rootProject.layout.buildDirectory
    .dir("js/packages/munchies-${project.name}").get().asFile

  workingDir.set(packageDir)
  args.set(listOf("pack"))

  inputs.dir(packageDir)
  outputs.file(packageDir.resolve("munchies-${project.name}-0.1.0.tgz"))
}
