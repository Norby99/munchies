import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import utils.JS_TYPE
import utils.KOTLIN_TYPE
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
  KOTLIN_TYPE -> configureSpotlessForKotlin(project)
  JS_TYPE -> configureSpotlessForJs(project)
}
