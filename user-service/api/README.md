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
    :user-service:api[api]:::unknown
  end

  :user-service:api -.->|aotApplicationClasspath| :user-service:api

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
