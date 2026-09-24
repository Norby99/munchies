# Multiplatform

Our Kotlin and TypeScript services need to agree on a lot: request/response shapes, endpoint paths, authentication
roles, and a handful of small utilities. Rather than writing that twice and keeping the two copies in sync by hand,
we write it once in Kotlin and use [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) (KMP) to
compile it to both targets our services actually run on: the JVM, for our Kotlin/Micronaut services, and JS, for
our Express.js services. A multiplatform module's source is split into ```commonMain```, compiled to every target,
and ```jvmMain```/```jsMain```, compiled only to their respective target for the rare piece of platform-specific
code.

## The `commons` module

[```commons```](https://github.com/Norby99/munchies/tree/master/commons) holds the handful of building blocks every
service needs regardless of stack: a typed ```UUIDEntityId``` used as the base class for every domain identifier, a
small DDD toolkit, an observer-based ```Notification``` port for domain events, an ```EmailValidator```, and a
generic ```API```/```HttpMethod``` abstraction used to build typed REST clients, described below.

Almost all of it lives in ```commonMain``` untouched. The one place it does not is generating a new identifier,
since the JVM and JS standard libraries disagree on how to do that, which is a textbook use of Kotlin's
```expect```/```actual``` mechanism: ```commonMain``` declares what every platform must provide, and each platform
supplies its own implementation.

```kotlin
// commonMain: declares the contract, with no implementation
expect fun getUUID(): String

open class UUIDEntityId(override val value: String = getUUID()) : EntityId<String>(value)
```

```kotlin
// jvmMain
actual fun getUUID(): String = java.util.UUID.randomUUID().toString()
```

```kotlin
// jsMain
actual fun getUUID(): String = kotlin.random.Random.nextLong(Long.MAX_VALUE).toString()
```

## One contract, two languages

Besides ```commons```, every backend service also has its own ```<service>-shared``` multiplatform module,
containing the DTOs, request/response types and endpoint definitions for that service's REST API. Since it is
compiled to both targets, the exact same Kotlin source ends up enforcing the contract on both the service that
implements an endpoint and every other service that calls it, in whichever language they are written.

As a concrete example, [```order-shared```](https://github.com/Norby99/munchies/tree/master/order-shared) defines
```OrderServiceConfig```, an object listing ```order-service```'s base path and every endpoint's sub-path:

```kotlin
@JsExport
object OrderServiceConfig {
    const val SERVICE_PATH = "/orders/"
    const val PAY_ORDER_PATH = "{id}/pay"
    // ...
}
```

```order-service``` itself, written in Kotlin, depends on ```order-shared``` as a plain Gradle project dependency
and uses this same object to declare the route:

```kotlin
// order-service, Kotlin/Micronaut
@Patch(OrderServiceConfig.PAY_ORDER_PATH)
override fun payOrder(@PathVariable id: String): HttpResponse<PayOrderResponse> { /* ... */
}
```

```payment-service```, written in TypeScript, needs to call that very endpoint once a payment succeeds. Instead of
hardcoding the path a second time, it imports the compiled JS/TypeScript version of the same object from the npm
package generated out of ```order-shared```, and builds the request path from the same constants:

```typescript
// payment-service, Express.js
import OrderServiceConfig from "munchies-order-service-shared";

const path = (OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.PAY_ORDER_PATH).replace("{id}", orderId);
await axios.patch(baseUrl + path, "");
```

Both sides read the path from the same Kotlin source, so the two services cannot silently drift apart the way they
could if the path were retyped as a plain string on each end.

Beyond configuration constants, a ```<service>-shared``` module also declares, per use case, a ```<UseCase>API```
interface generic over a ```Response``` type (for example ```OrderAPI.PlaceOrderAPI<Response>```), which the actual
Micronaut controller implements with ```HttpResponse<...>``` as its ```Response```. On the JS side, an equivalent
```Js<UseCase>API``` abstract class specializes the same interface with a ```Promise<...>``` instead, and pairs it
with a small shared HTTP-client abstraction from ```commons``` that already knows the endpoint's path, port, method
and required authentication role, leaving a JS/TypeScript caller only to provide the actual network call. Some
shared modules go further still: ```payment-shared```, for instance, reuses ```commons```' generic
```Notification``` observer port to define a ```PaymentSuccessNotification```, which the domain publishes and
```payment-service``` observes to react to a completed payment.

## Packaging

The two targets are consumed very differently. On the JVM side, a service simply adds the shared module as a
Gradle dependency, exactly like any other subproject:

```kotlin
dependencies {
    implementation(project(":order-shared"))
}
```

On the JS side, Kotlin's IR compiler additionally generates TypeScript type declarations
(```generateTypeScriptDefinitions()```), producing a regular npm package, that our ```pack_<project>``` Gradle task
packs into an installable ```.tgz``` tarball. Gradle handles this tarball dependency through a custom dependency
notation:

```kotlin
dependencies {
    jsImplementation(project(":order-shared"))
}
```

Lastly, express services declare it as a local file dependency in their
```package.json```, the same way they depend on ```commons```:

```json
"munchies-order-service-shared": "file:build/libs/munchies-order-shared.tgz"
```

Because that tarball is rebuilt from source on every build, its content hash changes every time; we strip the
```integrity``` field ```npm``` writes for it in ```package-lock.json```, otherwise a stale hash would make
```npm ci``` reject the freshly rebuilt package.
