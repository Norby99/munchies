plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-restaurant-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-restaurant-shared.js")
      customField("types", "kotlin/munchies-restaurant-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-restaurant-shared.d.ts",
            "default" to "./kotlin/munchies-restaurant-shared.js",
            "import" to "./kotlin/munchies-restaurant-shared.js",
          ),
          "./kotlin/restaurant-modules" to mapOf(
            "types" to "./kotlin/restaurant-modules.d.ts",
            "default" to "./kotlin/restaurant-modules.js",
            "import" to "./kotlin/restaurant-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
