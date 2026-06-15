# `:user-service`

## Module dependency graph

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  :user-shared[user-shared]:::unknown
  :user-service[user-service]:::unknown
  :payment-shared[payment-shared]:::unknown
  :commons[commons]:::unknown

  :payment-shared -.->|commonMainImplementation| :commons
  :user-service -.-> :commons
  :user-service -.-> :payment-shared
  :user-service -.-> :user-shared
  :user-shared -.->|commonMainImplementation| :commons

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
