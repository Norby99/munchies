package k8s

import utils.k8s.DeployServicesTask
import utils.k8s.discoverServices

tasks.register<DeployServicesTask>("deployServices") {
  group = "munchies"
  val service = (project.findProperty("service") as? String) ?: "all"
  services.set(
    discoverServices(
      rootProject.rootDir.resolve("k8s"),
      rootProject.rootDir.resolve("helm/values"),
      service,
    ),
  )
  rootDir.set(rootProject.rootDir)
}
