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

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-user-shared.d.ts",
            "default" to "./kotlin/munchies-user-shared.js",
            "import" to "./kotlin/munchies-user-shared.js",
          ),
          "./kotlin/user-modules" to mapOf(
            "types" to "./kotlin/user-modules.d.ts",
            "default" to "./kotlin/user-modules.js",
            "import" to "./kotlin/user-modules.js",
          ),
        ),
      )
    }
  }
}

dependencies {
  commonMainImplementation(project(":commons"))
}
