# Microservices

The _Munchies_ application is split up into multiple microservices, each with its own purpose:

## Microservices Division

### User Service

**Stack:** Kotlin + Micronaut.

#### Behavior

Microservice responsible for user management like:

- User authentication, registration, deletion.
- Updating user credentials and other information.
- Email verification.

### Restaurant Service

**Stack:** Kotlin + Micronaut.

#### Behavior

Microservice responsible for restaurant management:

- Create new restaurants.
- Handle the menu of each restaurant:
    - Create menu items.
    - Create categories that organize each dish.
- Check for dish availability.

### Order Service

**Stack:** Kotlin + Micronaut.

#### Behavior

Microservice responsible for managing orders:

- Create orders.
- Retrieve order information.
- Advance or discard their status.

### Gateway Service

**Stack:** Express.js.

#### Behavior

The main entry point of the application. Routes every request to the right microservice, managing authentication via
JWT.

### Notification Service

**Stack:** Express.js.

#### Behavior

Microservice responsible for sending notifications to the user through the frontend.

### Payment Service

**Stack:** Express.js.

#### Behavior

Microservice responsible for validation and processing of any payment on the platform, mainly for orders.

### Scheduler Service

**Stack:** Express.js.

#### Behavior

Microservice responsible for delivery scheduling and logistics of every order.

### Table Reservation Service - not implemented

**Stack:** Express.js.

#### Behavior

Microservice responsible for table reservations.

### Frontend Service

**Stack:** Vue.js, using the Composition API.

#### Behavior

This microservice provides a user interface for the end user.

## Microservice implementation

Each backend microservice implements the Hexagonal architecture described in
the [Domain Model](../01-deliverables/domain-model.md):

### Domain Layer

The domain layer contains entities, value objects, aggregates, factories. These are the building blocks for
every business operation of the application.

```kotlin
// domain/model/User.kt
class User private constructor(
    override val id: UserId,
    val profile: UserProfile,
) : Entity<UserId>(id) {

    fun updateEmailAsVerified(): User = User(this.id, this.profile.updateEmailAsVerified())

    companion object {
        // validates username/email/role before constructing a User
        val factory: UserFactory = DefaultUserFactory()
    }
}

// domain/model/UserProfile.kt
data class UserProfile(
    val username: String,
    val email: Email,
    val role: UserRole,
)
```

### Application Layer

This layer contains the business logic. Every operation is encapsulated in "use cases" that can return a result or an
error, handled by a `result` object. They are used by the controller of the microservice and are responsible for
validating input.

```kotlin
// application/port/inbound/RegisterUser.kt
interface RegisterUser {
    fun execute(user: User, credentials: UserCredentials): RegisterUserResult

    companion object {
        sealed interface RegisterUserResult {
            data class Success(val user: User) : RegisterUserResult
            data object UserIsAlreadyRegistered : RegisterUserResult
            data class Failure(val reason: String) : RegisterUserResult
        }
    }
}
```

```kotlin
// application/usecase/RegisterUserUseCase.kt
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val credentialsRepository: UserCredentialsRepository,
    private val hasher: PasswordHasher,
    private val mailer: Mailer,
) : RegisterUser {

    override fun execute(user: User, credentials: UserCredentials): RegisterUserResult {
        return findUser(user)
            ?.let { RegisterUserResult.UserIsAlreadyRegistered }
            ?: try {
                userRepository.save(user)
                credentialsRepository.save(credentials.copy(id = user.id))
                mailer.sendMail(user.profile.email.address, "...")
                RegisterUserResult.Success(user)
            } catch (e: kotlin.Error) {
                RegisterUserResult.Failure(e.localizedMessage)
            }
    }
    // findUser omitted...
}
```

### Infrastructure Layer

#### Persistence

For persistence, each microservice has its own MongoDB database. The CRUD operations are implemented in the `repository`
subpackage. At the same time, to separate Mongo documents from the domain model we implemented the `document` package
that maps each domain object to a Mongo `@MappedEntity`.

```kotlin
// infrastructure/adapter/outbound/mongo/document/UserDocument.kt
@MappedEntity
data class UserDocument(
    @field:Id val id: String,
    val username: String,
    val email: String,
    val isVerified: Boolean,
    val role: String,
)

@MongoRepository
sealed interface MongoCrudUserRepository : CrudRepository<UserDocument, String>

// infrastructure/adapter/outbound/mongo/repository/MongoUserRepository.kt
@Singleton
@Requires(env = ["prod"])
class MongoUserRepository(
    private val repository: MongoCrudUserRepository,
) : UserRepository {

    override fun findById(id: UserId): User? = repository.findById(id.value).map {
        it.toNullableDomain()
    }.orElse(null)

    override fun save(entity: User) {
        repository.save(entity.toDocument())
    }
    // update, delete, findByEmail, findByUsername omitted...
}
```

#### Events

Kafka producers implement a domain outbound port, so the application layer only ever depends on the port
interface, never on Kafka directly. In `order-service`, `notification-service` subscribes to order lifecycle
events published through `OrderNotificationPublisher`:

```kotlin
// domain/port/OrderNotificationPublisher.kt
interface OrderNotificationPublisher {
    fun publishStatusChanged(order: Order)
}

// infrastructure/adapter/outbound/kafka/OrderStatusChangedKafkaClient.kt
@KafkaClient
interface OrderStatusChangedKafkaClient {
    @Topic(OrderStatusChangedNotificationInfo.ORDER_STATUS_CHANGED_TOPIC)
    fun publish(notification: String)
}

// infrastructure/adapter/outbound/kafka/KafkaOrderNotificationPublisher.kt
@Singleton
class KafkaOrderNotificationPublisher(
    private val client: OrderStatusChangedKafkaClient,
) : OrderNotificationPublisher {

    override fun publishStatusChanged(order: Order) {
        client.publish(
            OrderStatusChangedNotification(
                order_id_key = order.id.value,
                restaurant_id_key = order.restaurantId.value,
                customer_id_key = order.customerId.value,
                status_key = order.status.name,
            ).toJson(),
        )
    }
}
```

#### Dependency Injection

A Micronaut `@Factory` wires each use case to its concrete infrastructure dependencies, so the rest of the
application only ever injects the inbound port interface:

```kotlin
// infrastructure/adapter/inbound/web/config/UserBeans.kt
@Factory
class UserBeans {

    @Singleton
    fun registerUser(
        userRepository: UserRepository,
        userCredentialsRepository: UserCredentialsRepository,
        hasher: PasswordHasher,
        mailer: Mailer,
    ): RegisterUser = RegisterUserUseCase(userRepository, userCredentialsRepository, hasher, mailer)

    // one @Singleton factory method per use case...
}
```

### Interfaces Layer

This layer houses the engine of the microservice: the controller that receives REST calls, translates and delegates
them to the inbound ports to the application layer:

```kotlin
// infrastructure/adapter/inbound/web/controller/MicronautUserController.kt
@Controller(value = UserServiceConfig.SERVICE_PATH)
class MicronautUserController(
    private val services: UserServices,
) : UserAPI.GetUserAPI<HttpResponse<GetUserResponse>> {

    private val getUser: GetUser = services.getUser

    @Get(UserServiceConfig.GET_USER_PATH)
    override fun getUser(@PathVariable id: String): HttpResponse<GetUserResponse> {
        return when (val res = getUser.execute(UserId(id))) {
            is GetUser.Companion.GetUserResult.Success -> HttpResponse.ok(
                GetUserResponse(result = res.user.toDTO(), code = HttpStatus.OK.code),
            )
            GetUser.Companion.GetUserResult.NotFound -> throw NotFoundException("User not found")
        }
    }
    // other endpoints follow the same pattern...
}
```

Micronaut lets us define an exception handler once and have it apply to every controller in the service:

```kotlin
// infrastructure/adapter/inbound/web/controller/ExceptionHandlers.kt
@Singleton
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException, HttpResponse<ErrorResponse>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: NotFoundException,
    ): HttpResponse<ErrorResponse> {
        return HttpResponse.status<ErrorResponse>(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(result = exception.message ?: "Resource not found", code = HttpStatus.NOT_FOUND.code))
    }
}
```

### Presentation Layer

To facilitate the controller's job, every DTO has its own mapper that converts it to a domain object through extension
methods and vice versa:

```kotlin
// infrastructure/adapter/dto/factory/UserDTOFactory.kt
object UserDTOFactory {
    fun User.toDTO(): UserDTO = UserDTO(
        id = this.id.value,
        username = this.profile.username,
        email = this.profile.email.address,
        role = this.profile.role.toString(),
        isEmailVerified = this.profile.email.isVerified,
    )
    // toDomain omitted...
}
```

### Configuration Layer

This layer contains the configuration files used to set up the application using Micronaut properties
or the `application.yml` file:

```kotlin
// infrastructure/adapter/inbound/web/config/OpenAPI.kt
@OpenAPIDefinition(info = Info(title = "Munchies User Service API", version = "1.0"))
@OpenAPIInclude(classes = [MicronautUserController::class])
object OpenAPI
```

```yaml
# src/main/resources/application.yml
micronaut:
  server:
    port: 8080
  application:
    name: user-service
mongodb:
  uri: mongodb://localhost:27017/user-service
kafka:
  bootstrap:
    servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```
