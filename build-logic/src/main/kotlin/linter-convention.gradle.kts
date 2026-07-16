
import com.diffplug.gradle.spotless.*
import utils.ProjectLanguage
import utils.getProjectLanguage


plugins {
  id("com.diffplug.spotless")
}

private fun ktlintConfigOverride(ktlint: BaseKotlinExtension.KtlintConfig) {
  ktlint.editorConfigOverride(
    mapOf(
      "ktlint_standard_no-wildcard-imports" to "disabled",
    ),
  )
}

private fun KotlinExtension.applyKtlintConfig() {
  ktlintConfigOverride(ktlint())
}
private fun KotlinGradleExtension.applyKtlintConfig() {
  ktlintConfigOverride(ktlint())
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
      targetExclude(
        "**/node_modules/**/*.ts",
        "**/dist/**/*.ts",
        "build/**",
        "**/resources/*.js",
      )
      prettier()
    }
    javascript {
      target("src/**/*.js")
      targetExclude(
        "**/node_modules/**/*.js",
        "**/dist/**/*.js",
        "**/*.cjs.js",
        "**/*.min.js",
        "**/resources/*.d.ts",
      )
      prettier()
    }
  }

  project.tasks.matching { it.name.startsWith("spotless") }.configureEach {
    dependsOn(project.tasks.named("npmInstall"))
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
