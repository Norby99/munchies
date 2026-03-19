# `:user-service`

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
    :user-service:dto[dto]:::unknown
    :user-service:service[service]:::unknown
  end
  :commons[commons]:::unknown

  :user-service:api -.-> :user-service:dto
  :user-service:service -.-> :commons
  :user-service:service -.-> :user-service:api

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
