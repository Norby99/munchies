package k8s

import utils.k8s.DeployServicesTask
import utils.k8s.discoverServices

tasks.register<DeployServicesTask>("deployServices") {
  val service = (project.findProperty("service") as? String) ?: "all"
  services.set(discoverServices(rootProject.rootDir.resolve("k8s"), service))
  rootDir.set(rootProject.rootDir)
}
