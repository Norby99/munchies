plugins {
    kotlin("multiplatform")
}

val javaVersion: String by project
kotlin {
    jvmToolchain(javaVersion.toInt())
}

kotlin {
    js {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs()
        binaries.executable()
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
