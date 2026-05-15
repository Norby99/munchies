package utils.k8s

import java.io.File

fun discoverServices(k8sDir: File, service: String): List<String> {
  if (service != "all") return listOf(service)

  val others = k8sDir.listFiles()
    ?.map { it.nameWithoutExtension }
    ?.filter { it != "kafka" }
    ?: emptyList()

  return listOf("kafka") + others
}
