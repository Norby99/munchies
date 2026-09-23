# Testing

For testing the application we adopted the "Testing Pyramid strategy", which consists of the following layers:

## Unit, Integration and Component Testing

Each service module keeps unit, integration and component tests in three separate source sets, one per level:

```
*-service/src/
├── test/            # unit tests
├── integrationTest/ # integration tests
└── componentTest/   # component tests
```

### Unit Testing

Unit tests exercise a single class in isolation. Every outbound port is mocked using the MockK library.

### Integration Testing

Integration testing checks that the code integrates correctly with an external dependency, like MongoDB or Kafka. To do
so, we use
the library `Testcontainers` that spins up a Docker container with MongoDB for every test.

```kotlin
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoOrderRepositoryIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        private val mongo = MongoDBContainer("mongo:7.0")
    }

    private lateinit var context: ApplicationContext
    private lateinit var repository: MongoOrderRepository

    @BeforeAll
    fun setup() {
        context = ApplicationContext.run(
            mapOf(
                "mongodb.uri" to "${mongo.connectionString}/order-service",
                "mongodb.package-names[0]" t
                        "com.munchies.order.infrastructure.adapter.outbound.mongo.document",
            ),
            "prod",
        )
        repository = context.getBean(MongoOrderRepository::class.java)
    }

    // clean up the database after each tests
    @AfterEach
    fun cleanup() {
        context.getBean(MongoCrudOrderRepository::class.java).deleteAll()
    }

    @Test
    fun `saves and retrieves an order
    val order = createDeliveryOrder()

    repository.save(order)
    val found = repository.findById(

        found shouldBe order
}
}
```

### Component Testing

Its objective is to test the acceptance criteria of a microservice in isolation. Under the hood the controller
makes and responds to http calls via `Micronaut`. The database is still accessed via `Testcontainers` and the HTTP
responses from other microservices are mocked.

## End-to-end Testing

The objective of End-to-end Testing is to test the system as a whole. A dedicated module called `e2e-test` contains
tests that mimic the behavior of a real user. he tests follow BDD principles using Cucumber, with scenarios written in
Gherkin. To do so we implemented a task, better described in [Deployment](../04-deployment.md)
that starts the whole application using `Docker Compose`.

```gherkin
Feature: Create an order

  Scenario: A authenticated client creates an delivery order
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    Then order is created successfully
```

Implemented then in Kotlin:

```kotlin
@Given("a valid delivery order")
fun aValidDeliveryOrder() {
    requestBody = OrderFixtures.deliveryOrderJson
}

@When("the client places the order")
fun placeTheOrder() {
    try {
        val response = client.toBlocking().exchange(
            HttpRequest.POST("/orders/place", requestBody)
                .cookie(world.authCookie)
                .contentType("application/json"),
            String::class.java,
        )
        world.responseStatus = response.status.code
        world.responseBody = response.body()
    } catch (e: HttpClientResponseException) {
        world.responseStatus = e.status.code
        world.responseBody = e.response.getBody(String::class.java).orElse(null)
    }
}

@Then("order is created successfully")
fun orderIsCreatedSuccessfully() {
    world.responseStatus shouldBe 200
}
```
