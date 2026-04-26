plugins {
  kotlin("multiplatform")
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
      customField("files", "kotlin/")
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
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
      }
    }
    val commonTest by getting {
      dependencies {
      }
    }
    val jsMain by getting {
    }
  }
}
