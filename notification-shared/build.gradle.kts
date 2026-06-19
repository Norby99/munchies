plugins {
  id("multiplatform-base")
}

kotlin {
  js(IR) {
    compilations["main"].packageJson {
      customField("name", "munchies-notification-service-shared")
      customField("version", "0.1.0")
      customField("main", "kotlin/munchies-notification-shared.js")
      customField("types", "kotlin/munchies-notification-shared.d.ts")

      customField(
        "exports",
        mapOf(
          "." to mapOf(
            "types" to "./kotlin/munchies-notification-shared.d.ts",
            "default" to "./kotlin/munchies-notification-shared.js",
            "import" to "./kotlin/munchies-notification-shared.js",
          ),
          "./kotlin/notification-modules" to mapOf(
            "types" to "./kotlin/notification-modules.d.ts",
            "default" to "./kotlin/notification-modules.js",
            "import" to "./kotlin/notification-modules.js",
          ),
        ),
      )
    }
  }
}
