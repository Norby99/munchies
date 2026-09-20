# Conclusions

## Against the brief

| Requirement | Status | Where |
| --- | --- | --- |
| Domain-Driven Design | Met | [Domain Model](01-deliverables/domain-model.md), [Glossary](01-deliverables/glossary.md) |
| Clear development process | Met | GitFlow, ruleset-enforced branch protection, Conventional Commits ([VCS & Release](03-devops/vcs.md)) |
| Full-scale automation, incl. CI/CD | Met | [CI/CD Workflows](03-devops/cicd.md) — build, test, quality gates and release are all pipeline-driven, not manual steps |
| Deploy automation via containerization/orchestration | Met | [Deployment](04-deployment.md) — Docker, Kubernetes, and a Helm chart verified equivalent to the manifests it replaced before anything was deleted |
| 2+ target platforms | Met | JVM (Micronaut/Kotlin) + Node.js (Express/TypeScript), sharing a compiled domain layer — [Multiplatform](02-implementation/multiplatform.md) |

## What this project actually demonstrated, beyond the checklist

The most useful parts of this work weren't the features that worked on the first try — they were the places where a claim got checked instead of assumed, and turned out to be wrong:

- **The Mongo readiness gap.** It would have been easy to declare a `readinessProbe` against `/health` and call the deployment story done. Actually checking what `/health` covered surfaced that Kafka connectivity was genuinely verified but MongoDB wasn't — Micronaut simply doesn't ship a health indicator for the driver/module combination these services use. The fix ([Deployment](04-deployment.md)) was verified the same way the gap was found: live, by stopping Mongo and watching the endpoint actually flip to `503`.
- **The Helm migration.** Rather than trusting that a hand-written chart was equivalent to the manifests it was meant to replace, every values file was rendered and diffed, structurally, against the original — and that diff caught a real naming bug (Mongo resource names that wouldn't have matched the DNS names already hardcoded in `MONGODB_URI`) before it ever reached a cluster. The manifests were only deleted after the render came back byte-for-byte identical.
- **Two Micronaut modules that don't compose.** Investigating *why* the reactive Mongo health indicator still didn't work after adding its driver surfaced a real integration gap between `micronaut-data-mongodb` and `micronaut-mongo-reactive` — not a misconfiguration on this project's part, confirmed by testing the "obvious" fix live and watching it fail in a specific, diagnosable way (`UNKNOWN` instead of `UP`/`DOWN`).

## What's honestly still open

- **Coverage isn't enforced**, only measured — the Kover `minBound` is a commented-out TODO ([Quality Assurance](03-devops/qa.md)).
- **No generated changelog** — `@semantic-release/changelog` is configured but commented out; release notes exist only as GitHub Releases, not a `CHANGELOG.md`.
- **`table-reservation-service` is incomplete** and excluded from both Docker Compose and the Kubernetes/Helm deployment.
- **The deploy pipeline and the release pipeline aren't connected** — Kubernetes/Helm manifests reference locally-built `:latest` images, not the versioned images CI already publishes to Docker Hub ([Deployment](04-deployment.md)).

## What's deliberately out of scope here

Horizontal scaling and autoscaling *are* implemented (`HorizontalPodAutoscaler` per service — [Deployment](04-deployment.md#horizontal-scaling)) but **empirically validating** that behavior under load — throughput, latency, the scaling timeline itself — was moved to the companion Software Architecture and Platforms report, since that's a performance/architecture question more than a process-engineering one. The two reports share the same underlying system rather than duplicating the same load test for two different audiences.
