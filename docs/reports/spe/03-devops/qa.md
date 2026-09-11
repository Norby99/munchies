# Quality Assurance & Documentation
Quality assurance (QA) is the term used to describe the systematic efforts taken to assure that the product(s) delivered to customer(s) meet with the contractual and other agreed upon performance, design, reliability, and maintainability expectations of that customer.

The core purpose of quality assurance is to prevent mistakes and defects in the development and production.

Our project's quality assurance targets are measured through the following criterias:

- Code Testing and Code Linting
- Code Coverage
- Documentation
## Code Testing & Code Linting
These are the following code testing techniques and tools to ensure code quality used:

- [KtLint]() for Kotlin code linting and consistent code formatting across the project
- [Prettier]() for TypeScript and JavaScript code linting, but it was later disabled as it was no longer supported 
- [Detekt]() for static code analysis and bug prevention
- ```Architecture Tests``` using [Konsist]() which enforce clean architecture rules and domain driven design principles
- Unit tests using [Kotest]() with the [JUnit]() testing platform for JVM-based project, whilst [Vitest]() was used for TypeScript projects
- Component tests for Micronaut services, testing the MongoDB instances
- Integration tests for Micronaut services, testing the http controller responses
- Behavioral tests using the Cucumber's Gherkin language
- End-to-End tests to test the whole system behavior and correctness

Lastly, through a Gradle plugin, [```org.danilopianini.gradle-pre-commit-git-hooks```](https://github.com/DanySK/gradle-pre-commit-git-hooks), any development done locally by a developer requires a ```./gradlew check``` before it can be pushed to a remote-branch.

As shown by this configuration:
```
plugins { id("org.danilopianini.gradle-pre-commit-git-hooks") }
gitHooks {
  hook("pre-push") { tasks("check") }
  createHooks(overwriteExisting = true)
}
```

## Code Coverage
Code coverage is a metric that measures the percentage of code that is executed during automated tests.

Our project enforces a 70% minimum for all subprojects though a [Kover config](https://github.com/Norby99/munchies/blob/master/build-logic/src/main/kotlin/test-suites.gradle.kts) for Kotlin subproject and a [vitest.config.ts](https://github.com/Norby99/munchies/blob/master/gateway-service/vitest.config.ts)   

## Documentation
Most of our documentation is generated via plugins and tools, such as:

- Dokka for Kotlin code documentation which natively supports Gradle subprojects
- TypeDocs for TypeScript documentation 
- ```io.swagger.core.v3:swagger-annotations``` was used in Kotlin projects to automatically generate OpenAPI schemas
- ```tsoa``` was used in TypeScript projects to automatically generate OpenAPI schemas, but it doesn't work well with our Multiplatform objects
- ```Redocly``` was used to generate web pages from the OpenAPI schemas
- ```mkdocs``` was used to turn ```.md``` files into web pages, such as this report

Lastly, all of our documentation is currently hosted online as a static web page on GitHub pages. 