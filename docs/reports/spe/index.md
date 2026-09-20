# Introduction

**Munchies** is a food-ordering and table-reservation platform, built as the joint project for four courses — Software Process Engineering, Software Architecture and Platforms, Distributed Systems, and Web Services and Applications — with this report covering the Software Process Engineering angle specifically: Domain-Driven Design, development process and DevOps automation.

## The domain

Two roles, hierarchical:

- **Customers** browse restaurants, order food for delivery/takeaway/dine-in, and book tables.
- **Managers** additionally administer the one restaurant they own — its details, opening hours, menus and tables. A manager satisfies everything a customer-gated action requires too (see [Glossary](01-deliverables/glossary.md)).

## Why microservices, and why this report structure

The system is deliberately engineered as an independent microservice per bounded context — one team-sized area of the domain, one dedicated database, communicating with the rest of the system only through its HTTP API or asynchronous events (see [Domain Model](01-deliverables/domain-model.md)). That split is also what makes "2+ target platforms" a real architectural property rather than a checkbox: three services run on the JVM (Micronaut/Kotlin), four on Node.js (Express/TypeScript), sharing a compiled Kotlin Multiplatform domain layer across the runtime boundary (see [Multiplatform](02-implementation/multiplatform.md)).

This report follows the shape of the system itself:

1. **[Deliverables](01-deliverables/glossary.md)** — the ubiquitous language and the domain model it's built from.
2. **[Implementation](02-implementation/microservices.md)** — how the services are structured, tested, and how two runtimes share one domain layer.
3. **[DevOps](03-devops/project-structure.md)** — the build system, version control practices, quality gates, and CI/CD pipeline.
4. **[Deployment](04-deployment.md)** — containerization and Kubernetes/Helm orchestration, including a real gap found and fixed along the way (MongoDB readiness checking) and a Helm migration verified equivalent before anything was deleted.
5. **[Conclusions](05-conclusions.md)** — what held up, what didn't, and what's explicitly left for the companion Software Architecture and Platforms report instead.

Repository: [github.com/Norby99/munchies](https://github.com/Norby99/munchies).
