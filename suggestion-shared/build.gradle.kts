plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-suggestion-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-suggestion-shared.js")
      customField("types", "kotlin/munchies-suggestion-shared.d.ts")
    }
  }
}
