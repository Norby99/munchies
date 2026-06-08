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

afterEvaluate {
  val input = project.configurations["jsImplementation"]
    .dependencies
    .map { it.name }

  tasks.register("printJsDeps") {
    doLast {
      println(input)
    }
  }

  val packTasks = input.map { jsDep ->
    ":$jsDep:pack_$jsDep"
  }

  tasks.register("moveJsDeps") {
    dependsOn("printJsDeps")
    dependsOn(packTasks)

    val sources = input.map { jsDep ->
      rootProject.layout.buildDirectory
        .file("js/packages/munchies-$jsDep/")
        .get().asFile to "munchies-$jsDep.tgz"
    }
    val output = project.layout.buildDirectory.dir("libs/").get().asFile

    inputs.files(sources.map { (f, _) -> f })
    outputs.dir(output)

    doLast {
      if (!output.exists()) output.mkdirs()
      sources.forEach { (sourceDir, targetName) ->
        sourceDir.listFiles { f ->
          f.isFile && f.extension != "json" // .tgz conflicted
        }?.forEach { file ->
          println("Copying ${file.name} to ${output.resolve(targetName).path}")
          file.copyTo(output.resolve(targetName), overwrite = true)
        }
      }
    }
  }

  tasks.named("npmInstall") {
    dependsOn("moveJsDeps")
    // dependsOn("addNpmLocalPaths")
    // mustRunAfter("addNpmLocalPaths")
  }

  tasks.named("npm_run_build") {
    mustRunAfter("npmInstall")
  }
  tasks.named("npm_run_specs") {
    mustRunAfter("npm_run_build")
  }
}

tasks.named("build") {
  dependsOn("moveJsDeps")

  dependsOn(
    "npmInstall",
    "npm_run_build",
    "npm_run_specs",
  )
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
  dependsOn(project.tasks.named("build"))

  from("node:$nodeVersion-alpine")
  workingDir("/app")
  copyFile("./", "/app/")
  runCommand("npm install && npm run build")

  defaultCommand("npm", "start")

  destFile = project.layout.buildDirectory.dir("docker/main/Dockerfile").get().asFile

  val sources = listOf(
    project.projectDir.resolve("src/"),
    project.projectDir.resolve("package.json"),
    project.projectDir.resolve("package-lock.json"),
    project.projectDir.resolve("tsconfig.json"),
    project.projectDir.resolve(".env"),
  )
  val libsDir = project.layout.buildDirectory.dir("libs").get().asFile

  inputs.files(sources)
  inputs.dir(libsDir)
  val outputDir = project.layout.buildDirectory.dir("docker/main/").get().asFile
  outputs.dir(outputDir)

  doLast {
    sources.forEach { source ->
      if (source.exists()) {
        source.copyRecursively(
          outputDir.resolve(source.name),
          overwrite = true,
        )
      }
    }

    val targetLibsDir = outputDir.resolve("build/libs")
    if (!libsDir.exists()) {
      targetLibsDir.mkdirs()
    }
    libsDir.listFiles { f -> f.isFile && f.extension == "tgz" }?.forEach { tgz ->
      tgz.copyTo(targetLibsDir.resolve(tgz.name), overwrite = true)
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
