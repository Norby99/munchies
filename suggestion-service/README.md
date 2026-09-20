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
  :suggestion-shared[suggestion-shared]:::unknown
  :suggestion-service[suggestion-service]:::unknown
  :micronaut-commons[micronaut-commons]:::unknown
  :commons[commons]:::unknown

  :suggestion-service -.-> :commons
  :suggestion-service -.-> :micronaut-commons
  :suggestion-service -.-> :suggestion-shared
  :suggestion-shared -.->|commonMainImplementation| :commons

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
