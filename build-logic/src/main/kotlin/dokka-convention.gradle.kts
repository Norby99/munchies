import utils.getServiceName

plugins {
  id("org.jetbrains.dokka")
}

dokka {
  if (project.parent?.name?.matches(Regex(".*-service")) ?: false) {
    moduleName = getServiceName(project.parent!!) + "-" + project.name
  }
}
