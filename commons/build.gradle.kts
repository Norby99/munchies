plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-commons")
      customField("version", "0.1.0")

      customField("main", "kotlin/munchies-commons.js")
      customField("types", "kotlin/munchies-commons.d.ts")
    }
  }
}
