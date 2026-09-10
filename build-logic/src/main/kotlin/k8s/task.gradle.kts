package k8s

plugins {
  id("k8s.deploy")
  id("k8s.undeploy")
}

tasks.register("deploy") {
  group = "munchies"
  description = "Deploys all services to Minikube. Usage: ./gradlew deploy"
  dependsOn("deployServices")
}

tasks.register("undeploy") {
  group = "munchies"
  description = "Undeploys all services from Minikube. Usage: ./gradlew undeploy [-PwipeData=true]"
  dependsOn("undeployServices")
}
