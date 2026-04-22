package k8s

tasks.register<Exec>("deploy") {
  group = "kubernetes"
  description = "Deploys all services to Minikube. Usage: ./gradlew deploy"

  commandLine("bash", "${rootProject.rootDir}/scripts/k8s/k8s-deploy.sh", "all")
}

tasks.register<Exec>("undeploy") {
  group = "kubernetes"
  description = "Undeploys all services from Minikube. " +
    "Usage: ./gradlew undeploy [-PwipeData=true]"

  val wipeData = (project.findProperty("wipeData") as? String)?.toBoolean() ?: false

  val args =
    mutableListOf("bash", "${rootProject.rootDir}/scripts/k8s/k8s-undeploy.sh", "all")
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
    "${rootProject.rootDir}/scripts/k8s/k8s-show-db.sh",
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

  commandLine("bash", "${rootProject.rootDir}/scripts/k8s/k8s-show-kf.sh", topic ?: "")
}
