# Conclusions

_Munchies_ set out to cover the full lifecycle of a restaurant order — browsing a menu, placing a delivery,
takeaway or dine-in order, paying for it, tracking it through preparation and dispatch, and keeping both the
customer and the restaurant informed — as a set of independently deployable microservices rather than a single
monolith. Looking back at the system as it stands, that goal was met: nine packages, split across two backend
stacks and a Vue.js frontend, each own their own data and communicate over REST or, where a fire-and-forget
notification is enough, over Kafka.

## What the architecture bought us

Committing early to the same Hexagonal/DDD layout across every service, regardless of stack, was the single
decision that paid off the most. A developer who understands ```order-service```'s ```domain```/```application```/
```infrastructure``` split already understands ```payment-service```'s, even though one is Kotlin and the other is
TypeScript — only the syntax changes, not the shape. That consistency was not left to convention alone: the
```architecture-rules``` project's [Konsist](https://kotest.io/) tests, run on every push, catch a dependency
pointing the wrong way before it reaches review.

[Kotlin Multiplatform](02-implementation/multiplatform.md) turned out to be the right tool for the specific problem
we had — Kotlin and TypeScript services needing to agree on request/response shapes and endpoint paths without
retyping them twice — but it was not free. Packaging a JVM library as a consumable npm module, and keeping
```package-lock.json``` happy with a tarball that is rebuilt, and therefore rehashed, on every build, took real
trial and error to get right, and is the kind of build-system detail this report would not have surfaced without
having lived through it.

The [testing pyramid](02-implementation/testing.md) and the [CI/CD pipeline](03-devops/cicd.md) built on top of it
gave us the confidence to keep changing a nine-service system without one service's change silently breaking
another: unit and integration tests catch regressions close to the code, component tests verify each service's
acceptance criteria in isolation, and end-to-end Cucumber scenarios, run against the real Docker Compose stack,
verify the scenario described in this report's [introduction](index.md) actually holds end to end. Combined with
enforced [Conventional Commits](03-devops/vcs.md), semantic release and automatically published Docker, npm and
Maven artifacts, the project reaches a state that could be handed off or built upon without archaeology.
 
## Limitations and future work

Not everything reached the same maturity. ```table-reservation-service``` is still a stub, with only its
controller in place and no domain logic behind it — the natural next service to bring up to the same standard as
```payment-service```. Kubernetes deployment, meant to complement Docker Compose as the production-like target
described in [Deployment](04-deployment.md), is likewise not yet finished. Both are scoped, structured places to
resume work rather than open-ended gaps: the architecture and conventions that would host them are already in
place.

Not every rule we set up survived contact with reality unchanged, and that too was a useful outcome. We initially
required, as a [branch protection rule](03-devops/vcs.md), that every commit reaching "Develop" or "Master" be
verified. GitHub's rebase-and-merge, however, does not preserve a commit's verified signature, so the rule ended up
blocking the very merges it was meant to allow. We removed it rather than keep a check that rejected legitimate
work — a reminder that a process rule is only as good as its fit with the exact tooling enforcing it.

## Closing remarks

More than any single technology choice, what made a nine-service, two-language system tractable for a three-person
team was consistency: one architectural shape, one set of build conventions, one commit and release discipline,
enforced automatically rather than left to memory. That is the main takeaway we would carry into a future project
of this kind.
