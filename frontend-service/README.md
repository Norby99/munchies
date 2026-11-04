# Frontend Service

## Running

The container image builds the application and serves it with nginx, which forwards API calls to the gateway at
`GATEWAY_URL`. It is built by `./gradlew :frontend-service:dockerBuild`, and started by `./gradlew composeUp` or
`./gradlew deploy`.

For development with hot reload, use `npm install && npm run dev`, which proxies API calls to `localhost:8086`.
