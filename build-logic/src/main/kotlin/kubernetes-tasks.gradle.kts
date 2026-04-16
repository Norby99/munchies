tasks.register<Exec>("deploy") {
  group = "kubernetes"
  description = "Deploys a service to Minikube. Usage: ./gradlew deploy -Pservice=<name|all>"

  val serviceName = project.findProperty("service") as? String ?: "all"

  doFirst {
    require(serviceName != "kafka") {
      "You cannot deploy only the kafka service."
    }
  }

  val servicesToDeploy = if (serviceName != "all") {
    "kafka $serviceName"
  } else {
    serviceName
  }

  commandLine("bash", "${rootProject.rootDir}/scripts/k8s-deploy.sh", servicesToDeploy)
}

tasks.register<Exec>("undeploy") {
  group = "kubernetes"
  description = "Undeploys a service from Minikube. " +
    "Usage: ./gradlew undeploy -Pservice=<name|all> [-PwipeData=true]"

  val serviceName = project.findProperty("service") as? String ?: "all"
  val wipeData = (project.findProperty("wipeData") as? String)?.toBoolean() ?: false

  doFirst {
    require(serviceName != "kafka") {
      "You cannot undeploy only the kafka service."
    }
  }

  val servicesToUndeploy = if (serviceName != "all") {
    "$serviceName kafka"
  } else {
    serviceName
  }

  val args =
    mutableListOf("bash", "${rootProject.rootDir}/scripts/k8s-undeploy.sh", servicesToUndeploy)
  if (wipeData) args.add("--wipe-data")

  commandLine(args)
}

tasks.register<Exec>("showDb") {
  group = "kubernetes"
  description = "Shows MongoDB data for a specific service. " +
    "Usage: ./gradlew showDb -Pservice=<name> [-Pcollection=<name>]"

  val serviceName = project.findProperty("service") as? String
  val collection = project.findProperty("collection") as? String

  doFirst {
    requireNotNull(serviceName) {
      "You must specify a service: ./gradlew showDb -Pservice=<name>"
    }
  }

  val args = mutableListOf(
    "bash",
    "${rootProject.rootDir}/scripts/k8s-show-db.sh",
    serviceName ?: "",
  )
  if (collection != null) args.add(collection)

  commandLine(args)
}

tasks.register<Exec>("showKf") {
  group = "kubernetes"
  description = "Tails a Kafka topic from Minikube. " +
    "Usage: ./gradlew showKf -Ptopic=<name>"

  val topic = project.findProperty("topic") as? String

  doFirst {
    requireNotNull(topic) {
      "You must specify a topic name: ./gradlew showKf -Ptopic=<name>"
    }
  }

  commandLine("bash", "${rootProject.rootDir}/scripts/k8s-show-kf.sh", topic ?: "")
}
