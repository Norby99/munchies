plugins {
  id("multiplatform-base")
}

dependencies {
  commonMainImplementation(project(":commons"))
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-suggestion-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-suggestion-shared.js")
      customField("types", "kotlin/munchies-suggestion-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-suggestion-shared.d.ts",
            "default" to "./kotlin/munchies-suggestion-shared.js",
            "import" to "./kotlin/munchies-suggestion-shared.js",
          ),
          "./kotlin/suggestion-modules" to mapOf(
            "types" to "./kotlin/suggestion-modules.d.ts",
            "default" to "./kotlin/suggestion-modules.js",
            "import" to "./kotlin/suggestion-modules.js",
          ),
        ),
      )
    }
  }
}
