# `:payment-service:service`

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
  :payment-shared[payment-shared]:::unknown
  :payment-service[payment-service]:::unknown
  :commons[commons]:::unknown

  :payment-service -.->|jsImplementation| :commons
  :payment-service -.->|jsImplementation| :payment-shared
  :payment-shared -.->|commonMainImplementation| :commons

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
