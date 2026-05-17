import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import utils.ProjectType
import utils.getProjectType


plugins {
  id("com.diffplug.spotless")
}

fun configureSpotlessForKotlin(project: Project) {
  project.configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt")
      ktlint()
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("**/build/**/*.kts")
      ktlint()
    }
  }
}

fun configureSpotlessForJs(project: Project) {
  project.configure<SpotlessExtension> {
    typescript {
      target("**/*.ts")
      targetExclude("**/build/**/*.ts", "**/node_modules/**/*.ts", "**/dist/**/*.ts")
      prettier()
    }
    javascript {
      target("**/*.js")
      targetExclude(
        "**/build/**/*.js",
        "**/node_modules/**/*.js",
        "**/dist/**/*.js",
        "**/*.cjs.js",
        "**/*.min.js",
      )
      prettier()
    }
  }

  project.pluginManager.withPlugin("com.github.node-gradle.node") {
    project.tasks.matching { it.name.startsWith("spotless") }.configureEach {
      mustRunAfter(project.tasks.matching { it.name == "npmInstall" })
    }
  }
}

apply<SpotlessPlugin>()

when (project.getProjectType()) {
  ProjectType.KOTLIN -> configureSpotlessForKotlin(project)
  ProjectType.JS -> configureSpotlessForJs(project)
}
