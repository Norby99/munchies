package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.util.removeSuffixIfPresent

fun getProjectName(project: Project): String = project.name
fun getServiceName(project: Project): String {
  val projectName = getProjectName(project)
  return projectName
    .removeSuffixIfPresent("-service")
    .removeSuffixIfPresent("-client")
    .removeSuffixIfPresent("-shared")
}

const val MUNCHIES_BASE_PACKAGE = "com.munchies"

fun Project.libs() = the<org.gradle.accessors.dm.LibrariesForLibs>()

enum class ProjectLanguage {
  KOTLIN,
  JS,
}

fun Project.getProjectLanguage(): ProjectLanguage {
  val stringKotlinProjects = listOf(
    "commons",
    "architecture-rules",
    "user",
    "order",
    "restaurant",
    "scheduler",
  )
  val stringExpressProjects = listOf(
    "payment",
    "gateway",
    "table-reservation",
    "notification",
  )
  return when (
    this.name
      .replace("-service", "")
      .replace("-client", "")
      .replace("-service", "")
      .replace("-shared", "")
      .replace(":", "")
  ) {
    in stringKotlinProjects -> ProjectLanguage.KOTLIN
    in stringExpressProjects -> ProjectLanguage.JS
    else -> throw IllegalArgumentException("Unknown project type for project ${this.name}")
  }
}

enum class ProjectType {
  SERVICE,
  SHARED,
  CLIENT,
  UTILS,
}

fun Project.getProjectType(): ProjectType {
  val exlcuded = listOf(
    "commons",
    "architecture-rules",
  )

  return when {
    name.endsWith("-service") -> ProjectType.SERVICE
    name.endsWith("-shared") -> ProjectType.SHARED
    name.endsWith("-client") -> ProjectType.CLIENT
    name in exlcuded -> ProjectType.UTILS
    else -> throw IllegalArgumentException("Unknown project type for project ${this.name}")
  }
}
