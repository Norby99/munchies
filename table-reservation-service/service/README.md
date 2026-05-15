# `:table-reservation-service:service`

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
  subgraph :table-reservation-service
    direction TB
    :table-reservation-service:service[service]:::unknown
    :table-reservation-service:shared[shared]:::unknown
  end
  :commons[commons]:::unknown

  :table-reservation-service:service -.->|jsImplementation| :commons
  :table-reservation-service:service -.->|jsImplementation| :table-reservation-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
