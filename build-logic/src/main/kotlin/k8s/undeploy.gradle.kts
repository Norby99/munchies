package k8s

plugins {
  id("k8s.discover-services")
}

tasks.register("undeployServices") {
  dependsOn("discoverServices")

  doLast {
    @Suppress("UNCHECKED_CAST")
    val services =
      project.extra["discoveredServices"] as List<String>

    val wipeData =
      (project.findProperty("wipeData") as? String)
        ?.toBoolean() ?: false

    services.forEach { srv ->
      println("===========================================")
      println("Undeploying: $srv")
      println("===========================================")

      val singleManifest = file("k8s/$srv.yml")
      val manifestDir = file("k8s/$srv")

      when {
        singleManifest.exists() -> {
          println("Deleting resources from k8s/$srv.yml...")
          exec {
            commandLine(
              "minikube",
              "kubectl",
              "--",
              "delete",
              "-f",
              singleManifest,
              "--ignore-not-found",
            )
          }
        }

        manifestDir.exists() -> {
          if (wipeData) {
            println("Deleting all resources from k8s/$srv/...")
            exec {
              commandLine(
                "minikube",
                "kubectl",
                "--",
                "delete",
                "-f",
                "k8s/$srv/",
                "--ignore-not-found",
              )
            }
          } else {
            println(
              "Deleting resources from k8s/$srv/ " +
                "(excluding PVC and Namespace)...",
            )

            manifestDir
              .walkTopDown()
              .filter {
                it.isFile &&
                  (it.extension == "yml" || it.extension == "yaml")
              }
              .filterNot {
                "pvc" in it.name || "namespace" in it.name
              }
              .forEach { manifest ->
                exec {
                  commandLine(
                    "minikube",
                    "kubectl",
                    "--",
                    "delete",
                    "-f",
                    manifest,
                    "--ignore-not-found",
                  )
                }
              }
          }
        }

        else -> println(
          "Warning: No manifest found for $srv. " +
            "Attempting namespace deletion anyway...",
        )
      }

      if (wipeData) {
        println("Wiping PersistentVolumeClaims for $srv...")
        exec {
          commandLine(
            "minikube",
            "kubectl",
            "--",
            "delete",
            "pvc",
            "--all",
            "-n",
            srv,
            "--ignore-not-found",
          )
        }

        println("Deleting namespace $srv...")
        exec {
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
        println(
          "Keeping PersistentVolumeClaims " +
            "(pass '-PwipeData=true' to delete them).",
        )
      }

      println()
    }

    println("Undeploy Completed!")
  }
}
