plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-user-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-user-shared.js")
      customField("types", "kotlin/munchies-user-shared.d.ts")
    }
  }
}
