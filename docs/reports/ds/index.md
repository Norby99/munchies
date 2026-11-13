# Munchies

- [Alessandro Agosta](mailto:alessandro.agosta@studio.unibo.it)
- [Norbert Gabos](mailto:tiberiunorbert.gabos@studio.unibo.it)
- [Emiliano Rattini](mailto:emiliano.rattini@studio.unibo.it)

### AI Disclaimer

```
"During the preparation of this work, the author(s) used:
- Gemini to edit simple documents, generate code documentation and write basic unit tests. 
After using this tool/service, the author(s) reviewed and edited the
content as needed and take(s) full responsibility for the content of the
final report/artifact."
```

## Abstract

_Munchies_ is an online food ordering platform that will allow both customers to order their
favourite food and managers to create and manage their restaurants. The platform allows customers to
browse the closest restaurants and to order from their menus. They can choose when the food should
be delivered, and they can also customize each dish. Customers can also choose if they want their order
to be delivered to their home, if they want a takeaway, or consume their meal at the restaurant allowing them
to reserve a table.

Meanwhile, managers can create and manage multiple restaurants. Specifically, they can manage
the business hours, menus and the variations for each dish.

### Goals
_Munchies_ is meant to condense multiple exams into one project. The technologies, programming paradigms and design choises used, respect and fulfill the requirements given by each course.

In this report we wrote the design, implementation and operation choices, we made during _munchies_' development to fulfill the requirements set by the Distributed Systems' course.


### Achievements

As of currently writing this, _munchies_ is a distributed, multiplatform, containerized, microservices-structured application; which uses the following technologies:

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
- [Maven](https://central.sonatype.com/)
- [NPM](https://www.npmjs.com/)
- [Gradle](https://gradle.org/)


It is still in development as not all other courses requirements are fulfilled yet.  