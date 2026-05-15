plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-notification-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-notification-service-shared.js")
      customField("types", "kotlin/munchies-notification-service-shared.d.ts")
    }
  }
}
