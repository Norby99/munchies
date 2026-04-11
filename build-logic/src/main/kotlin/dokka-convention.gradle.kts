import org.gradle.kotlin.dsl.project
import utils.getServiceName

plugins {
  id("org.jetbrains.dokka")
}

dokka {
  if (project.parent?.name?.matches(Regex(".*-service")) ?: false) {
    moduleName = getServiceName(project.parent!!) + "-" + project.name
  }
}

dokka {
  dokkaPublications.html {
    outputDirectory.set(
      rootProject.layout.buildDirectory
        .dir("docs/html"),
    )
  }
}
