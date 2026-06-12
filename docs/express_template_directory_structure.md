```

<name>-service/
├── build.gradle.kts
├── package.json
├── package-lock.json
├── README.md
├── src
│   ├── main
│   │   └── ts
│   │       ├── application
│   │       │   └── usecase
│   │       ├── domain
│   │       │   ├── external-modules.ts
│   │       │   ├── model
│   │       │   │   ├── <Name>Id.ts
│   │       │   │   └── <Name>.ts
│   │       │   └── port
│   │       │       └── <name>-repository.ts
│   │       ├── index.ts
│   │       └── infrastructure
│   │           └── adapter
│   │               ├── inbound
│   │               │   └── web
│   │               │       └── controller
│   │               │           └── controller.ts
│   │               └── outbound
│   │                   └── mongo
│   │                       ├── config
│   │                       │   └── db.ts
│   │                       ├── document
│   │                       │   └── <name>-document.ts
│   │                       ├── factory
│   │                       │   └── <name>-factory.ts
│   │                       └── repository
│   │                           └── <name>-mongo-repository.ts
│   └── test
│       └── ts
│           └── infrastructure
│               └── adapter
│                   ├── inbound
│                   │   └── web
│                   │       └── controller
│                   │           └── controller.test.ts
│                   └── outbound
│                       └── mongo
│                           └── repository
│                               └── <name>-mongo-repository.test.ts
├── tsconfig.json
├── tsoa.json
└── vitest.config.ts
```

```gradle

// settings.gradle.kts
include(":<name>-service", ":<name>-shared")

// <name>-service/build.gradle.kts
dependencies{
  jsImplementation(":<other-service>-shared")
}
```

```json
// <name>-service/package.json
"dependencies":{
  "munchies-<other-service>-shared": "file:./build/libs/munchies-<other-service>-shared.tgz"
}

// <name>-service/package-lock.json
"node_modules/munchies-<other-service>-shared": {
  "integrity": "...."  // delete this line
}
```
