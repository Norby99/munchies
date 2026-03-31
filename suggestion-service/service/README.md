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
    :suggestion-service:service[service]:::unknown
    :suggestion-service:shared[shared]:::unknown
  end
  :commons[commons]:::unknown

  :suggestion-service:service -.-> :commons
  :suggestion-service:service -.-> :suggestion-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
