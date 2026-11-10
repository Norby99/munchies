# Testing

Testing follows the test pyramid: many fast unit tests at the base, fewer and slower tests going up to the end-to-end
level. Each level has a concrete role in a microservices system: unit tests check the domain and the use cases in
isolation, integration tests check the adapters against the real technology, component tests check a single service as
a black box, and end-to-end tests check the services working together.

The examples below come mainly from `user-service`, the same reference service used in
[Hexagonal Architecture](hexagonal-architecture.md); `payment-service` shows the same levels on the Express.js stack.

| Level            | What is tested                        | Tools                                 | Location                                |
|------------------|---------------------------------------|---------------------------------------|-----------------------------------------|
| Unit (sociable)  | Aggregates, value objects, factories  | JUnit 5, Kotest matchers              | `<service>/src/test`                    |
| Unit (solitary)  | Use cases, controllers, adapters      | Mockito / MockK, Vitest (`vi.fn()`)   | `<service>/src/test`                    |
| Integration      | Mongo adapters against a real MongoDB | Testcontainers                        | `<service>/src/integrationTest`         |
| Component        | One service through its HTTP API      | `@MicronautTest`, Testcontainers      | `<service>/src/componentTest`           |
| End-to-end       | User journeys across all services     | Cucumber, Docker Compose              | `e2e-test/src/e2e`                      |
| Acceptance (BDD) | Use cases against the user stories    | Cucumber, Gherkin, in-memory adapters | `<service>/src/test/resources/features` |
| Architecture     | Dependency rule between layers        | Konsist                               | `architecture-rules`                    |
| Load             | Horizontal scaling under load         | k6 on Kubernetes                      | `loadtest/`                             |

## Unit tests

Unit tests live in `src/test`. The hexagonal structure determines which style is used for each class.

**Sociable unit tests for the domain.** Aggregates, value objects and factories are plain objects with no
infrastructure dependency, so they are tested with their real collaborators and no mocks. For example,
`UserFactoryTest` checks the invariants enforced when a `User` is created:

```kotlin
@Test
fun `create should not create a user with empty username`() {
    val res = User.factory.create("specific-id", exampleUser.profile.copy(username = ""))

    val msg = res.shouldBeFailure()
    msg.shouldNotBeEmpty()
}
```

**Solitary unit tests for the use cases.** A use case only depends on outbound ports, so every port is replaced by a
mock. The test checks both the returned result and the interactions with the ports. In `RegisterUserUseCaseTest`, the
repositories, the hasher and the mailer are all mocks:

```kotlin
val userRepository = mock<UserRepository> { on { findById(any()) } doReturn null }
val credentialsRepository = mock<UserCredentialsRepository>()
val hasher = mock<PasswordHasher> { on { hash(any(), any()) } doReturn "" }
val mailer = mock<Mailer> { on { sendMail(any(), any()) } doReturn MailSent }
val useCase = RegisterUserUseCase(userRepository, credentialsRepository, hasher, mailer)

val result = useCase.execute(user, credentials)

assertTrue(result is RegisterUserResult.Success)
verify(userRepository).save(user)
verify(mailer).sendMail(any(), any())
```

Controllers (`MicronautUserControllerTest`) and the outbound adapters that do not need a real external system
(`KotlinPasswordHasherTest`, `FakeMailerTest`, the in-memory repositories) are covered in the same way.

On the Express.js stack, `payment-service` uses Vitest with the same approach: in `ProcessPaymentUseCase.test.ts` the
ports towards `order-service` and Kafka are replaced by `vi.fn()` stubs, so the use case is tested without any network
call:

```typescript
orderServiceClient = {markOrderAsPaid: vi.fn().mockResolvedValue({success: true})};
```

The step definitions (`LoginUserSteps`, ...) receive the inbound ports through Micronaut injection: a custom Cucumber
object factory (`MicronautUserObjectFactory`) builds them from the application context. `TestServicesFactory` binds the
repositories to their in-memory adapters, so the scenarios exercise the real use cases and domain without any
database. This is possible because of the hexagonal structure: the use cases are the same, only the adapters behind
the ports change.

## Integration tests

Integration tests live in the `integrationTest` source set and check the outbound adapters against the real technology.
The MongoDB adapters are tested against a MongoDB started by Testcontainers, with the same `mongo:7.0` image used in
deployment:

```kotlin
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoUserRepositoryIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        private val mongo = MongoDBContainer("mongo:7.0")
    }

    @BeforeAll
    fun setup() {
        context = ApplicationContext.run(
            mapOf("mongodb.uri" to "${mongo.connectionString}/user-service", /* ... */),
            "prod",
        )
        repository = context.getBean(MongoUserRepository::class.java)
    }

    @AfterEach
    fun cleanup() {
        context.getBean(MongoCrudUserRepository::class.java).deleteAll()
    }

    @Test
    fun `find by id should return null on user not found `() {
        repository.findById(UserId()).shouldBeNull()
    }
}
```

Only the adapter under test and its Micronaut wiring are started, and the collection is emptied after each test, so
tests are independent of each other. They verify the mapping between domain objects and documents and the custom
queries (`findByEmail`, `findByUsername`, login attempt counters).

### Contracts between services

No consumer-driven contract tests are used. The contract between services is instead shared as code: every service
publishes its API (paths, methods, required role, request and response classes) and its Kafka messages (topic names
and payloads) in its `-shared` Kotlin Multiplatform module, compiled both for the JVM and to JavaScript. Consumers
build on those definitions:

- `gateway-service` routes extend the API classes of the services they proxy (e.g. `PlaceOrderRoute` extends
  `PlaceOrderAPI`);
- `payment-service` calls `order-service` with the path taken from `order-shared`;
- `notification-service` parses Kafka messages with the deserializers of the producers' `-shared` modules.

A breaking change in a provider's contract therefore shows up as a compilation or build failure of its consumers,
before any test runs. What this does not cover is a provider whose implementation diverges from its own published
contract, which is left to the component and end-to-end tests.

## Component tests

Component tests live in the `componentTest` source set and treat one microservice as a black box, testing it through
its HTTP API against the acceptance criteria. The service runs for real (`@MicronautTest` starts the embedded server on
a random port) with its real MongoDB adapters, backed again by Testcontainers. The interactions with the other
microservices are the only part that is replaced.

The common setup is in `BaseUserController`, which starts one MongoDB container for all the component tests and
provides an HTTP client towards the embedded server. Each test class covers one endpoint, e.g.
`RegisterUserControllerComponentTest`:

```kotlin
@MicronautTest(environments = ["prod"], transactional = false)
class RegisterUserControllerComponentTest : BaseUserController() {

    @Test
    fun `invalid register user request should return bad request`() {
        val request = RegisterUserRequest(
            username = "", email = "", role = "CUSTOMER", hashedPassword = "", saltValue = "",
        )

        val response = assertThrows(HttpClientResponseException::class.java) {
            httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.REGISTER_USER_PATH)
        }
        response.status shouldBe HttpStatus.BAD_REQUEST
    }
}
```

These tests cover the whole inbound path: JSON deserialization, validation, mapping to domain objects, use case,
persistence and translation of the result into the HTTP status.

## End-to-end tests

The `e2e-test` module tests the whole system as a black box, through `gateway-service` exactly as the frontend does.
The scenarios are user journeys written in Gherkin and executed with Cucumber:

```gherkin
Feature: Register user

  Scenario: A user registers a new account
    Given a user which doesnt exist yet
    When the user registers its info
    Then the user receives a personal auth token
    And the user can query its info
```

The suite covers registering a user, updating the user profile, placing an order and advancing its status, so it
crosses `gateway-service`, `user-service` and `order-service` with real authentication. The `e2eTest` task depends on
`composeUp` and is finalized by `composeDown`, so the whole stack described in
[Docker Compose](../03-Deployment/docker-compose.md) is started before the scenarios and torn down after them. Service
URLs can be overridden through system properties to run the same suite against another deployment.

## Other tests

### Acceptance tests (BDD) on the use cases

The user stories of `user-service` are written in Gherkin and executed with Cucumber, one feature file per use case
(`register-user.feature`, `login-user.feature`, ...). Scenario outlines cover the different outcomes of the same use
case:

```gherkin
Feature: Login User

  Scenario Outline: Login a user with login statuses
    Given a login <system_status> system
    When I attempt to login with <credentials>.
    Then the login system should respond with <expected_response>.

    Examples:
      | system_status | credentials | expected_response |
      | registered    | correct     | login             |
      | registered    | wrong       | failure           |
      | not found     | correct     | not found         |
      | blocked       | correct     | blocked           |
```

### Architecture tests

The dependency rule of the hexagonal architecture is checked automatically by the Konsist tests of the
`architecture-rules` module: the domain must depend only on `commons`, the application on the domain, and use cases
must reside in `application.usecase`. They run as unit tests in the services that extend `ArchitectureKonsistTest`
(`user-service` and `restaurant-service`). Details are given in
[Hexagonal Architecture](hexagonal-architecture.md#verification).

### Load tests

The `loadtest/` module runs a k6 job inside the Kubernetes cluster against a deployed service, while printing the
replica count and the HPA CPU usage. It is used to verify that the services scale horizontally under load; the results
are read on the Grafana dashboards. It is described together with the Kubernetes deployment.

## Continuous integration and coverage

All automated levels run in the `test.yaml` GitHub Actions workflow, called by the CI/CD pipeline:

```bash
./gradlew test integrationTest componentTest e2eTest
./gradlew koverVerify vitestCoverageVerify
```

The second step enforces a minimum coverage of 70%: through Kover for the Kotlin services and through Vitest
thresholds (lines, functions, branches, statements) for the Express.js ones. Locally, the pre-push hook runs the
`check` task, which includes unit, integration and component tests.
