
plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-payment-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-payment-shared.js")
      customField("types", "kotlin/munchies-payment-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-payment-shared.d.ts",
            "default" to "./kotlin/munchies-payment-shared.js",
            "import" to "./kotlin/munchies-payment-shared.js",
          ),
          "./kotlin/payment-modules" to mapOf(
            "types" to "./kotlin/payment-modules.d.ts",
            "default" to "./kotlin/payment-modules.js",
            "import" to "./kotlin/payment-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
