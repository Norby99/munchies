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

abstract class UndeployServicesTask @Inject constructor(
  private val execOps: ExecOperations,
  objects: ObjectFactory,
) : DefaultTask() {

  @get:Input
  val services: ListProperty<String> = objects.listProperty(String::class.java)

  @get:InputDirectory
  val rootDir: DirectoryProperty = objects.directoryProperty()

  @get:Input
  val wipeData: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

  @TaskAction
  fun undeploy() {
    val root = rootDir.get().asFile
    val wipe = wipeData.get()

    services.get().forEach { srv ->
      println("===========================================")
      println("Undeploying: $srv")
      println("===========================================")

      val singleManifest = root.resolve("k8s/$srv.yml")
      val manifestDir = root.resolve("k8s/$srv")

      when {
        singleManifest.exists() -> {
          println("Deleting resources from k8s/$srv.yml...")
          execOps.exec {
            commandLine(
              "minikube",
              "kubectl",
              "--",
              "delete",
              "-f",
              singleManifest.absolutePath,
              "--ignore-not-found",
            )
          }
        }

        manifestDir.exists() -> {
          if (wipe) {
            println("Deleting all resources from k8s/$srv/...")
            execOps.exec {
              commandLine(
                "minikube",
                "kubectl",
                "--",
                "delete",
                "-f",
                manifestDir.absolutePath,
                "--ignore-not-found",
              )
            }
          } else {
            println("Deleting resources from k8s/$srv/ (excluding PVC and Namespace)...")

            manifestDir
              .walkTopDown()
              .filter {
                it.isFile && (it.extension == "yml" || it.extension == "yaml")
              }
              .filterNot {
                "pvc" in it.name || "namespace" in it.name
              }
              .forEach { manifest ->
                execOps.exec {
                  commandLine(
                    "minikube",
                    "kubectl",
                    "--",
                    "delete",
                    "-f",
                    manifest.absolutePath,
                    "--ignore-not-found",
                  )
                }
              }
          }
        }

        else -> println(
          "Warning: No manifest found for $srv. Attempting namespace deletion anyway...",
        )
      }

      if (wipe) {
        println("Wiping PersistentVolumeClaims for $srv...")
        execOps.exec {
          commandLine(
            "minikube", "kubectl", "--", "delete", "pvc", "--all", "-n", srv, "--ignore-not-found",
          )
        }

        println("Deleting namespace $srv...")
        execOps.exec {
          commandLine(
            "minikube",
            "kubectl",
            "--",
            "delete",
            "namespace",
            srv,
            "--ignore-not-found",
          )
        }
      } else {
        println("Keeping PersistentVolumeClaims (pass '-PwipeData=true' to delete them).")
      }

      println()
    }

    println("Undeploy Completed!")
  }
}
