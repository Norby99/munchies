package utils.k8s

import java.io.File

fun discoverServices(k8sDir: File, helmValuesDir: File, service: String): List<String> {
  if (service != "all") return listOf(service)

  val fromK8s = k8sDir.listFiles()
    ?.map { it.nameWithoutExtension }
    ?.filter { it != "kafka" }
    ?: emptyList()

  // Services deployed via the helm/munchies-service chart (see DeployServicesTask) — their
  // k8s/<service>/ manifests were removed once the chart was proven equivalent.
  val fromHelm = helmValuesDir.listFiles()
    ?.filter { it.extension == "yaml" || it.extension == "yml" }
    ?.map { it.nameWithoutExtension }
    ?: emptyList()

  return listOf("kafka") + (fromK8s + fromHelm).distinct()
}
