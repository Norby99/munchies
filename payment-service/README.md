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
  :order-shared[order-shared]:::unknown
  :commons[commons]:::unknown

  :order-shared -.->|commonMainImplementation| :commons
  :payment-service -.->|jsImplementation| :commons
  :payment-service -.->|jsImplementation| :order-shared
  :payment-service -.->|jsImplementation| :payment-shared
  :payment-shared -.->|commonMainImplementation| :commons

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
