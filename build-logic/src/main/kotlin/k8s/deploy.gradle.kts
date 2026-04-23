package k8s

plugins {
  id("k8s.discover-services")
}

tasks.register("deployServices") {
  dependsOn("discoverServices")

  doLast {
    @Suppress("UNCHECKED_CAST")
    val services = project.extra["discoveredServices"] as List<String>

    services.forEach { srv ->
      println("===========================================")
      println("Processing: $srv")
      println("===========================================")

      // Docker build
      val srvDir = file(srv)
      when {
        !srvDir.exists() ->
          println("Skipping Docker image build for $srv...")

        file("$srv/service/build.gradle.kts").exists() -> {
          println("Building Docker image for $srv (using :$srv:service)...")
          exec { commandLine("./gradlew", ":$srv:service:dockerBuild") }
          exec { commandLine("docker", "tag", "service:latest", "$srv:latest") }
        }

        else -> {
          println("Building Docker image for $srv using Gradle...")
          exec { commandLine("./gradlew", ":$srv:dockerBuild") }
        }
      }

      // Apply manifests
      val singleManifest = file("k8s/$srv.yml")
      val manifestDir = file("k8s/$srv")
      when {
        singleManifest.exists() -> {
          println("Applying Kubernetes manifest $singleManifest...")
          exec { commandLine("minikube", "kubectl", "--", "apply", "-f", singleManifest) }
        }
        manifestDir.exists() -> {
          println("Applying namespace first, then full manifest in k8s/$srv/...")
          exec { commandLine("minikube", "kubectl", "--", "apply", "-f", "k8s/$srv/namespace.yml") }
          exec { commandLine("minikube", "kubectl", "--", "apply", "-f", "k8s/$srv/", "-n", srv) }
        }
        else -> {
          println("Warning: No manifest found for $srv.")
          return@forEach
        }
      }

      // Rollout restart
      exec {
        commandLine("minikube", "kubectl", "--", "rollout", "restart", "deployment", "-n", srv)
        isIgnoreExitValue = true
      }
      exec {
        commandLine("minikube", "kubectl", "--", "rollout", "restart", "statefulset", "-n", srv)
        isIgnoreExitValue = true
      }
    }

    println("Deployment Completed!")
  }
}
