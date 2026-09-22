# Introduction

**Munchies** is a food-ordering and table-reservation platform, built as the joint project for four courses:
Software Process Engineering, Software Architecture and Platforms, Distributed Systems, and Web Services and
Applications. This report covers the Software Process Engineering angle specifically: Domain-Driven Design,
development process and DevOps automation.

## The team

- [Norbert Tiberiu Gabos](tiberiunorbert.gabos@studio.unibo.it)
- [Alessandro Agosta](alessandro.agosta@studio.unibo.it)
- [Emiliano Rattini](emiliano.rattini@studio.unibo.it)

## The domain

Two roles, hierarchical:

- **Customers** browse restaurants, order food for delivery/takeaway/dine-in, and book tables.
- **Managers** creates and administers one or more restaurants: its details, opening hours, menus and tables.
A manager satisfies everything a customer-gated action requires too (see [Glossary](01-deliverables/glossary.md)).

## Why microservices, and why this report structure

The system is deliberately engineered as an independent microservice per bounded context: one team-sized area of
the domain, one dedicated database, communicating with the rest of the system through its HTTP API and, in a few cases,
asynchronous events over Kafka (see [Domain Model](01-deliverables/domain-model.md)). 
That split is also what makes "2+ target platforms" a real architectural property: three services run on the JVM 
(Micronaut/Kotlin), four on Node.js (Express/TypeScript), sharing one Kotlin Multiplatform source for their REST 
contracts and DDD base types across the runtime boundary, while each service's own domain logic is implemented once 
per runtime (see [Multiplatform](02-implementation/multiplatform.md)).

This report follows the shape of the system itself:

1. **[Deliverables](01-deliverables/glossary.md)**: the ubiquitous language and the domain model it's built from.
2. **[Implementation](02-implementation/microservices.md)**: how the services are structured, how they communicate 
between each other and how they're tested.
3. **[DevOps](03-devops/project-structure.md)**: the build system, version control practices, quality gates, and CI/CD pipeline.
4. **[Deployment](04-deployment.md)**: containerization and orchestration.
5. **[Conclusions](05-conclusions.md)**: the problems met along the way and the future work.

Repository: [github.com/Norby99/munchies](https://github.com/Norby99/munchies).
