# Testing

We followed the testing pyramid strategy, with four levels of tests, each covering a different scope and trading off
speed for realism as we move up:

- **Unit tests** sit at the base. They exercise a single class in isolation, typically a use case or a domain
  object, with every outbound port (repositories, external service clients) replaced by a mock. They are the
  fastest to run and make up the bulk of our test suite.
- **Integration tests** check that our own code integrates correctly with an external dependency we do not
  control, such as MongoDB or Kafka. Rather than mocking the dependency, we spin up a real instance of it in a
  container via [Testcontainers](https://testcontainers.com/), so the adapter under test talks to the real thing.
- **Component tests** verify a service's acceptance criteria in isolation: the service under test runs for real,
  end-to-end from its HTTP layer down to its own database, and only its calls to *other* microservices are mocked.
  Everything else is real.
- **End-to-end tests** verify the behavior of the whole system, with every microservice actually running and
  talking to one another.

## Test Source Sets

Each service module keeps unit, integration and component tests in three separate source sets, one per level,
mirroring the distinction above. ```order-service``` is a good example, as it has all three filled in:

```
order-service/src/
├── test/            # unit tests
├── integrationTest/ # integration tests
└── componentTest/   # component tests
```

For instance, ```AdvanceOrderStatusUseCaseUnitTest``` lives under ```test/``` and checks the use case's logic against
a mocked ```OrderRepository```; ```MongoOrderRepositoryIntegrationTest``` lives under ```integrationTest/``` and runs
the same repository against a real, disposable MongoDB container; ```PlaceOrderControllerComponentTest``` lives under
```componentTest/``` and drives the service through its actual HTTP controller, with a real database underneath.
Keeping them in separate source sets lets us run only the fast unit tests while developing, and run the slower,
container-backed suites separately, in Gradle or in CI, without having to filter tests by name or tag.

## End-to-end Tests

Unlike the first three levels, end-to-end tests are not about any single service: they exercise the system as a
whole, across service boundaries, so they do not belong inside any one service module. We gave them their own
dedicated module, [```e2e-test```](https://github.com/Norby99/munchies/tree/master/e2e-test), independent of every
microservice.

We implemented them using BDD with [Cucumber](https://cucumber.io/): each scenario is written in Gherkin, as a
```.feature``` file under ```src/e2e/resources/features```, and backed by Kotlin step definitions under
```src/e2e/kotlin```, which call the real services over HTTP.

```gherkin
Feature: Create an order

  Scenario: A authenticated client creates an delivery order
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    Then order is created successfully
```

Writing scenarios this way keeps them readable independently of the implementation, and close to the acceptance
criteria we started from. Running them, however, requires every microservice to actually be up: as described in
[Deployment](../04-deployment.md), the ```e2eTest``` Gradle task takes care of that itself, bringing up the whole
Docker Compose stack before the scenarios run and tearing it back down afterward.
