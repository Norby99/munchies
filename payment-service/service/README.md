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
  subgraph :payment-service
    direction TB
    :payment-service:service[service]:::unknown
    :payment-service:shared[shared]:::unknown
  end
  :commons[commons]:::unknown

  :payment-service:service -.->|jsImplementation| :commons
  :payment-service:service -.->|jsImplementation| :payment-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
