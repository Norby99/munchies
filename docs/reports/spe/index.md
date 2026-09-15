# Introduction

## Members

- Alessandro Agosta ([Agostax0](https://github.com/Agostax0))
- Norbert Gabos ([Norby99](https://github.com/Norby99))
- Emiliano Rattini ([emirattini](https://github.com/emirattini))

## Scenario

_Munchies_ is a restaurant management platform covering the whole lifecycle of a restaurant order. Restaurant
managers can register their restaurant and build its menu, organizing dishes into categories and offering
customizable variations. Customers can browse restaurants and their menus, and place a delivery, takeaway or
dine-in order. Once an order is placed, its status is tracked through preparation and, for deliveries, scheduling
and dispatch to the customer's address. Payment is processed against the order, and both the restaurant and the
customer are kept informed through notifications as the order and payment progress. Customers can also reserve a
table at a restaurant ahead of time.

Behind this single user-facing flow, the system is split into independently deployable microservices, each owning
its own part of the domain and its own database, communicating over REST and, where an asynchronous, fire-and-forget
notification is enough, over Kafka. This report documents how that architecture is designed, implemented, tested
and deployed.

## Technology Stack

**Infrastructure**

- [Docker](https://www.docker.com/)
- [Kubernetes](https://kubernetes.io/)
- [Kafka](https://kafka.apache.org/)
- [MongoDB](https://www.mongodb.com/)

**Kotlin Microservices**

- [Kotlin](https://kotlinlang.org/)
- [Micronaut](https://micronaut.io/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Gradle](https://gradle.org/)
- [Konsist](https://docs.konsist.lemonappdev.com/) (architectural tests)
- [Kotest](https://kotest.io/)
- [Dokka](https://kotlinlang.org/docs/dokka-introduction.html)

**Integrated (Express.js) Microservices**

- [Node.js](https://nodejs.org/)
- [TypeScript](https://www.typescriptlang.org/)
- [Express.js](https://expressjs.com/)
- [Mongoose](https://mongoosejs.com/)

**Frontend**

- [Vue.js](https://vuejs.org/)
- [Pinia](https://pinia.vuejs.org/)
- [Vue Router](https://router.vuejs.org/)
- [Axios](https://axios-http.com/)

**API Documentation**

- [OpenAPI](https://www.openapis.org/)
- [Swagger](https://swagger.io/)
- [tsoa](https://tsoa-community.github.io/docs/)
- [Redocly](https://redocly.com/)

**Code Quality**

- [Detekt](https://detekt.dev/)
- [Spotless](https://github.com/diffplug/spotless)

**Versioning**

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Commitlint](https://commitlint.js.org/)
- [Semantic Release](https://semantic-release.gitbook.io/semantic-release/)
- [Renovate](https://docs.renovatebot.com/)

**CI/CD**

- [GitHub Actions](https://github.com/features/actions)
- [Docker Hub](https://hub.docker.com/)
- [Gradle](https://gradle.org/)
