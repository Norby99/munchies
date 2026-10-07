package k8s

import utils.k8s.DeployObservabilityTask
import utils.k8s.UndeployObservabilityTask

tasks.register<DeployObservabilityTask>("deployObservability") {
  group = "munchies"
  description = "Installs the observability stack (Prometheus + Grafana) into Minikube."
  valuesDir.set(rootProject.layout.projectDirectory.dir("observability/values"))
}

tasks.register<UndeployObservabilityTask>("undeployObservability") {
  group = "munchies"
  description = "Uninstalls the observability stack. Usage: [-PwipeData=true]"
  wipeData.set((project.findProperty("wipeData") as? String)?.toBoolean() ?: false)
}
