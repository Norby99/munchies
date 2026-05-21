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
  :notification-shared[notification-shared]:::unknown
  :notification-service[notification-service]:::unknown
  :commons[commons]:::unknown

  :notification-service -.->|jsImplementation| :commons
  :notification-service -.->|jsImplementation| :notification-shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
