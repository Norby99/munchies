import utils.ProjectType
import utils.getProjectName
import utils.getProjectType
import utils.getServiceName

plugins {
  id("org.jetbrains.dokka")
}

if (project.getProjectType() != ProjectType.UTILS) {

  dokka {
    moduleName = getServiceName(project) + "-" + project.getProjectType().toString().lowercase()
  }

  dokka {
    dokkaPublications.html {
      outputDirectory.set(
        rootProject.layout.buildDirectory
          .dir("docs/html"),
      )
      failOnWarning = true
    }
  }

  if (project.getProjectType() == ProjectType.SERVICE) {
    project.tasks.dokkaGeneratePublicationHtml {
      dependsOn(":${getServiceName(project)}-shared:jsDokkaJavadocJar")
    }
  }
}
