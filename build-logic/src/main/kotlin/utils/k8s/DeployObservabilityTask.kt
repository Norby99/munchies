package utils.k8s

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Installs (or upgrades) the observability stack into the cluster, one Helm release per entry of
 * [observabilityReleases]. Unlike the services, these are upstream charts: there is no image to
 * build or load into Minikube, only values files under `observability/values`.
 */
abstract class DeployObservabilityTask @Inject constructor(
  private val execOps: ExecOperations,
  objects: ObjectFactory,
) : DefaultTask() {

  @get:InputDirectory
  val valuesDir: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun deploy() {
    val values = valuesDir.get().asFile

    observabilityReleases.forEach { release ->
      println("===========================================")
      println("Processing: ${release.name} (observability)")
      println("===========================================")

      execOps.exec {
        commandLine("helm", "repo", "add", release.repoName, release.repoUrl, "--force-update")
      }
      execOps.exec {
        commandLine("helm", "repo", "update", release.repoName)
      }
      execOps.exec {
        commandLine(
          "helm",
          "upgrade",
          "--install",
          release.name,
          "${release.repoName}/${release.chart}",
          "-n",
          OBSERVABILITY_NAMESPACE,
          "--create-namespace",
          "-f",
          values.resolve(release.valuesFile).absolutePath,
        )
      }
    }
    println("Observability stack deployed!")
  }
}
