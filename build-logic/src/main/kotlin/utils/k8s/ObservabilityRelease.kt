package utils.k8s

/**
 * A Helm release of the observability stack (see `observability/README.md`).
 *
 * @property name Helm release name. The Grafana datasource URL in `observability/values/grafana.yaml`
 * depends on the Prometheus one, so renaming a release requires updating it.
 * @property repoName local alias of the Helm repository hosting the chart.
 * @property repoUrl URL of the Helm repository hosting the chart.
 * @property chart chart name inside the repository.
 * @property valuesFile values file, relative to the `observability/values` directory.
 */
data class ObservabilityRelease(
  val name: String,
  val repoName: String,
  val repoUrl: String,
  val chart: String,
  val valuesFile: String,
)

const val OBSERVABILITY_NAMESPACE = "observability"

/** Releases making up the stack, in installation order. */
val observabilityReleases = listOf(
  ObservabilityRelease(
    name = "prometheus",
    repoName = "prometheus-community",
    repoUrl = "https://prometheus-community.github.io/helm-charts",
    chart = "prometheus",
    valuesFile = "prometheus.yaml",
  ),
  ObservabilityRelease(
    name = "grafana",
    repoName = "grafana-community",
    repoUrl = "https://grafana-community.github.io/helm-charts",
    chart = "grafana",
    valuesFile = "grafana.yaml",
  ),
)
