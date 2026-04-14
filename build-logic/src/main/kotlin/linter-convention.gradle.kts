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
      targetExclude("**/build/**/*.ts")
      prettier()
    }
    javascript {
      target("**/*.js")
      targetExclude("**/build/**/*.js")
      prettier()
    }
  }
}

apply<SpotlessPlugin>()

when (project.getProjectType()) {
  ProjectType.KOTLIN -> configureSpotlessForKotlin(project)
  ProjectType.JS -> configureSpotlessForJs(project)
}
