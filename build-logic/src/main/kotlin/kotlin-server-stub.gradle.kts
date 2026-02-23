import utils.getServiceName
import utils.libs
import utils.munchiesBasePackage

plugins {
    id("kotlin-jvm")
    id("io.micronaut.application")
    id("io.micronaut.openapi")
    kotlin("kapt")
    application
}
dependencies {
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation(libs().jakarta.validation.api)
    implementation("io.micronaut:micronaut-http-client")

    runtimeOnly("io.micronaut:micronaut-http-server-netty")
}

micronaut {
    openapi {
        server(file("${projectDir}/src/main/yaml/api/openapi.yaml")) {
            val servicePackage = "$munchiesBasePackage.${getServiceName(project)}"
            apiPackageName = "$servicePackage.api"
            modelPackageName = "$servicePackage.model"
            controllerPackage = "$servicePackage.controller"
            lang.set("kotlin")
        }
    }
}

application {
    mainClass = "$munchiesBasePackage.${getServiceName(project)}.MainKt"
}