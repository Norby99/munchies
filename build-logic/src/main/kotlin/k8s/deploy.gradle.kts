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
  excluded.set(
    (project.findProperty("exclude") as? String)
      ?.split(",")
      ?.map { it.trim() }
      ?.filter { it.isNotEmpty() }
      ?: emptyList(),
  )
  skipBuild.set((project.findProperty("skipBuild") as? String)?.toBoolean() ?: false)
}
