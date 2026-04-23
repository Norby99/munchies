package compose

tasks.register<Exec>("composeShowDb") {
  group = "compose"
  description = "Shows MongoDB data for a specific docker compose service. " +
    "Usage: ./gradlew composeShowDb -Pservice=<name> [-Pcollection=<name>]"

  val serviceName = project.findProperty("service") as? String
  val collection = project.findProperty("collection") as? String

  doFirst {
    requireNotNull(serviceName) {
      "You must specify a service: ./gradlew composeShowDb -Pservice=<name>"
    }
  }

  val args = mutableListOf(
    "bash",
    "${rootProject.rootDir}/scripts/compose/docker-show-db.sh",
    serviceName ?: "",
  )
  if (collection != null) args.add(collection)

  commandLine(args)
}
