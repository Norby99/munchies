import com.github.gradle.node.task.NodeTask
import utils.getServiceName

plugins {
  id("com.github.node-gradle.node")
}

val serviceName = getServiceName(project.parent!!)

tasks.named("build") {
  dependsOn(project(":commons").tasks.named("build"))
  dependsOn(project(":$serviceName-service:shared").tasks.named("build"))
}

tasks.register<NodeTask>("run") {
  dependsOn(
    project.tasks.named("build"),
    "npmInstall",
    "npm_run_build",
  )

  script = file(project.projectDir.resolve("dist/index.js").path)
}
