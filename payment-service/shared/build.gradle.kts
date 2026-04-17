
plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-payment-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-payment-service-shared.js")
      customField("types", "kotlin/munchies-payment-service-shared.d.ts")
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
