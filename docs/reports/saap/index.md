# Software Architecture And Platforms

This report describes the architecture of _Munchies_: how the domain was modelled, how it was split into
microservices, which architectural and microservices patterns were applied, and how the system is deployed and scaled
on Kubernetes. It also presents an agent-based component built on top of the platform.

The engineering process behind the project (build automation, version control, quality assurance and CI/CD) is
covered in the [SPE report](../spe/index.md) and is not repeated here.

## Members

- Alessandro Agosta ([Agostax0](https://github.com/Agostax0))
- Norbert Gabos ([Norby99](https://github.com/Norby99))
- Emiliano Rattini ([emirattini](https://github.com/emirattini))

## Scenario

_Munchies_ is an online food ordering platform where customers order their favourite food and managers create and
run their restaurants. Customers browse the closest restaurants, order from their menus, customize each dish and
choose when the food should be ready. An order can be delivered at home, picked up as takeaway, or eaten at the
restaurant, in which case the customer can also reserve a table.

Managers can create and manage multiple restaurants: their business hours, their menus and the variations available
for each dish.

## Technology stack

The technologies that shape the architecture are listed below. The complete list, including build and CI/CD tooling,
is in the [SPE report](../spe/index.md#technology-stack).

**Services**

- [Kotlin](https://kotlinlang.org/) and [Micronaut](https://micronaut.io/)
- [Node.js](https://nodejs.org/), [TypeScript](https://www.typescriptlang.org/) and [Express.js](https://expressjs.com/)
- [Vue.js](https://vuejs.org/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html), for the `-shared` contract modules

**Data and messaging**

- [MongoDB](https://www.mongodb.com/), one database per service
- [Apache Kafka](https://kafka.apache.org/), for notification events

**Platform**

- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)
- [Kubernetes](https://kubernetes.io/) ([Minikube](https://minikube.sigs.k8s.io/)) and [Helm](https://helm.sh/)

**Observability**

- [Prometheus](https://prometheus.io/), [Loki](https://grafana.com/oss/loki/), [Tempo](https://grafana.com/oss/tempo/),
  [Alloy](https://grafana.com/oss/alloy/) and [Grafana](https://grafana.com/oss/grafana/)

**API documentation**

- [OpenAPI](https://www.openapis.org/), published in the
  [OpenAPI specs](https://norby99.github.io/munchies/openapi/index.html)

## Structure of the report

1. **Domain Driven Design**: Requirements ([Business](01-Domain%20Driven%20Design/business-requirements.md), [Functional](01-Domain%20Driven%20Design/functional-requirements.md), [Non-functional](01-Domain%20Driven%20Design/non-functional-requirements.md), [Quality Attribute Scenarios](01-Domain%20Driven%20Design/quality-attribute-scenarios.md)), [User Stories](01-Domain%20Driven%20Design/User%20Stories.md), [Use Cases](01-Domain%20Driven%20Design/use-cases.md),
   [Glossary](01-Domain%20Driven%20Design/glossary.md),
   [Event Storming](01-Domain%20Driven%20Design/event-storming.md),
   [Bounded Contexts](01-Domain%20Driven%20Design/bounded-contexts.md),
   [Context Map](01-Domain%20Driven%20Design/context-map.md) and
   [Domain Model](01-Domain%20Driven%20Design/domain-model.md).
2. **Architecture and Implementation**:
   [General Architecture](02-Architecture%20and%20Implementation/general-architecture.md),
   [Hexagonal Architecture](02-Architecture%20and%20Implementation/hexagonal-architecture.md),
   [Microservices Patterns](02-Architecture%20and%20Implementation/microservices-patterns.md),
   [Interactions](02-Architecture%20and%20Implementation/interactions.md),
   [Architecture Decisions](02-Architecture%20and%20Implementation/architecture-decisions.md) and
   [Testing](02-Architecture%20and%20Implementation/testing.md).
3. **Deployment**: [Docker Compose](03-Deployment/docker-compose.md), [Kubernetes](03-Deployment/kubernetes.md) and the
   [Scaling Benchmark](03-Deployment/scaling-benchmark.md) that checks autoscaling against the service level
   objectives.
4. **Agent-Based Architecture**: the [Capacity Agent](04-Agent-Based%20Architecture/capacity-agent.md), an autonomous
   agent that decides how many orders a restaurant accepts per time slot.
5. **[Conclusions](05-conclusions.md)**: what the architecture delivered, what it cost and what remains open.
