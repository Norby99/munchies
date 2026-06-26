plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-order-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-order-service-shared.js")
      customField("types", "kotlin/munchies-order-service-shared.d.ts")
    }
  }
}
