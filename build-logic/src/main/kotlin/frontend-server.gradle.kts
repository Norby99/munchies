import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import utils.getServiceName

plugins {
  base
  id("com.bmuschko.docker-remote-api")
}

// The application is compiled inside the image (see frontend-service/Dockerfile), so unlike the
// Express services this needs no Node.js on the host.
tasks.register<DockerBuildImage>("dockerBuild") {
  group = "munchies"
  inputDir.set(layout.projectDirectory)
  images.set(listOf("${getServiceName(project)}-service:latest"))
}
