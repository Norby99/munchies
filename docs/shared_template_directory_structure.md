```

<name>-shared/
├── build.gradle.kts
├── README.md
└── src
    ├── commonMain
    │   └── kotlin
    │       └── com
    │           └── munchies
    │               └── <name>
    │                   └── infrastructure
    │                       └── adapter
    │                           ├── dto
    │                           │   └── <Name>DTO.kt
    │                           ├── inbound
    │                           │   ├── request
    │                           │   │   └── Delete<Name>Request.kt
    │                           │   ├── response
    │                           │   │   └── Delete<Name>Response.kt
    │                           │   ├── <Name>API.kt
    │                           │   └── web
    │                           │       └── config
    │                           │           └── <Name>ServiceConfig.kt
    │                           └── outbound
    │                               └── notification
    │                                   ├── <Name>NotificationInfo.kt
    │                                   ├── <Name>Notification.kt
    │                                   ├── <Name>NotificationObserver.kt
    │                                   └── <Name>NotificationSubject.kt
    └── jsMain
        └── kotlin
            └── com
                └── munchies
                    └── <name>
                        └── infrastructure
                            └── adapter
                                └── inbound
                                    └── Js<Name>API.kt
```