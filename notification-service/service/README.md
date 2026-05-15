# `:notification-service:service`

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
  subgraph :notification-service
    direction TB
    :notification-service:service[service]:::unknown
    :notification-service:shared[shared]:::unknown
  end
  :commons[commons]:::unknown

  :notification-service:service -.->|jsImplementation| :commons
  :notification-service:service -.->|jsImplementation| :notification-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
