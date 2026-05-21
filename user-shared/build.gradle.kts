plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-user-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-user-service-shared.js")
      customField("types", "kotlin/munchies-user-service-shared.d.ts")
    }
  }
}
