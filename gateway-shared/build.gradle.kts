plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-gateway-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-gateway-shared.js")
      customField("types", "kotlin/munchies-gateway-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-gateway-shared.d.ts",
            "default" to "./kotlin/munchies-gateway-shared.js",
            "import" to "./kotlin/munchies-gateway-shared.js",
          ),
          "./kotlin/gateway-modules" to mapOf(
            "types" to "./kotlin/gateway-modules.d.ts",
            "default" to "./kotlin/gateway-modules.js",
            "import" to "./kotlin/gateway-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
