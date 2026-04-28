import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeTask
import utils.getServiceName

plugins {
  id("com.github.node-gradle.node")
  id("com.bmuschko.docker-remote-api")
}

val jsImplementation = configurations.create("jsImplementation") {
  isCanBeConsumed = false
  isCanBeResolved = true
}

val nodeVersion = "24.0.0"
val nodePackageVersion = "11.0.0"

node {
  version.set(nodeVersion)
  npmVersion.set(nodePackageVersion)
}

val serviceName = getServiceName(project.parent!!)

tasks.named("build") {

  dependsOn(
    "npmInstall",
    "npm_run_build",
  )
}

tasks.named("npm_run_build") {
  mustRunAfter("npmInstall")
}

tasks.register("test") {
  dependsOn(project.tasks.named("build"))
  dependsOn("npm_run_test")
}

tasks.register<NodeTask>("run") {
  dependsOn(
    project.tasks.named("build"),
  )

  script = file(project.projectDir.resolve("dist/main/ts/index.js").path)
}

tasks.named("clean") {
  delete(project.projectDir.resolve("dist"))
  delete(project.projectDir.resolve("node_modules"))
  delete(project.projectDir.resolve("build"))
}

tasks.register("dockerCreate", Dockerfile::class) {
  from("node:$nodeVersion-alpine")
  workingDir("/app")
  copyFile("./", "/app/")
  runCommand("npm install && npm run build")
  exposePort(3000)
  defaultCommand("npm", "start")

  destFile = project.layout.buildDirectory.dir("docker/main/Dockerfile").get().asFile

  val sources = listOf(
    project.projectDir.resolve("src/"),
    project.projectDir.resolve("package.json"),
    project.projectDir.resolve("package-lock.json"),
    project.projectDir.resolve("tsconfig.json"),
    project.projectDir.resolve(".env"),
  )

  inputs.files(sources)
  val outputDir = project.layout.buildDirectory.dir("docker/main/").get().asFile
  outputs.dir(outputDir)

  doLast {
    sources.forEach { source ->
      source.copyRecursively(
        outputDir
          .resolve(source.name),
        overwrite = true,
      )
    }
  }
}

tasks.register<DockerBuildImage>("dockerBuild") {
  dependsOn("dockerCreate")
  inputDir.set(project.layout.buildDirectory.dir("docker/main/"))
  images.set(listOf("$serviceName-service:latest"))
}

tasks.register<NpxTask>("typeDocs") {
  dependsOn(project.tasks.named("build"))
  dependsOn(
    "npm_run_routes",
    "npm_run_specs",
  )
  command.set("typedoc")
  workingDir.set(project.projectDir.resolve("src"))
  args.set(
    listOf(
      "--entryPointStrategy",
      "expand",
      "main",
      "--readme",
      "none",
      "--out",
      project.layout.buildDirectory.dir("typedoc/").get().asFile.path,
    ),
  )
}
tasks.register("vitestCoverageVerify") {
  mustRunAfter(project.tasks.named("test"))
  dependsOn("npm_run_coverage")
}
