package k8s

val discoverServices = tasks.register("discoverServices") {
  val service = project.findProperty("service") as String? ?: "all"

  doLast {
    val services = mutableListOf<String>()

    if (service == "all") {
      services.add("kafka")
      val k8sDir = file("k8s")
      if (k8sDir.exists()) {
        k8sDir.listFiles()?.forEach { item ->
          val name = item.nameWithoutExtension
          if (name != "kafka") services.add(name)
        }
      }
    } else {
      services.add(service)
    }

    project.extra["discoveredServices"] = services
  }
}
