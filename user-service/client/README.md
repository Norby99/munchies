# `:user-service:api`

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
  subgraph :user-service
    direction TB
    :user-service:client[client]:::unknown
    :user-service:shared[shared]:::unknown
  end

  :user-service:client -.-> :user-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
