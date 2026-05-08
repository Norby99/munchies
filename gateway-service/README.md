# `:gateway-service`

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
    :notification-service:shared[shared]:::unknown
  end
  subgraph :payment-service
    direction TB
    :payment-service:shared[shared]:::unknown
  end
  subgraph :table-reservation-service
    direction TB
    :table-reservation-service:shared[shared]:::unknown
  end
  subgraph :user-service
    direction TB
    :user-service:shared[shared]:::unknown
  end
  :gateway-service[gateway-service]:::unknown

  :gateway-service -.->|jsImplementation| :notification-service:shared
  :gateway-service -.->|jsImplementation| :payment-service:shared
  :gateway-service -.->|jsImplementation| :table-reservation-service:shared
  :gateway-service -.->|jsImplementation| :user-service:shared

classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```
<!--endregion-->
