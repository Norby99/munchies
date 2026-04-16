import com.github.gradle.node.task.NodeTask

plugins {
  id("com.github.node-gradle.node")
}

tasks.named("build") {
  dependsOn(project(":commons").tasks.named("build"))
}

tasks.register<NodeTask>("run") {
  dependsOn(
    ":commons:jsPublicPackageJson",
    ":commons:jsProductionExecutableCompileSync",
    "npmInstall",
    "npm_run_build",
  )

  script = file(project.projectDir.resolve("dist/index.js").path)
}
