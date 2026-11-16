plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-table-reservation-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-table-reservation-shared.js")
      customField("types", "kotlin/munchies-table-reservation-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-table-reservation-shared.d.ts",
            "default" to "./kotlin/munchies-table-reservation-shared.js",
            "import" to "./kotlin/munchies-table-reservation-shared.js",
          ),
          "./kotlin/table-reservation-modules" to mapOf(
            "types" to "./kotlin/table-reservation-modules.d.ts",
            "default" to "./kotlin/table-reservation-modules.js",
            "import" to "./kotlin/table-reservation-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
