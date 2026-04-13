package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.util.removeSuffixIfPresent

fun getProjectName(project: Project): String = project.name
fun getServiceName(project: Project): String = project.name.removeSuffixIfPresent("-service")
const val MUNCHIES_BASE_PACKAGE = "com.munchies"
fun getMunchiesServerPackage(service: String): String =
  "$MUNCHIES_BASE_PACKAGE.service.$service.server"

fun Project.libs() = the<org.gradle.accessors.dm.LibrariesForLibs>()

typealias ProjectType = String

const val KOTLIN_TYPE: ProjectType = "KOTLIN"
const val JS_TYPE: ProjectType = "JS"

fun Project.getProjectType(): ProjectType {
  return when (
    this.parent?.name
      ?.replace("-service", "")
      ?.replace("client", "")
      ?.replace("service", "")
      ?.replace("shared", "")
  ) {
    null -> KOTLIN_TYPE
    "munchies" -> KOTLIN_TYPE
    in stringKotlinProjects -> KOTLIN_TYPE
    in stringExpressProjects -> JS_TYPE
    else -> throw IllegalArgumentException("Unknown project type for project ${this.name}")
  }
}

private val stringKotlinProjects = listOf(
  "commons",
  "architecture-rules",
  "user",
  "restaurant",
  "scheduler",
)

private val stringExpressProjects = listOf(
  "payment",
  "gateway",
  "order",
  "table",
  "notification",
)
