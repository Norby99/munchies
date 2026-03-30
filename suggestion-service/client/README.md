# `:suggestion-service:api`

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
  subgraph :suggestion-service
    direction TB
    :suggestion-service:api[api]:::unknown
    :suggestion-service:dto[dto]:::unknown
  end

  :suggestion-service:api -.-> :suggestion-service:dto

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
