
import utils.ProjectType
import utils.getProjectType
import utils.getServiceName

plugins {
  id("org.jetbrains.dokka")
}

if (project.getProjectType() == ProjectType.SERVICE) {

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
      for (item in rootProject.subprojects) {
        if (item.getProjectType() == ProjectType.SHARED) {
          val sharedName = "${getServiceName(item)}-shared"
          val jsDokkaTaskName = "dokkaGeneratePublicationHtml"
          val sharedProject = project(":$sharedName")

          if (sharedProject.tasks.findByName(jsDokkaTaskName) != null) {
            dependsOn(":$sharedName:$jsDokkaTaskName")
          }
        }
      }
    }
  }
}
