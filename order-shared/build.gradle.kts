plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-order-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-order-shared.js")
      customField("types", "kotlin/munchies-order-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-order-shared.d.ts",
            "default" to "./kotlin/munchies-order-shared.js",
            "import" to "./kotlin/munchies-order-shared.js",
          ),
          "./kotlin/order-modules" to mapOf(
            "types" to "./kotlin/order-modules.d.ts",
            "default" to "./kotlin/order-modules.js",
            "import" to "./kotlin/order-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
