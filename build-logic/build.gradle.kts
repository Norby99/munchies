plugins {
  `kotlin-dsl`
}

repositories {
  google()
  mavenCentral()
  gradlePluginPortal()
}

fun fromPluginToDependency(plugin: Provider<PluginDependency>): String =
  "${plugin.get().pluginId}:${plugin.get().pluginId}.gradle.plugin:${plugin.get().version}"

dependencies {
  implementation(libs.kotlin.multiplatform.plugin)
  // Makes libs.* accessible inside convention plugins
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  // For a plugin, you pull it in as a classpath dependency like this
  implementation(fromPluginToDependency(libs.plugins.openapi.generator))
}