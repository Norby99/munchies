plugins {
    kotlin("multiplatform")
}

val javaVersion: String by project
kotlin {
    jvmToolchain(javaVersion.toInt())
    js (IR) {
        nodejs{
            testTask {
                enabled = false
            }
        }
        binaries.executable()
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
