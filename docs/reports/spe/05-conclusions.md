# Conclusions

## Problems encountered

### Multiplatform difficulties

Sharing one Kotlin source between the JVM and Node.js services was the most crucial part of the solution, and also where
most of the difficulties came from.
Every problem below was invisible in a single-service unit test and only appeared when a data was pushed between
services, during the later stages of development.

- **Micronaut cannot see the shared DTOs.** Request and Response types live in the `*-shared` modules, which compile to
  JavaScript too, so they can only carry the multiplatform `kotlinx.serialization` annotation and not Micronaut's
  `@Serdeable`; which always requires a `@SerdeImport` annotation for each used Request or Response, for each service;
  causing a lot of overhead for new features.
- **Default-valued fields don't appear in the JSON parsing.** `kotlinx.serialization`'s default `Json` instance has
  `encodeDefaults = false` so any value left at its default doesn't appear in the encoded output.
- **TypeScript declarations are maintained by hand.** KMP's JavaScript generated code is under its full package path (
  `com.munchies.order.infrastructure.adapter...`), so every shared module ships has a handwritten
  `<service>-modules.d.ts` that re-exports the names the Express services actually use for simplyfing importing on the
  TypeScript side.
- **Generated tarballs break `npm ci`.** A shared module is consumed as a local `.tgz` that is rebuilt on every build,
  so its hash changes every time and the `integrity` entry in `package-lock.json` isn't recognized. The workaround was
  removing that field by-hand and was only a documented
  rule ([Multiplatform](02-implementation/multiplatform.md#packaging)) rather than being something automated.

### Infrastructure

- **`/health` reports `UP` even with the database down.** Declaring a `readinessProbe` against `/health` seemed like a
  correct choice, but we later realized that a service's `UP` status didn't check its dependencies were also `UP`. The
  fix is a small shared `MongoHealthIndicator` ([Deployment](04-deployment.md)), verified live by stopping Mongo and
  watching the endpoint flip to `503`.
- **A single laptop is a small cluster.** Building the project, running tests or even deploying the Minikube clusters
  always exhausts the available memory of the development machine. During the later stages of development, tests could
  no longer be run locally and were rather run on the GitHub Runners, with developers avoiding the local Git hooks.

### Repository

- **GitHub's repository rulesets** gave us a lot of trouble and even now, we don't have an up-to-date changelog due to
  protection rules on the master branch.
- **GitHub's rebase** strips our commits from their verified tag, conflicting with branch rules that require a verified
  commit.
- **Renovate** would more often that not delay new features being pushed, since it would merge automatically and require
  developer branches to rerun checks with updated branches.
- **Maven** will enforce publishing limits to open source projects. We will be rate limited, and we will have to
  unfortunately drop any future Maven publish.

## Future work

- **Enforce the serialization rules instead of documenting them.** A Konsist or detekt rule that checks missing
  `@Serdeable` tags.
- **Checking that `*-modules.d.ts`** files match the compiled Kotlin exports, or generating them, would remove the
  manual efforts and resulting fatigue.
- **Coverage of the shared code.** Coverage is enforced at 70% (Kover for the Kotlin services, Vitest for the TypeScript
  ones), but the Kover report excludes the `commons` package; which contains the DDD base types and the HTTP client
  abstraction.
- **Finish the remaining services.** `table-reservation-service` is a stub and is excluded from both Docker Compose and
  the Kubernetes/Helm deployment, and `scheduler-service` and the `frontend-service` are still incomplete.
- **Devcontainers** may resolve some issues we've faced with different underlying Operative Systems on developer
  machines.

## Out of scope for this report

Horizontal scaling and autoscaling *are* implemented (`HorizontalPodAutoscaler` per service,
see [Deployment](04-deployment.md#horizontal-scaling)), but validating that behavior under load (throughput, latency,
the scaling timeline itself) will be written in the Software Architecture and Platforms's report; furthermore this also
extends Grafana and Prometheus, available but not reported.

As mentioned before, there is not a fully-complete graphical user interface (our would-be `frontend-service`), so our
project's can only be checked via end-to-end tests or raw http requests to endpoints.

## Final Remarks

This project confidently sits atop the rankings as our biggest project yet; as of currently writing this, it took 8
months of development, and there's still much to do.

But we can confidently say that, it has allowed us to explore and discover guidelines, best practises and technologies,
unused in other courses; giving us an interesting challenge, much to our delight.  