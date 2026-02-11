import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin

plugins {
    alias    (libs.plugins.spotless)     apply false
}

subprojects {
    apply<SpotlessPlugin>()
    configure<SpotlessExtension>{
        kotlin {
            target    ("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint()
        }
        kotlinGradle {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint()
        }
    }
}
