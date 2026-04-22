package k8s

tasks.register<Exec>("k8sInfo") {
  group = "kubernetes"
  description = "Prints the current pods and deployments across all namespaces in Minikube."

  commandLine(
    "bash",
    "-c",
    """
        echo "==========================================="
        echo "                   PODS                    "
        echo "==========================================="
        minikube kubectl -- get pods -A
        echo ""
        echo "==========================================="
        echo "               DEPLOYMENTS                 "
        echo "==========================================="
        minikube kubectl -- get deployments -A
    """.trimIndent(),
  )
}
