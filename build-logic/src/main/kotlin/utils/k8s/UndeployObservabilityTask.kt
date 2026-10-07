package utils.k8s

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Uninstalls the Helm releases of the observability stack. The namespace, and with it any
 * leftover volume, is only deleted when [wipeData] is set, mirroring [UndeployServicesTask].
 */
abstract class UndeployObservabilityTask @Inject constructor(
  private val execOps: ExecOperations,
  objects: ObjectFactory,
) : DefaultTask() {

  @get:Input
  val wipeData: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

  @TaskAction
  fun undeploy() {
    observabilityReleases.reversed().forEach { release ->
      println("Uninstalling Helm release ${release.name} (observability)...")
      execOps.exec {
        commandLine("helm", "uninstall", release.name, "-n", OBSERVABILITY_NAMESPACE)
        isIgnoreExitValue = true
      }
    }

    if (wipeData.get()) {
      println("Deleting namespace $OBSERVABILITY_NAMESPACE...")
      execOps.exec {
        commandLine(
          "minikube",
          "kubectl",
          "--",
          "delete",
          "namespace",
          OBSERVABILITY_NAMESPACE,
          "--ignore-not-found",
        )
      }
    }
    println("Observability stack undeployed!")
  }
}
