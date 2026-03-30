# `:suggestion-service:service`

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
    :suggestion-service:service[service]:::unknown
  end
  :commons[commons]:::unknown

  :suggestion-service:api -.-> :suggestion-service:dto
  :suggestion-service:service -.-> :commons
  :suggestion-service:service -.-> :suggestion-service:api

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
