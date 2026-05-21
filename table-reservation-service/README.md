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
  :table-reservation-shared[table-reservation-shared]:::unknown
  :table-reservation-service[table-reservation-service]:::unknown
  :commons[commons]:::unknown

  :table-reservation-service -.->|jsImplementation| :commons
  :table-reservation-service -.->|jsImplementation| :table-reservation-shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
