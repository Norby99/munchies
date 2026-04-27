package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.util.removeSuffixIfPresent

fun getProjectName(project: Project): String = project.name
fun getServiceName(project: Project): String {
  if (project.name in listOf("client", "shared", "service")) {
    return getServiceName(project.parent!!)
  }
  return project.name.removeSuffixIfPresent("-service")
}

const val MUNCHIES_BASE_PACKAGE = "com.munchies"

fun Project.libs() = the<org.gradle.accessors.dm.LibrariesForLibs>()

enum class ProjectType {
  KOTLIN,
  JS,
}

fun Project.getProjectType(): ProjectType {
  val stringKotlinProjects = listOf(
    "commons",
    "architecture-rules",
    "user",
    "restaurant",
    "scheduler",
  )
  val stringExpressProjects = listOf(
    "payment",
    "gateway",
    "order",
    "table",
    "notification",
  )
  return when (
    this.parent?.name
      ?.replace("-service", "")
      ?.replace("client", "")
      ?.replace("service", "")
      ?.replace("shared", "")
      ?.replace(":", "")
  ) {
    null -> ProjectType.KOTLIN
    "munchies" -> ProjectType.KOTLIN
    in stringKotlinProjects -> ProjectType.KOTLIN
    in stringExpressProjects -> ProjectType.JS
    else -> throw IllegalArgumentException("Unknown project type for project ${this.name}")
  }
}
