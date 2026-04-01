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
    :user-service:service[service]:::unknown
    :user-service:shared[shared]:::unknown
  end
  :commons[commons]:::unknown

  :user-service:service -.-> :commons
  :user-service:service -.-> :user-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
