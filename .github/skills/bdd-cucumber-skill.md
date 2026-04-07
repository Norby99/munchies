# Skill: BDD + Cucumber for `service` Microservices

This skill defines how to write BDD tests in Munchies.

## Goal

- Step definition implementation in `service/src/test/kotlin`
- Gherkin feature files in `service/src/test/resources`
- Consistency with DDD (Domain, Application, Infrastructure)

## Standard Structure

```text
<microservice>/service/
  src/test/kotlin/.../bdd/
    <ServiceName>CucumberTest.kt
    <ServiceName>StepDefinitions.kt
  src/test/resources/features/
    <bounded-context>.feature
```

## Naming Rules

- Runner: `<ServiceName>CucumberTest`
- Step definitions: `<ServiceName>StepDefinitions`
- Feature: `<bounded-context>.feature`
- Scenarios written in business language

## Given/When/Then Pattern

- `Given`: set up the application context and test doubles
- `When`: invoke the use case or inbound adapter
- `Then`: verify observable behavior and output

## DDD Constraints

- Steps must orchestrate application use cases, not complex infrastructure logic.
- Assertions must validate domain outcomes and application contracts.
- The feature should describe bounded-context capabilities, not technical details.

## Minimal Template

### Runner (`service/src/test/kotlin/.../bdd/<ServiceName>CucumberTest.kt`)

```kotlin
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.munchies.<context>.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class <ServiceName>CucumberTest
```

### Feature (`service/src/test/resources/features/<bounded-context>.feature`)

```gherkin
Feature: User management
  Scenario: Create a user and retrieve it
    Given an empty user repository
    When a new user is created
    And the user is requested by id
    Then the user is found
```

### Step definitions (`service/src/test/kotlin/.../bdd/<ServiceName>StepDefinitions.kt`)

- fake/mock repository setup
- use case execution
- business-result assertions

## Run

```bash
./gradlew :<microservice>:service:test
```

