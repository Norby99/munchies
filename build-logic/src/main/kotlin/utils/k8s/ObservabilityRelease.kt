package utils.k8s

/**
 * A Helm release of the observability stack (see `observability/README.md`).
 *
 * @property name Helm release name. The Prometheus, Loki and Tempo ones are part of the Service
 * URLs used in `observability/values/grafana.yaml` and `alloy.yaml`, and the Tempo one of the
 * `OTEL_EXPORTER_OTLP_ENDPOINT` of every service, so renaming a release requires updating them.
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
    name = "loki",
    repoName = "grafana-community",
    repoUrl = "https://grafana-community.github.io/helm-charts",
    chart = "loki",
    valuesFile = "loki.yaml",
  ),
  ObservabilityRelease(
    name = "alloy",
    repoName = "grafana",
    repoUrl = "https://grafana.github.io/helm-charts",
    chart = "alloy",
    valuesFile = "alloy.yaml",
  ),
  ObservabilityRelease(
    name = "tempo",
    repoName = "grafana-community",
    repoUrl = "https://grafana-community.github.io/helm-charts",
    chart = "tempo",
    valuesFile = "tempo.yaml",
  ),
  ObservabilityRelease(
    name = "grafana",
    repoName = "grafana-community",
    repoUrl = "https://grafana-community.github.io/helm-charts",
    chart = "grafana",
    valuesFile = "grafana.yaml",
  ),
)
