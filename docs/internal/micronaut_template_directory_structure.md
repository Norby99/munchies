# Service Directory Structure Template

## Overview

Each service follows a **Hexagonal Architecture** pattern organized into three main modules:
- **`service`** - Core application logic and business rules
- **`shared`** - Public DTOs and API interfaces for inter-service communication
- **`client`** - Client adapters for external service consumption (optional)

## Standard Directory Layout

```
<service-name>/
│
├── client/                                    # Client adapter module
│   ├── build.gradle.kts
│   ├── README.md
│   └── src/main/kotlin/com/munchies/<service>/
│       └── infrastructure/adapter/inbound/web/
│           └── Micronaut[Service]Client.kt     # Client adapter for external service consumption
│
├── service/                                   # Main service module
│   ├── build.gradle.kts
│   ├── README.md
│   └── src/
│       ├── main/kotlin/com/munchies/<service>/
│       │   ├── Main.kt                                # Application entry point
│       │   │ 
│       │   ├── application/                   # Application/Orchestration layer
│       │   │   ├── usecase/                   # Use case implementations
│       │   │   │   ├── Primary[Action]UseCase.kt
│       │   │   │   └── ...
│       │   │   └── port/inbound/              # Inbound port interfaces
│       │   │       ├── [Action]Port.kt
│       │   │       └── ...
│       │   │
│       │   ├── domain/                        # Domain/Business layer
│       │   │   ├── model/                     # Domain entities & value objects
│       │   │   │   ├── [Entity].kt
│       │   │   │   ├── [EntityId].kt          # Value object for IDs
│       │   │   │   └── ...
│       │   │   ├── factory/                   # Domain factories
│       │   │   │   └── [Entity]Factory.kt
│       │   │   └── port/                      # Domain ports (abstractions)
│       │   │       ├── [Technology]Port.kt    # e.g., DatabasePort, CachePort
│       │   │       └── ...
│       │   │
│       │   └── infrastructure/adapter/        # Infrastructure/Technical layer
│       │       ├── dto/factory/               # DTO factories for mapping between domain and external representations
│       │       │   └── [Domain]DTOFactory.kt
│       │       ├── inbound/                   # Inbound adapters
│       │       │   └── web/                   # HTTP/Web adapters
│       │       │       ├── config/
│       │       │       │   ├── [Service]Beans.kt   # Bean definitions and dependency injection
│       │       │       │   └── OpenApi.kt          # OpenAPI configuration
│       │       │       └── controller/
│       │       │           └── [Resource]Controller.kt
│       │       │
│       │       └── outbound/                  # Outbound adapters
│       │           ├── memory/           # InMemory Database adapters
│       │           │   └── Memory[Entity]Repository.kt
│       │           ├── mongo/           # MondoDB Database adapters
│       │           │   ├── document/    
│       │           │   │   ├── [Service]Document.kt  # Domain document representation 
│       │           │   │   └── ...    
│       │           │   ├── mapper/     # Domain entities mapped to MongoDB documents
│       │           │   │   └── DomainToDocument.kt     
│       │           │   ├── repository/    # MongoDB repository implementation
│       │           │   │   └── Mongo[Service]Repository.kt
│       │           │   └── factory/           # Factory implementations for technical components
│       │           │       └── [Entity]Factory.kt      
│       │           └── external/              # External service adapters
│       │               └── [Service]Client.kt
│       │
│       └── test/kotlin/com/munchies/<service>/
│           ├── application/
│           │   └── usecase/                   # Use case unit tests
│           │       └── [UseCase]Test.kt
│           ├── domain/                        # Domain logic tests
│           │   └── model/
│           │       └── [Entity]Test.kt
│           ├── architecture/                  # Architecture compliance tests
│           │   └── [Service]ArchitectureTest.kt
│           └── infrastructure/
│               └── adapter/
│                   ├── inbound/
│                   │   └── web/controller/
│                   │       └── [Resource]ControllerTest.kt
│                   └── outbound/
│                       ├── persistence/
│                       │   └── [Entity]RepositoryTest.kt
│                       └── external/
│                           └── [Service]ClientTest.kt
│
└── shared/                                    # Shared module (inter-service communication)
    ├── build.gradle.kts
    ├── README.md
    └── src/main/kotlin/com/munchies/<service>/infrastructure/adapter/
        ├── dto/                               # Data Transfer Objects
        │   ├── [Entity]DTO.kt
        │   └── ...
        └── inbound/                           # Public interfaces
            ├── [Service]API.kt                # Main API interface
            ├── request/                       # Request DTOs for API
            │   ├── [Action]Request.kt
            │   └── ...          
            └── web/config/
                └── [Service]ServiceConfig.kt  # Ports and paths

```

## Layer Descriptions

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
- **dto/** - Data serialization and mapping

### Shared Module (`shared/`)
**Public API for inter-service communication**
- Exports DTOs and API interfaces only
- Minimal dependencies
- Used by other services via `@Client` annotation

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
include(":[service]-service:service", ":[service]-service:shared", ":[service]-service:client")

// Build dependencies
service depends on: shared
client depends on: shared
```

```yml
// k8s/[service]-deployment.yml
// k8s/[service]-mongodb-pvc.yml
// k8s/[service]-mongodb-statefulset.yml
// k8s/[service]-namespace.yml
```

---

**Note:** This template ensures **clean architecture**, **testability**, and **maintainability** across all microservices.

