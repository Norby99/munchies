val kotlin_version: String by extra
plugins {
    kotlin("multiplatform")
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

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.js)
            }
        }

        val jsTest by getting
    }
}