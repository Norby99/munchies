package k8s

plugins {
  id("k8s.deploy")
  id("k8s.undeploy")
  id("k8s.observability")
}

// The observability stack is always-on, so it follows a full deploy/undeploy. It is left alone
// when a single service is targeted with -Pservice=<name>.
val targetsAllServices = !project.hasProperty("service")

tasks.register("deploy") {
  group = "munchies"
  description = "Deploys all services to Minikube. Usage: ./gradlew deploy"
  dependsOn("deployServices")
  if (targetsAllServices) dependsOn("deployObservability")
}

tasks.register("undeploy") {
  group = "munchies"
  description = "Undeploys all services from Minikube. Usage: ./gradlew undeploy [-PwipeData=true]"
  dependsOn("undeployServices")
  if (targetsAllServices) dependsOn("undeployObservability")
}
