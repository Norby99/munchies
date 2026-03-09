package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.util.removeSuffixIfPresent

fun getProjectName(project: Project): String = project.name
fun getServiceName(project: Project): String = project.name.removeSuffixIfPresent("-service")
const val MUNCHIES_BASE_PACKAGE = "com.munchies"
fun getMunchiesServerPackage(service: String): String = "$MUNCHIES_BASE_PACKAGE.service.${service}.server"


fun Project.libs() = the<org.gradle.accessors.dm.LibrariesForLibs>()
