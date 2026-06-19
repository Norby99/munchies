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

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-commons.d.ts",
            "default" to "./kotlin/munchies-commons.js",
            "import" to "./kotlin/munchies-commons.js",
          ),
          "./kotlin/commons-modules" to mapOf(
            "types" to "./kotlin/commons-modules.d.ts",
            "default" to "./kotlin/commons-modules.js",
            "import" to "./kotlin/commons-modules.js",
          ),
        ),
      )
    }
  }
}
