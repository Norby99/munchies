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
  // Makes libs.* accessible inside convention plugins
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(fromPluginToDependency(libs.plugins.kotlin.multiplatform))
  implementation(fromPluginToDependency(libs.plugins.ksp))
  implementation(fromPluginToDependency(libs.plugins.detekt))
  implementation(fromPluginToDependency(libs.plugins.kover))
  implementation(fromPluginToDependency(libs.plugins.dokka))
  implementation(fromPluginToDependency(libs.plugins.spotless))

  implementation(fromPluginToDependency(libs.plugins.allopen))
  implementation(fromPluginToDependency(libs.plugins.shadow))

  implementation(fromPluginToDependency(libs.plugins.micronaut.application))
  implementation(fromPluginToDependency(libs.plugins.micronaut.aot))

  implementation(libs.micronaut.buildtools)
  implementation(fromPluginToDependency(libs.plugins.micronaut.testResources))
}
