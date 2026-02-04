plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
}

allprojects {
    repositories {
        mavenCentral()
    }
}