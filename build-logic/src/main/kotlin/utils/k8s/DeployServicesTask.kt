package utils.k8s

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class DeployServicesTask @Inject constructor(
  private val execOps: ExecOperations,
  objects: ObjectFactory,
) : DefaultTask() {

  @get:Input
  val services: ListProperty<String> = objects.listProperty(String::class.java)

  @get:InputDirectory
  val rootDir: DirectoryProperty = objects.directoryProperty()

  /**
   * When set, the Docker images are not rebuilt: the ones already present locally are loaded into
   * Minikube as they are. Building compiles the services while the cluster is running, which does
   * not fit in memory on small machines; this lets the images be built beforehand, with the
   * cluster stopped.
   */
  @get:Input
  val skipBuild: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

  /** Services to leave out of this deployment, e.g. the ones not needed for a demo. */
  @get:Input
  val excluded: ListProperty<String> = objects.listProperty(String::class.java)

  @TaskAction
  fun deploy() {
    val skipped = excluded.get().toSet()
    services.get().filterNot { it in skipped }.forEach { srv ->
      println("===========================================")
      println("Processing: $srv")
      println("===========================================")

      val root = rootDir.get().asFile
      val srvDir = root.resolve(srv)
      when {
        !srvDir.exists() ->
          println("Skipping Docker image build for $srv...")
        skipBuild.get() ->
          println("Skipping Docker image build for $srv (skipBuild): using the existing image...")
        root.resolve("$srv/service/build.gradle.kts").exists() -> {
          println("Building Docker image for $srv (using :$srv:service)...")
          execOps.exec { commandLine("./gradlew", ":$srv:service:dockerBuild") }
          execOps.exec { commandLine("docker", "tag", "service:latest", "$srv:latest") }
        }
        else -> {
          println("Building Docker image for $srv using Gradle...")
          execOps.exec { commandLine("./gradlew", ":$srv:dockerBuild") }
        }
      }

      loadImageIntoMinikube(srv)

      val helmValuesFile = root.resolve("helm/values/$srv.yaml")
      val helmChart = root.resolve("helm/munchies-service")
      val singleManifest = root.resolve("k8s/$srv.yml")
      val manifestDir = root.resolve("k8s/$srv")
      when {
        helmValuesFile.exists() -> {
          println("Deploying $srv via Helm (helm/munchies-service, -f ${helmValuesFile.name})...")
          execOps.exec {
            commandLine(
              "helm",
              "upgrade",
              "--install",
              srv,
              helmChart.absolutePath,
              "-n",
              srv,
              "--create-namespace",
              "-f",
              helmValuesFile.absolutePath,
            )
          }
        }
        singleManifest.exists() -> {
          println("Applying Kubernetes manifest $singleManifest...")
          execOps.exec {
            commandLine("minikube", "kubectl", "--", "apply", "-f", singleManifest)
          }
        }
        manifestDir.exists() -> {
          println("Applying namespace first, then full manifest in k8s/$srv/...")
          execOps.exec {
            commandLine(
              "minikube",
              "kubectl",
              "--",
              "apply",
              "-f",
              manifestDir.resolve("namespace.yml"),
            )
          }
          execOps.exec {
            commandLine("minikube", "kubectl", "--", "apply", "-f", manifestDir, "-n", srv)
          }
        }
        else -> {
          println("Warning: No manifest found for $srv.")
          return@forEach
        }
      }

      execOps.exec {
        commandLine("minikube", "kubectl", "--", "rollout", "restart", "deployment", "-n", srv)
        isIgnoreExitValue = true
      }
      execOps.exec {
        commandLine("minikube", "kubectl", "--", "rollout", "restart", "statefulset", "-n", srv)
        isIgnoreExitValue = true
      }
    }
    println("Deployment Completed!")
  }

  private fun loadImageIntoMinikube(srv: String) {
    println("Pushing $srv:latest to Minikube...")
    execOps.exec {
      commandLine("minikube", "image", "load", "$srv:latest")
    }
  }
}
