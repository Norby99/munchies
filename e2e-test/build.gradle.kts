//@file:Suppress("UnstableApiUsage")
import org.gradle.kotlin.dsl.kotlin
import utils.libs

plugins {
  kotlin("jvm")
  id("jvm-test-suite")
  id("java-test-fixtures")

}

dependencies {
  implementation(libs().konsist)
  implementation(libs().kotest)

  implementation(project(":order-shared"))
  implementation(project(":restaurant-shared"))
  implementation(project(":user-shared"))
  implementation(project(":payment-shared"))
}


tasks.register("e2eTest") {
  group = "verification"
  dependsOn(rootProject.tasks.named("composeUp"))
  finalizedBy(rootProject.tasks.named("composeDown"))
}
