package k8s

import utils.k8s.UndeployServicesTask
import utils.k8s.discoverServices

tasks.register<UndeployServicesTask>("undeployServices") {
  val service = (project.findProperty("service") as? String) ?: "all"
  services.set(discoverServices(rootProject.rootDir.resolve("k8s"), service))
  rootDir.set(rootProject.rootDir)
  wipeData.set((project.findProperty("wipeData") as? String)?.toBoolean() ?: false)
}
