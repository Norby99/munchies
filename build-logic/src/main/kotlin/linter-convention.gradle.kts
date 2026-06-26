import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.KotlinGradleExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import utils.ProjectLanguage
import utils.getProjectLanguage


plugins {
  id("com.diffplug.spotless")
}
private fun KotlinExtension.applyKtlintConfig() {
  ktlint().editorConfigOverride(
    mapOf(
      "ktlint_standard_no-wildcard-imports" to "disabled",
    ),
  )
}
private fun KotlinGradleExtension.applyKtlintConfig() {
  ktlint().editorConfigOverride(
    mapOf(
      "ktlint_standard_no-wildcard-imports" to "disabled",
    ),
  )
}

fun configureSpotlessForKotlin(project: Project) {
  project.configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      applyKtlintConfig()
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      applyKtlintConfig()
    }
  }
}

fun configureSpotlessForJs(project: Project) {
  project.configure<SpotlessExtension> {
    typescript {
      target("src/**/*.ts")
      targetExclude("**/node_modules/**/*.ts", "**/dist/**/*.ts", "build/**")
      BiomeTs("2.1.0")
    }
    javascript {
      target("src/**/*.js")
      targetExclude(
        "**/node_modules/**/*.js",
        "**/dist/**/*.js",
        "**/*.cjs.js",
        "**/*.min.js",
      )
      BiomeJs("2.1.0")
    }
  }

  /*project.pluginManager.withPlugin("com.github.node-gradle.node") {
    project.tasks.matching { it.name.startsWith("spotless") }.configureEach {
      mustRunAfter(project.tasks.matching { it.name == "npmInstall" })
    }
  }*/
}

apply<SpotlessPlugin>()

when (project.getProjectLanguage()) {
  ProjectLanguage.KOTLIN -> configureSpotlessForKotlin(project)
  ProjectLanguage.JS -> configureSpotlessForJs(project)
}
