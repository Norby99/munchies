import utils.ProjectType
import utils.getProjectType
import utils.getServiceName

plugins {
  id("org.jetbrains.dokka")
}

if (project.getProjectType() == ProjectType.SERVICE) {
  dokka {
    moduleName = getServiceName(project) + "-" + project.getProjectType().toString().lowercase()
    dokkaPublications.html {
      failOnWarning = true
    }
  }
}
