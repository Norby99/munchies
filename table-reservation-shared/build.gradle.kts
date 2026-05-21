plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-table-reservation-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-table-reservation-service-shared.js")
      customField("types", "kotlin/munchies-table-reservation-service-shared.d.ts")
    }
  }
}
