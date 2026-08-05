# `:order-service`

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
  :order-shared[order-shared]:::unknown
  :order-service[order-service]:::unknown
  :commons[commons]:::unknown

  :order-service -.-> :commons
  :order-service -.-> :order-shared
  :order-shared -.->|commonMainImplementation| :commons

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
