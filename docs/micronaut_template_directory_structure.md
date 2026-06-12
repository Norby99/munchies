# Service Directory Structure Template

## Overview

Each service follows a **Hexagonal Architecture** pattern organized into three main modules:
- **`service`** - Core application logic and business rules
- **`shared`** - Public DTOs and API interfaces for inter-service communication
- **`client`** - Client adapters for external service consumption (optional)

## Standard Directory Layout
## Layer Descriptions
````
```
<name>-service/
├── build.gradle.kts   
├── README.md
└── src
    ├── main
    │   ├── kotlin
    │   │   └── com
    │   │       └── munchies
    │   │           └── <name>
    │   │               ├── application
    │   │               │   ├── port
    │   │               │   │   └── inbound
    │   │               │   │       ├── Delete<Name>.kt
    │   │               │   │       └── Get<Name>.kt
    │   │               │   └── usecase
    │   │               │       ├── Delete<Name>UseCase.kt
    │   │               │       └── Get<Name>UseCase.kt
    │   │               ├── domain
    │   │               │   ├── factory
    │   │               │   │   └── <Name>Factory.kt
    │   │               │   ├── model
    │   │               │   │   ├── <Name>Id.kt
    │   │               │   │   ├── <Name>.kt
    │   │               │   └── port
    │   │               │       └── <Name>Repository.kt
    │   │               ├── infrastructure
    │   │               │   └── adapter
    │   │               │       ├── dto
    │   │               │       │   └── factory
    │   │               │       │       └── <Name>DTOFactory.kt
    │   │               │       ├── inbound
    │   │               │       │   └── web
    │   │               │       │       ├── config
    │   │               │       │       │   ├── OpenAPI.kt
    │   │               │       │       │   └── <Name>Beans.kt
    │   │               │       │       └── controller
    │   │               │       │           └── Micronaut<Name>Controller.kt
    │   │               │       └── outbound
    │   │               │           ├── http
    │   │               │           │   └── <Other>Service.kt
    │   │               │           ├── kafka
    │   │               │           │   └── EmailConfirmationClient.kt
    │   │               │           └── mongo
    │   │               │               ├── document
    │   │               │               │   └── <Name>Document.kt
    │   │               │               ├── factory
    │   │               │               │   └── <Name>DocumentFactory.kt
    │   │               │               └── repository
    │   │               │                   └── Mongo<Name>Repository.kt
    │   │               └── Main.kt
    │   └── resources
    │       └── application.yml
    └── test
        ├── kotlin
        │   └── com
        │       └── munchies
        │           └── user
        │               ├── application
        │               │   └── usecase
        │               │       ├── Get<Name>UseCaseTest.kt
        │               │       └── Delete<Name>UseCaseTest.kt
        │               ├── architecture
        │               │   └── <Name>ArchitectureKonsistTest.kt
        │               ├── domain
        │               │   └── factory
        │               │       └── <Name>FactoryTest.kt
        │               └── infrastructure
        │                   └── adapter
        │                       ├── inbound
        │                       │   └── web
        │                       │       └── controller
        │                       │           └── Micronaut<Name>ControllerTest.kt
        │                       └── outbound
        │                           └── mongo
        │                               └── repository
        │                                   └── MongoUserRepositoryTest.kt
        └── resources
            └── application.yml
```
### Domain Layer (`domain/`)
**Core business logic - INDEPENDENT from frameworks**
- **model/** - Entities, value objects, and domain rules
- **port/** - Port interfaces that define contracts for technical operations

### Application Layer (`application/`)
**Use cases and orchestration - DEPENDS on domain, INDEPENDENT from frameworks**
- **usecase/** - Implements business flows using domain entities and ports
- **port/inbound/** - Interfaces exposed to adapters
- Coordinates between domain and infrastructure
- Handles failure scenarios and transactions of the business logic

### Infrastructure Layer (`infrastructure/adapter/`)
**Technical implementations - DEPENDS on domain and application**
- **inbound/** - HTTP controllers, message listeners (entry points)
  - **web/** - REST controllers, request/response mapping to business logic's commands and queries
- **outbound/** - Database repositories, external API clients (exit points)

## Naming Conventions

| Layer | Pattern | Example |
|-------|---------|---------|
| Domain Entity | `[Singular]` | `User`, `MenuItem` |
| Domain Value Object | `[Singular]Id` | `UserId`, `MenuItemId` |
| Domain Port | `[Concept]Port` | `UserRepository`, `CachePort` |
| Use Case | `[Action][Entity]UseCase` | `CreateUserUseCase`, `FetchMenuItemUseCase` |
| DTO | `[Entity]DTO` | `UserDTO`, `MenuItemDTO` |
| Controller | `[Resource]Controller` | `UserController`, `MenuItemController` |
| Repository | `[Entity]Repository` | `UserRepository`, `MenuItemRepository` |
| Test | `[Class]Test` | `UserRepositoryTest`, `CreateUserUseCaseTest` |

## Common Patterns

### Adding a New Feature

1. **Define Domain Model** → `domain/model/NewEntity.kt`
2. **Define Domain Port** → `domain/port/NewEntityPort.kt`
3. **Implement Use Case** → `application/usecase/ActionNewEntityUseCase.kt`
4. **Create Inbound Adapter** → `infrastructure/adapter/inbound/web/controller/NewEntityController.kt`
5. **Create Outbound Adapter** → `infrastructure/adapter/outbound/persistence/NewEntityRepository.kt`
6. **Export API** → `shared/infrastructure/adapter/inbound/ServiceAPI.kt`
7. **Add Tests** → Mirror structure in `test/`



## Multi-Module Setup

```gradle
// settings.gradle.kts
include(":[service]-service", ":[service]-shared")

// [service]-service/build.gradle.kts
dependencies{
  implementation(project(":[service]-shared"))
}
```

```yml
// k8s/[service]-deployment.yml
// k8s/[service]-mongodb-pvc.yml
// k8s/[service]-mongodb-statefulset.yml
// k8s/[service]-namespace.yml
```

---

**Note:** This template ensures **clean architecture**, **testability**, and **maintainability** across all microservices.

