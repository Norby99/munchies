plugins {
  id("express-server")
}

dependencies {
  jsImplementation(project(":commons"))
  jsImplementation(project(":order-shared"))
  jsImplementation(project(":restaurant-shared"))
}

// The express-server convention runs `npm run specs` (tsoa) on build. The scheduler has no tsoa
// controllers, hence no generated OpenAPI spec and no `specs` script: skip that step.
tasks.named("npm_run_specs") {
  enabled = false
}
